package ru.megateam.lab.domain;
import java.time.Instant;

public class IncidentCommet {
    // Уникальный номер комментария. Программа назначает сама.
    public long id;

    // К какому инциденту относится (id инцидента).
    // Должен ссылаться на реально существующий Incident.
    public long incidentId;

    // Текст комментария. Нельзя пустое. До 512 символов.
    public String text;

    // Кто оставил комментарий (логин). На ранних этапах можно "SYSTEM".
    public String ownerUsername;

    // Когда оставили комментарий. Программа ставит автоматически.
    public Instant createdAt;
}
