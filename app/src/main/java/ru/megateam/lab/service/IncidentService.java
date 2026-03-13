package ru.megateam.lab.service;

import ru.megateam.lab.domain.*;
import ru.megateam.lab.repository.IncidentRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;


public class IncidentService {
    private final IncidentRepository repository;
    private long nextId = 1;

    public IncidentService(IncidentRepository repository) {
        this.repository = repository;
    }

    public Incident add(String title, IncidentSeverity severity, String description, String owner) {
        if (severity == null) severity = IncidentSeverity.LOW;
        Incident incident = new Incident(nextId++, title, description, severity,
                IncidentStatus.NEW, 0L, 0L, owner != null ? owner : "SYSTEM",
                Instant.now(), Instant.now());
        return repository.add(incident);
    }

    public List<Incident> list(Optional<IncidentStatus> status, Optional<Integer> lastN) {
        List<Incident> all = repository.getAll();
        List<Incident> filtered = all;

        if (status.isPresent()) {
            filtered = filtered.stream()
                    .filter(i -> i.status == status.get())
                    .collect(Collectors.toList());
        }

        if (lastN.isPresent()) {
            filtered = filtered.stream()
                    .sorted((a, b) -> Long.compare(b.id, a.id))
                    .limit(lastN.get())
                    .collect(Collectors.toList());
        }

        return filtered;
    }

    public Optional<Incident> getById(long id) {
        return repository.getById(id);
    }

    public Optional<Incident> update(long id, String field, String value) {
        return repository.getById(id).map(incident -> {
            if (incident.getStatus() == IncidentStatus.CLOSED) {
                throw new IllegalArgumentException("You can not update closed incident");
            }
            Incident updated = switch (field.toLowerCase()) {
                case "title" -> incident.setTitle(value);
                case "description" -> incident.setDescription(value);
                case "severity" -> incident.setSeverity(IncidentSeverity.valueOf(value.toUpperCase()));
                case "status" -> {
                    IncidentStatus newStatus = IncidentStatus.valueOf(value.toUpperCase());
                    if (incident.getStatus() == IncidentStatus.CLOSED) {
                        throw new IllegalArgumentException("You can not change status in closed incident");
                    }
                    incident.setStatus(newStatus);
                }
                throw new IllegalArgumentException("Unknown field");


            };
            incident.updatedAt();
            return repository.update(incident);
        });
    }

    public boolean linkSample(long incidentId, long sampleId) {
        return repository.getById(incidentId).map(inc -> {
            inc.setSampleId(sampleId);
            Instant updatedAt = inc.updatedAt;
            repository.update(inc);
            return true;
        }).orElse(false);
    }

    public boolean linkInstrument(long incidentId, long instrumentId) {
        return repository.getById(incidentId).map(inc -> {
            inc.setInstrumentId(instrumentId);
            inc.updatedAt();
            repository.update(inc);
            return true;
        }).orElse(false);
    }

    public boolean close(long id) {
        return update(id, "status", "CLOSED").isPresent();
    }

    public Map<IncidentSeverity, Long> report(LocalDate from, LocalDate to) {
        Instant fromInstant = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toInstant = to.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        return repository.getAll().stream()
                .filter(inc -> inc.createdAt != null &&  // ← ДОБАВЬ ЭТО
                        inc.createdAt.isAfter(fromInstant) &&
                        inc.createdAt.isBefore(toInstant))
                .collect(Collectors.groupingBy(inc -> inc.severity, Collectors.counting()));
    }

    private long nextCommentId = 1L;

    public long addComment(long incidentId, String text, String owner) {
        if (!repository.getById(incidentId).isPresent()) {
            throw new IllegalArgumentException("Incident not found");
        }
        if (text.trim().isEmpty() || text.length() > 512) {
            throw new IllegalArgumentException("Comment 1-512 chars");
        }

        Comment comment = new Comment(nextCommentId++, text, Instant.now(), owner);
        return repository.addComment(incidentId, comment);
    }

    public List<Comment> getComments(long incidentId) {
        return repository.getComments(incidentId);
    }
}