package ru.megateam.lab.service;

import ru.megateam.lab.domain.Instrument;
import ru.megateam.lab.repository.JdbcInstrumentRepository;

import java.util.List;

public class InstrumentService {
    private final JdbcInstrumentRepository repository;

    public InstrumentService(JdbcInstrumentRepository repository) {
        this.repository = repository;
    }

    public long instAdd(String name) {
        return repository.save(name);
    }

    public boolean exists(long id) {
        return repository.exists(id);
    }

    public String getName(long id) {
        Instrument instrument = repository.findById(id);
        return instrument != null ? instrument.getName() : null;
    }

    public Instrument getById(long id) {
        return repository.findById(id);
    }

    public Long getId(String name) {
        Instrument instrument = repository.findByName(name);
        return instrument != null ? instrument.getId() : null;
    }

    public List<Instrument> getAll() {
        return repository.findAll();
    }
}