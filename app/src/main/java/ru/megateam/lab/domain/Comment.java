package ru.megateam.lab.domain;

import java.time.Instant;

public final class Comment {
    private long id;
    private String text;
    private Instant createdAt;
    private String ownerUsername;
    private long incidentId; // вроде привязывается в образце, а не наоборот. убрать

    //заменить на private
    public Comment(long id, String text, Instant createdAt, String ownerUsername) {
        this.id = id;
        this.text = text;
        this.createdAt = Instant.now();
        this.ownerUsername = ownerUsername != null ? ownerUsername : "SYSTEM";
    }
    //нужны ли тут геттеры и сеттеры?

    public static int maxCommentLength = 512;
    //геттеры
    public long getId() { return id; }
    public String getText() { return text; }
    public Instant getCreatedAt() { return createdAt; }
    public String getOwnerUsername() { return ownerUsername; }

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
        this.ownerUsername = ownerUsername != null ? ownerUsername : "SYSTEM";
    }

    @Override
    public String toString() {
        return "Comment №" + id
                + "  [" + createdAt + "]  " + text;
    }

}
