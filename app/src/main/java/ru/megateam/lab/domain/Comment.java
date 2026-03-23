package ru.megateam.lab.domain;

import java.time.Instant;

public final class Comment {
    private long id;
    private String text;
    private Instant createdAt;
    private String ownerUsername;
    private long incidentId;

    //заменить на private
    public Comment(long id, String text, String ownerUsername) {
        this.id = id;
        this.text = text;
        this.createdAt = Instant.now();
        this.ownerUsername = ownerUsername != null ? ownerUsername : "SYSTEM";
    }
    //нужны ли тут геттеры и сеттеры?

    public long getId()              { return id; }
    public long getIncidentId()      { return incidentId; }
    public String getText()          { return text; }
    public String getOwnerUsername()  { return ownerUsername; }
    public Instant getCreatedAt()    { return createdAt; }

    public void setId(long id) {
        this.id = id;
        // вызывается репозиторием при добавлении
    }

    public void setIncidentId(long incidentId) {
        this.incidentId = incidentId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Comment №" + id
                + "  [" + createdAt + "]  " + text;
    }

}
