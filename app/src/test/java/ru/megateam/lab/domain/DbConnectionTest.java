package ru.megateam.lab.domain;

import ru.megateam.lab.persistence.DbConnectionManager;

import java.sql.Connection;

public class DbConnectionTest {
    public static void main(String[] args) {
        try {
            DbConnectionManager manager = new DbConnectionManager();
            try (Connection connection = manager.getConnection()) {
                System.out.println("Подключение к БД успешно");
                System.out.println("AutoCommit = " + connection.getAutoCommit());
            }
        } catch (Exception e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
