package ru.megateam.lab.domain;

import java.time.Instant;
public final class Incident {
    private long id;
    public String title;
    public String description;
    public IncidentSeverity severity;
    public IncidentStatus status;
    public long sampleId;
    public long instrumentId;
    public String ownerUsername;
    public Instant createdAt;
    public Instant updatedAt;
    private IncidentSeverity severity;
    private IncidentStatus status;

    public Incident(long id, String title, String description, IncidentSeverity severity, IncidentStatus status, long sampleId, long instrumentId, String ownerUsername, Instant createdAt, Instant updatedAt){
        this.id = id;
        setTitle(title);
        setDescription(description);
        setSeverity(severity);
        setStatus(status);
        this.sampleId = sampleId;
        this.instrumentId = instrumentId;
        setOwnerUsername(ownerUsername);
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? createdAt : Instant.now();
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public IncidentSeverity getSeverity() { return severity; }
    public IncidentStatus getStatus() { return status; }
    public long getSampleId() { return sampleId; }
    public long getInstrumentId() { return instrumentId; }
    public String getOwnerUsername() { return ownerUsername; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()){
            throw new IllegalArgumentException("Title can not be empty");
        }
        if (title.length() > 128) {
            throw new IllegalArgumentException("Title must be under 128 chars");
        }
        this.title = title.trim();
    }

    public void setDescription(String description) {
        if (description != null && description.length() > 1024) {
            throw new IllegalArgumentException("Description must be under 1024 chars");
        }
        this.description = description;
    }

    public void setSeverity(IncidentSeverity severity) {
        if (severity == null) {
            throw new IllegalArgumentException("Severity cannot be null");
        }
        this.severity = severity;
    }

    public void setStatus(IncidentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status;
    }

    public void setOwnerUsername(String ownerUsername) {
        if (ownerUsername == null || ownerUsername.trim().isEmpty()) {
            this.ownerUsername = "SYSTEM";
        } else {
            this.ownerUsername = ownerUsername;
        }

        /// вот сюда нужны сеттеры с проверкой для status, severity, owner

    public void updatedAt() {
        this.updatedAt = Instant.now();
    }
}

