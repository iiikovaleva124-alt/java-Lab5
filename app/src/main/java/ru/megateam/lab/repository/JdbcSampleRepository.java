package ru.megateam.lab.repository;

import ru.megateam.lab.domain.Sample;
import ru.megateam.lab.persistence.DbConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcSampleRepository {
    private final DbConnectionManager connectionManager;

    public JdbcSampleRepository(DbConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public List<Sample> findAll() {
        List<Sample> samples = new ArrayList<>();
        String sql = "SELECT id, name FROM samples ORDER BY id";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Sample sample = new Sample();
                sample.setId(rs.getLong("id"));
                sample.setName(rs.getString("name"));
                samples.add(sample);
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении samples из БД", e);
        }

        return samples;
    }

    public Sample findById(long id) {
        String sql = "SELECT id, name FROM samples WHERE id = ?";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Sample sample = new Sample();
                    sample.setId(rs.getLong("id"));
                    sample.setName(rs.getString("name"));
                    return sample;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске sample по id", e);
        }

        return null;
    }

    public Sample findByName(String name) {
        String sql = "SELECT id, name FROM samples WHERE LOWER(name) = LOWER(?)";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Sample sample = new Sample();
                    sample.setId(rs.getLong("id"));
                    sample.setName(rs.getString("name"));
                    return sample;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске sample по name", e);
        }

        return null;
    }

    public boolean exists(long id) {
        String sql = "SELECT 1 FROM samples WHERE id = ?";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке sample", e);
        }
    }

    public long save(String name) {
        String sql = "INSERT INTO samples(name) VALUES (?)";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, name);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new RuntimeException("Не удалось получить id нового sample");

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при сохранении sample в БД", e);
        }
    }
}
