package ru.megateam.lab.service;

import ru.megateam.lab.domain.Sample;
import ru.megateam.lab.repository.JdbcSampleRepository;

import java.util.List;

public class SampleService {
    private final JdbcSampleRepository repository;

    public SampleService(JdbcSampleRepository repository) {
        this.repository = repository;
    }

    public long sampleAdd(String name) {
        return repository.save(name);
    }

    public boolean exists(long id) {
        return repository.exists(id);
    }

    public String getName(long id) {
        Sample sample = repository.findById(id);
        return sample != null ? sample.getName() : null;
    }

    public Long getId(String name) {
        Sample sample = repository.findByName(name);
        return sample != null ? sample.getId() : null;
    }

    public List<Sample> getAll() {
        return repository.findAll();
    }

    public Sample getById(long id) {
        return repository.findById(id);
    }
}