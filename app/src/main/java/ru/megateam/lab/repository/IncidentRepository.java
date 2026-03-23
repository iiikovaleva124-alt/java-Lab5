package ru.megateam.lab.repository;

import ru.megateam.lab.domain.Comment;
import ru.megateam.lab.domain.Incident;
import java.util.List;
import java.util.Optional;

public interface IncidentRepository {
    Incident add(Incident incident);
    Optional<Incident> getById(long id); //optional потому что может не найти он может быть пустой
    List<Incident> getAll();
    Incident update(Incident incident);
    boolean remove(long id); //удалять по id
    long addComment(long incidentId, Comment comment);
    List<Comment> getComments(long incidentId);
}
