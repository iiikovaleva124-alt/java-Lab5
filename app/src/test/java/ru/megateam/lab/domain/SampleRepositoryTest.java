package ru.megateam.lab.domain;

import ru.megateam.lab.persistence.DbConnectionManager;
import ru.megateam.lab.repository.JdbcSampleRepository;

public class SampleRepositoryTest {
    public static void main(String[] args) {
        DbConnectionManager manager = new DbConnectionManager();
        JdbcSampleRepository repository = new JdbcSampleRepository(manager);

        var samples = repository.findAll();

        for (var sample : samples) {
            System.out.println(sample.getId() + " | " + sample.getName());
        }
    }
}
