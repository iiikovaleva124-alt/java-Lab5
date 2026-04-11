package ru.megateam.lab.repository;

import ru.megateam.lab.domain.Comment;
import ru.megateam.lab.domain.Incident;

import java.util.*;

public class InMemoryIncidentRepository implements IncidentRepository {

    private final Map<Long, Incident> storage = new HashMap<>(); //нам нужен hashmap
    //storage—HashMap, где ключ=Long (id инцидента), значение=объект Incident
    private long nextIncidentId = 1L;
    //счетчик айди

    private final Map<Long, List<Comment>> comments = new HashMap<>();
    private long nextCommentId = 1L;
    //метод добавляет ком к инциденту и возвращает id кома
    @Override
    public long addComment(long incidentId, Comment comment) {
        long id = nextCommentId++;
        comment.setId(id); //у переданного comment устанавливается поле id
        comments.computeIfAbsent(incidentId, k -> new ArrayList<>()).add(comment); //->- для параметра k выполнить...
        // computerIfAbsent ищет в карте comments ключ, если его нет, то создает arraylist
        return id;
    }
    // в скобках параметр - id инцидента, чьи комментарии мы хотим получить
    @Override
    public List<Comment> getComments(long incidentId) {

        return comments.getOrDefault(incidentId, List.of());
    }
    // list.of возвращает пустой список, а не null

    @Override
    public Incident add(Incident incident) {
        long id = nextIncidentId++; //берем текущее id
        incident.setId(id); //записывает id в инцидент
        storage.put(id, incident); // в хэшмап идет уже инцидент с id
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
        storage.put(incident.getId(), incident);
        return incident;
    }

    @Override
    public boolean remove(long id) {

        return storage.remove(id) != null;
    }
}
