package ru.megateam.lab.domain;

import java.time.Instant;

public final class Sample {
    public long id;
    public String name;
    public String type;
    public String location;
    public String status;
    public String ownerUsername;
    public Instant createdAt;
    public Instant updatedAt;

    public Sample(long id, String name, String type, String location, String status, String ownerUsername, Instant createdAt, Instant updatedAt) {
        this.id = id;
        setName(name);
        setType(type);
        setLocation(location);
        setStatus(status);
        setOwnerUsername(ownerUsername);
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public String getOwnerUsername() { return ownerUsername; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name of sample can not be empty");
        }
        if (name.length() > 128) {
            throw new IllegalArgumentException("Name of sample must be under 128 chars");
        }
        this.name = name.trim();
    }

    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type of sample can not be empty");
        }
        if (type.length() > 64) {
            throw new IllegalArgumentException("Type of sample must be under 64 chars");
        }
        this.type = type.trim();
    }

    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location of sample can not be empty");
        }
        if (location.length() > 64) {
            throw new IllegalArgumentException("Location of sample must be under 64 chars");
        }
        this.location = location.trim();
    }

    public void setStatus(String status) {
        if (this.status == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("status of sample can not be empty");
        }
        this.status = this.status.trim();
    }

    public void setOwnerUsername(String ownerUsername) {
        if (ownerUsername == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner Username can not be empty");
        }
        this.ownerUsername = ownerUsername.trim();
    }

    public void updatedAt() {
        this.updatedAt = Instant.now();
    }
}
