package ru.megateam.lab.repository;

import ru.megateam.lab.domain.Comment;
import ru.megateam.lab.domain.Incident;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public void replaceAll(List<Incident> incidents) {
        storage.clear(); //очищает карту

        long maxId = 0;
        for (Incident incident : incidents) { //берем по 1 инциденту из листа
            storage.put(incident.getId(), incident);

            if (incident.getId() > maxId) {
                maxId = incident.getId();
                //если текущий инцидент имеет id больше найденного максимума, то обновляем максимум
            }
        }

        nextIncidentId = maxId + 1;
    }

    @Override
    public Map<Long, List<Comment>> getAllCommentsMap() {
        Map<Long, List<Comment>> copy = new HashMap<>();

        for (Map.Entry<Long, List<Comment>> entry : comments.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        return copy;
    }

    @Override
    public void replaceAllComments(Map<Long, List<Comment>> newComments) {
        comments.clear();

        long maxCommentId = 0;

        for (Map.Entry<Long, List<Comment>> entry : newComments.entrySet()) {
            List<Comment> copiedList = new ArrayList<>(entry.getValue());
            comments.put(entry.getKey(), copiedList);

            for (Comment comment : copiedList) {
                if (comment.getId() > maxCommentId) {
                    maxCommentId = comment.getId();
                }
            }
        }

        nextCommentId = maxCommentId + 1;
    }
    public void deleteById(long id) {
        storage.remove(id);
    }

}
