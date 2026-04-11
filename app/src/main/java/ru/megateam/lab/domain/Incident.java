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
    //сделать поля сделать private
    public Incident(long id, String title, String description,
                    IncidentSeverity severity, IncidentStatus status,
                    long sampleId, long instrumentId, String ownerUsername,
                    Instant createdAt, Instant updatedAt) {
        this.id = id;

        setTitle(title);
        setDescription(description);
        setSeverity(severity);
        setStatus(status);
        setSampleId(sampleId);
        setInstrumentId(instrumentId);
        setOwnerUsername(ownerUsername);

        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    //геттеры
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public IncidentSeverity getSeverity() { return severity; } //?
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public IncidentStatus getStatus() { return status; }
    public long getSampleId() { return sampleId; }
    public long getInstrumentId() { return instrumentId; }
    public String getOwnerUsername() { return ownerUsername; }

    public int maxTitleLength = 128;
    public int maxDescriptionLength = 1024;

     //сеттеры
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()){
            throw new IllegalArgumentException("Title can not be empty");
        }
        if (title.length() > maxTitleLength) {
            throw new IllegalArgumentException("Title must be under 128 chars");
        }
        this.title = title.trim();
        this.updatedAt = Instant.now();
    } //может добавить his.updatedAt = Instant.now(); - при изменении обновляем время


    public void setDescription(String description) {
                if (description != null && description.length() > maxDescriptionLength) {
            throw new IllegalArgumentException("Description must be under 1024 chars");
        }
        this.description = description;
        this.updatedAt = Instant.now();
    } // может добавить his.updatedAt = Instant.now(); - при изменении обновляем время

    public void setSeverity(IncidentSeverity severity) {
        if (severity == null) {
            throw new IllegalArgumentException("Severity cannot be null");
        }
        this.severity = severity;
        this.updatedAt = Instant.now();
    } //может добавить his.updatedAt = Instant.now(); - при изменении обновляем время

    public void setStatus(IncidentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status;
        this.updatedAt = Instant.now();
    } //может добавить his.updatedAt = Instant.now(); - при изменении обновляем время

    public void setOwnerUsername(String ownerUsername) {
        if (ownerUsername == null || ownerUsername.trim().isEmpty()) {
            this.ownerUsername = "SYSTEM";
        } else {
            this.ownerUsername = ownerUsername;
        }
        this.updatedAt = Instant.now();
    }
    public void setSampleId(long sampleId) {
        if (sampleId < 0) {
            throw new IllegalArgumentException("Sample ID must be > 0");
        }
        this.sampleId = sampleId;
        this.updatedAt = Instant.now();
    }
    public void setInstrumentId(long instrumentId) {
        if (instrumentId < 0) {
            throw new IllegalArgumentException("Instrument ID must be > 0");
        }
        this.instrumentId = instrumentId;
        this.updatedAt = Instant.now();
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCreatedAt(Instant createdAt)       {
        this.createdAt = createdAt; }

    public void setUpdatedAt(Instant updatedAt)       {
        this.updatedAt = updatedAt; }

    public void updatedAt() {

        this.updatedAt = Instant.now();
        //выдает 2026-04-10T10:42:48.399476Z вот в таком формате
        // Z = UTC
        //.399476 = микросекунды (доли секунды)
    }

    @Override // для нормального ввода
    public String toString() {
        return "Incident #" + id
                + "\n  severity: " + severity
                + "\n  status: " + status
                + "\n  title: " + title
                + "\n  description: " + (description == null ? "" : description) //может удалить
                + "\n  sampleId: " + sampleId
                + "\n  instrumentId: " + instrumentId
                + "\n  owner: " + ownerUsername
                + "\n  created: " + createdAt
                + "\n  updated: " + updatedAt;
    }

}

