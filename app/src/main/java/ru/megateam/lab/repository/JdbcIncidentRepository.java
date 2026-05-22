package ru.megateam.lab.repository;

import ru.megateam.lab.domain.Comment;
import ru.megateam.lab.domain.Incident;
import ru.megateam.lab.domain.IncidentSeverity;
import ru.megateam.lab.domain.IncidentStatus;
import ru.megateam.lab.persistence.DbConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcIncidentRepository implements IncidentRepository {

    private final DbConnectionManager connectionManager;

    // Временно comments оставляем в памяти, как промежуточный этап
    private final Map<Long, List<Comment>> comments = new HashMap<>();
    private long nextCommentId = 1L;

    public JdbcIncidentRepository(DbConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Incident add(Incident incident) {
        String sql = """
                INSERT INTO incidents
                (title, description, severity, status, sample_id, instrument_id, owner_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, incident.getTitle());
            statement.setString(2, incident.getDescription());
            statement.setString(3, incident.getSeverity().name());
            statement.setString(4, incident.getStatus().name());
            statement.setLong(5, incident.getSampleId());
            statement.setLong(6, incident.getInstrumentId());
            statement.setLong(7, incident.getOwnerId());
            statement.setTimestamp(8, Timestamp.from(incident.getCreatedAt()));
            statement.setTimestamp(9, Timestamp.from(incident.getUpdatedAt()));

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Не удалось добавить incident");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    incident.setId(generatedKeys.getLong(1));
                } else {
                    throw new RuntimeException("Не удалось получить id нового incident");
                }
            }

            return incident;

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при добавлении incident в БД", e);
        }
    }

    @Override
    public Optional<Incident> getById(long id) {
        String sql = """
                SELECT i.id,
                       i.title,
                       i.description,
                       i.severity,
                       i.status,
                       i.sample_id,
                       i.instrument_id,
                       i.owner_id,
                       u.username AS owner_username,
                       i.created_at,
                       i.updated_at
                FROM incidents i
                JOIN users u ON u.id = i.owner_id
                WHERE i.id = ?
                """;

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapIncident(rs));
                }
            }

            return Optional.empty();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении incident по id", e);
        }
    }

    @Override
    public List<Incident> getAll() {
        List<Incident> incidents = new ArrayList<>();

        String sql = """
                SELECT i.id,
                       i.title,
                       i.description,
                       i.severity,
                       i.status,
                       i.sample_id,
                       i.instrument_id,
                       i.owner_id,
                       u.username AS owner_username,
                       i.created_at,
                       i.updated_at
                FROM incidents i
                JOIN users u ON u.id = i.owner_id
                ORDER BY i.id
                """;

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                incidents.add(mapIncident(rs));
            }

            return incidents;

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении incidents из БД", e);
        }
    }

    @Override
    public Incident update(Incident incident) {
        String sql = """
                UPDATE incidents
                SET title = ?,
                    description = ?,
                    severity = ?,
                    status = ?,
                    sample_id = ?,
                    instrument_id = ?,
                    owner_id = ?,
                    updated_at = ?
                WHERE id = ?
                """;

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            incident.setUpdatedAt(Instant.now());

            statement.setString(1, incident.getTitle());
            statement.setString(2, incident.getDescription());
            statement.setString(3, incident.getSeverity().name());
            statement.setString(4, incident.getStatus().name());
            statement.setLong(5, incident.getSampleId());
            statement.setLong(6, incident.getInstrumentId());
            statement.setLong(7, incident.getOwnerId());
            statement.setTimestamp(8, Timestamp.from(incident.getUpdatedAt()));
            statement.setLong(9, incident.getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Incident с id=" + incident.getId() + " не найден");
            }

            return incident;

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при обновлении incident", e);
        }
    }

    @Override
    public boolean remove(long id) {
        String sql = "DELETE FROM incidents WHERE id = ?";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            int affectedRows = statement.executeUpdate();

            comments.remove(id);
            return affectedRows > 0;

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении incident", e);
        }
    }

    @Override
    public long addComment(long incidentId, Comment comment) {
        long id = nextCommentId++;
        comment.setId(id);
        comments.computeIfAbsent(incidentId, k -> new ArrayList<>()).add(comment);
        return id;
    }

    @Override
    public List<Comment> getComments(long incidentId) {
        return comments.getOrDefault(incidentId, List.of());
    }

    @Override
    public Map<Long, List<Comment>> getAllCommentsMap() {
        Map<Long, List<Comment>> copy = new HashMap<>();

        for (Map.Entry<Long, List<Comment>> entry : comments.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        return copy;
    }

    @Override
    public void replaceAllComments(Map<Long, List<Comment>> newComments) {
        comments.clear();

        long maxCommentId = 0;

        for (Map.Entry<Long, List<Comment>> entry : newComments.entrySet()) {
            List<Comment> copiedList = new ArrayList<>(entry.getValue());
            comments.put(entry.getKey(), copiedList);

            for (Comment comment : copiedList) {
                if (comment.getId() > maxCommentId) {
                    maxCommentId = comment.getId();
                }
            }
        }

        nextCommentId = maxCommentId + 1;
    }

    @Override
    public void replaceAll(List<Incident> incidents) {
        String deleteSql = "DELETE FROM incidents";
        String insertSql = """
                INSERT INTO incidents
                (id, title, description, severity, status, sample_id, instrument_id, owner_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = connectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                deleteStatement.executeUpdate();
            }

            try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                for (Incident incident : incidents) {
                    insertStatement.setLong(1, incident.getId());
                    insertStatement.setString(2, incident.getTitle());
                    insertStatement.setString(3, incident.getDescription());
                    insertStatement.setString(4, incident.getSeverity().name());
                    insertStatement.setString(5, incident.getStatus().name());
                    insertStatement.setLong(6, incident.getSampleId());
                    insertStatement.setLong(7, incident.getInstrumentId());
                    insertStatement.setLong(8, incident.getOwnerId());
                    insertStatement.setTimestamp(9, Timestamp.from(incident.getCreatedAt()));
                    insertStatement.setTimestamp(10, Timestamp.from(incident.getUpdatedAt()));
                    insertStatement.addBatch();
                }

                insertStatement.executeBatch();
            }

            connection.commit();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при replaceAll incidents", e);
        }
    }

    @Override
    public void deleteById(long id) {
        remove(id);
    }

    private Incident mapIncident(ResultSet rs) throws Exception {
        Incident incident = new Incident();

        incident.setId(rs.getLong("id"));
        incident.setTitle(rs.getString("title"));
        incident.setDescription(rs.getString("description"));
        incident.setSeverity(IncidentSeverity.valueOf(rs.getString("severity")));
        incident.setStatus(IncidentStatus.valueOf(rs.getString("status")));
        incident.setSampleId(rs.getLong("sample_id"));
        incident.setInstrumentId(rs.getLong("instrument_id"));
        incident.setOwnerId(rs.getLong("owner_id"));
        incident.setOwnerUsername(rs.getString("owner_username"));

        Timestamp createdAtTs = rs.getTimestamp("created_at");
        Timestamp updatedAtTs = rs.getTimestamp("updated_at");

        if (createdAtTs != null) {
            incident.setCreatedAt(createdAtTs.toInstant());
        }
        if (updatedAtTs != null) {
            incident.setUpdatedAt(updatedAtTs.toInstant());
        }

        return incident;
    }
}
