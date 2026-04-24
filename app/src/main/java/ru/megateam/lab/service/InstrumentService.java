package ru.megateam.lab.service;

import ru.megateam.lab.domain.Instrument;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        Instrument inst = instruments.get(id);
        return inst != null ? inst.getName() : null;
    }

    public Long getId(String name) {
        if (name == null) return null;

        for (Map.Entry<Long, Instrument> entry : instruments.entrySet()) { //
            if (entry.getValue().getName().equalsIgnoreCase(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public List<Instrument> getAll() {
        return new ArrayList<>(instruments.values());
    }

    public void replaceAll(List<Instrument> newInstruments) { //удаляем все при перезаписи
        instruments.clear();
        for (Instrument inst : newInstruments) {
            instruments.put(inst.getId(), inst);
            if (inst.getId() >= nextId) {
                nextId = inst.getId() + 1;
            }
        }
    }

    public void clear() {
        instruments.clear();
        nextId = 1;
    }
}