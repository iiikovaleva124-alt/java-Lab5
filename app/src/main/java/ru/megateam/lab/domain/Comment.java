package ru.megateam.lab.domain;

import java.time.Instant;

public final class Comment {
    public final long id;
    public final String text;
    public final Instant createdAt;
    public final String ownerUsername;

    public Comment(long id, String text, Instant createdAt, String ownerUsername) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
        this.ownerUsername = ownerUsername != null ? ownerUsername : "SYSTEM";
    }
}
