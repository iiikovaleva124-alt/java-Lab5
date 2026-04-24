package ru.megateam.lab.service;

import java.util.HashMap;
import java.util.Map;

public class InstrumentService {
    private final Map<Long, Instrument> instruments = new HashMap<>();
    private long nextId = 1;

    public long InstAdd(String name) {
        long id = nextId++;
        Instrument instrument = new Instrument(id, name);
        instruments.put(id, instrument);
        return id;
    }

    public boolean exists(long id) {
        return instruments.containsKey(id);
    }

    public String getName(long id) {
        return instruments.get(id);
    }
}
