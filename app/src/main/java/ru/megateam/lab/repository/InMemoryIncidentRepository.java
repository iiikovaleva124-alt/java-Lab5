package ru.megateam.lab.repository;

import ru.megateam.lab.domain.Comment;
import ru.megateam.lab.domain.Incident;

import java.util.*;

public class InMemoryIncidentRepository implements IncidentRepository {
    private final Map<Long, Incident> storage = new TreeMap<>();

    private final Map<Long, List<Comment>> comments = new HashMap<>();

    public long addComment(long incidentId, Comment comment) {
        comments.computeIfAbsent(incidentId, k -> new ArrayList<>()).add(comment);
        return comment.id;
    }

    public List<Comment> getComments(long incidentId) {
        return comments.getOrDefault(incidentId, List.of());
    }

    @Override
    public Incident add(Incident incident) {
        storage.put(incident.id, incident);
        return incident;
    }

    @Override
    public Optional<Incident> getById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Incident> getAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Incident update(Incident incident) {
        storage.put(incident.id, incident);
        return incident;
    }

    @Override
    public boolean remove(long id) {
        return storage.remove(id) != null;
    }
}
