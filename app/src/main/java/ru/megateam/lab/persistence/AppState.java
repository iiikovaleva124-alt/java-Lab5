package ru.megateam.lab.persistence;

import ru.megateam.lab.domain.Comment;
import ru.megateam.lab.domain.Incident;
import ru.megateam.lab.domain.Sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppState {
    //поля, списки, потому что так удобнее хранить данные
    private List<Incident> incidents;
    private Map<Long, String> samples;
    private Map<Long, List<Comment>> comments;

    //пустой констурктор:
    public AppState() {
        this.incidents = new ArrayList<>();
        this.samples = new HashMap<>();
        this.comments = new HashMap<>();
    }

    //конструктор со списками, чтобы быстро собрать состояния из репозитория
    public AppState(List<Incident> incidents,
                    Map<Long, String> samples,
                    Map<Long, List<Comment>> comments) {
        this.incidents = incidents != null ? new ArrayList<>(incidents) : new ArrayList<>();
        this.samples = samples != null ? new HashMap<>(samples) : new HashMap<>();
        this.comments = comments != null ? copyCommentsMap(comments) : new HashMap<>();
    }

    public List<Incident> getIncidents() {
        return incidents;
    }

    public void setIncidents(List<Incident> incidents) {
        this.incidents = incidents != null ? new ArrayList<>(incidents) : new ArrayList<>();
    }

    public Map<Long, String> getSamples() {
        return samples;
    }

    public void setSamples(Map<Long, String> samples) {
        this.samples = samples != null ? new HashMap<>(samples) : new HashMap<>();
    }

    public Map<Long, List<Comment>> getComments() {
        return comments;
    }

    public void setComments(Map<Long, List<Comment>> comments) {
        this.comments = comments != null ? copyCommentsMap(comments) : new HashMap<>();
    }

    private Map<Long, List<Comment>> copyCommentsMap(Map<Long, List<Comment>> source) {
        Map<Long, List<Comment>> copy = new HashMap<>();

        for (Map.Entry<Long, List<Comment>> entry : source.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        return copy;
    }
}

