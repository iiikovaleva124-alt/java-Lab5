package ru.megateam.lab.domain;

import java.time.Instant;
public final class Incident {
// Уникальный номер инцидента. Программа назначает сама.
    public long id;
// Короткий заголовок (что случилось). Нельзя пустое. До 128 символов.
    public String title;
// Подробности (что, где, как). Можно пусто. До 1024 символов.
    public String description;
// Серьёзность: LOW, MEDIUM или HIGH.
    public IncidentSeverity severity;
// Статус: NEW, INVESTIGATING или CLOSED.
    public IncidentStatus status;
// Связанный образец (если относится к образцу). Может быть 0, если не связано.
    public long sampleId;
// Связанный прибор (если относится к прибору). Может быть 0, если не связано.
    public long instrumentId;
// Кто создал запись (логин). На ранних этапах можно "SYSTEM".
    public String ownerUsername;
// Когда создано. Программа ставит автоматически.
    public Instant createdAt;
// Когда обновляли. Программа обновляет автоматически.
    public Instant updatedAt;

    public Incident(long l, String title, String description, IncidentSeverity severity, IncidentStatus incidentStatus, long l1, long l2, String s, Instant now, Instant now1) {
    }
}

