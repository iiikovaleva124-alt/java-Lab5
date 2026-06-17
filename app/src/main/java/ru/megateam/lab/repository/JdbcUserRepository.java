package ru.megateam.lab.repository;

import ru.megateam.lab.domain.User;
import ru.megateam.lab.persistence.DbConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {
    private final DbConnectionManager connectionManager;

    public JdbcUserRepository(DbConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void save(User user) {
        // Проверяем существует ли пользователь
        Optional<User> existing = findByLogin(user.getLogin());

        if (existing.isPresent()) {
            // Обновляем существующего
            String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, user.getPassword());
                stmt.setString(2, user.getLogin());
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("Ошибка при обновлении пользователя", e);
            }
        } else {
            // Создаём нового
            String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, user.getLogin());
                stmt.setString(2, user.getPassword());
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("Ошибка при добавлении пользователя", e);
            }
        }
    }

    @Override
    public Optional<User> findByLogin(String login) {
        String sql = "SELECT id, username, password_hash FROM users WHERE username = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setLogin(rs.getString("username"));
                    user.setPassword(rs.getString("password_hash"));
                    return Optional.of(user);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске пользователя", e);
        }

        return Optional.empty();
    }

    @Override
    public boolean existsByLogin(String login) {
        return findByLogin(login).isPresent();
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password_hash FROM users";

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setLogin(rs.getString("username"));
                user.setPassword(rs.getString("password_hash"));
                users.add(user);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении списка пользователей", e);
        }

        return users;
    }
}