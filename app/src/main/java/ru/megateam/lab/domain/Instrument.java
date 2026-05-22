package ru.megateam.lab.domain;

public class Instrument {
    private long id;
    private String name;

    public Instrument() {}

    public Instrument(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() { return id; }
    public void setId(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("Id of instrument can not be negative");
        }
        this.id = id;
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name of instrument can not be empty");
        }
        if (name.trim().length() > 128) {
            throw new IllegalArgumentException("Name of instrument must be under 128 chars");
        }
        this.name = name.trim();
    }

    @Override
    public String toString() {
        return "Instrument{id=" + id + ", name='" + name + "'}";
    }
}
