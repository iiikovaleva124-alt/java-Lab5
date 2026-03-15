package ru.megateam.lab.service;

import java.util.HashMap;
import java.util.Map;

public class InstrumentService {
    private final Map<Long, String> instruments = new HashMap<>(); // id -> name
    private long nextId = 1;

    public long add(String name) {
        long id = nextId++;
        instruments.put(id, name);
        return id;
    }

    public boolean exists(long id) {
        return instruments.containsKey(id);
    }

    public String getName(long id) {
        return instruments.get(id);
    }
}
