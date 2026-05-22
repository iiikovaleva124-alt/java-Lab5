package ru.megateam.lab.domain;


public final class Sample {
    private long id;
    public String name;


    public Sample() {
        this.name = "";
    }

    public Sample(long id, String name) {
        this.id = id;
        setName(name);
    }


    public String getName() {
        return name;
    }
    public long getId() { return id; }

    // чтобы получить id из бд
    public void setId(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("Id of sample can not be negative");
        }
        this.id = id;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name of sample can not be empty");
        }
        if (name.length() > 128) {
            throw new IllegalArgumentException("Name of sample must be under 128 chars");
        }
        this.name = name.trim();
    }

}
