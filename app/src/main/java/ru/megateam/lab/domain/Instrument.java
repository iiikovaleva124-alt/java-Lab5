package ru.megateam.lab.domain;

public class Instrument {
    private final long id;
    private final String name;

    public Instrument() {
        this.id = 0;
        this.name = "";
    }

    public Instrument(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() { return id; }
    public String getName() { return name; }
}