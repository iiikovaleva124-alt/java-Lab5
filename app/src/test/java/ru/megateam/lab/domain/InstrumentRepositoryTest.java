package ru.megateam.lab.domain;

import ru.megateam.lab.persistence.DbConnectionManager;
import ru.megateam.lab.repository.JdbcInstrumentRepository;

public class InstrumentRepositoryTest {
    public static void main(String[] args) {
        DbConnectionManager manager = new DbConnectionManager();
        JdbcInstrumentRepository repository = new JdbcInstrumentRepository(manager);

        var instruments = repository.findAll();

        for (var instrument : instruments) {
            System.out.println(instrument.getId() + " | " + instrument.getName());
        }
    }
}
