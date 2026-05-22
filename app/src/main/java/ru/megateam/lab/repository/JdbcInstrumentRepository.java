package ru.megateam.lab.repository;

import ru.megateam.lab.domain.Instrument;
import ru.megateam.lab.persistence.DbConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcInstrumentRepository {
    private final DbConnectionManager connectionManager;

    public JdbcInstrumentRepository(DbConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public List<Instrument> findAll() {
        List<Instrument> instruments = new ArrayList<>();
        String sql = "SELECT id, name FROM instruments ORDER BY id";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Instrument instrument = new Instrument();
                instrument.setId(rs.getLong("id"));
                instrument.setName(rs.getString("name"));
                instruments.add(instrument);
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении instruments из БД", e);
        }

        return instruments;
    }

    public Instrument findById(long id) {
        String sql = "SELECT id, name FROM instruments WHERE id = ?";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Instrument instrument = new Instrument();
                    instrument.setId(rs.getLong("id"));
                    instrument.setName(rs.getString("name"));
                    return instrument;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске instrument по id", e);
        }

        return null;
    }

    public Instrument findByName(String name) {
        String sql = "SELECT id, name FROM instruments WHERE LOWER(name) = LOWER(?)";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Instrument instrument = new Instrument();
                    instrument.setId(rs.getLong("id"));
                    instrument.setName(rs.getString("name"));
                    return instrument;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске instrument по name", e);
        }

        return null;
    }

    public boolean exists(long id) {
        String sql = "SELECT 1 FROM instruments WHERE id = ?";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке instrument", e);
        }
    }

    public long save(String name) {
        String sql = "INSERT INTO instruments(name) VALUES (?)";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, name);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new RuntimeException("Не удалось получить id нового instrument");

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при сохранении instrument в БД", e);
        }
    }
}
