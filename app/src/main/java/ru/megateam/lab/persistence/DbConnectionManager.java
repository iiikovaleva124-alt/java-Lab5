package ru.megateam.lab.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbConnectionManager {
    private final String url;
    private final String username;
    private final String password;

    public DbConnectionManager() {
        Properties properties = new Properties();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Файл db.properties не найден");
            }

            properties.load(inputStream);

            this.url = properties.getProperty("db.url");
            this.username = properties.getProperty("db.username");
            this.password = properties.getProperty("db.password");

            if (url == null || username == null || password == null) {
                throw new RuntimeException("В db.properties отсутствуют обязательные поля: db.url, db.username, db.password");
            }

        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения db.properties", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
