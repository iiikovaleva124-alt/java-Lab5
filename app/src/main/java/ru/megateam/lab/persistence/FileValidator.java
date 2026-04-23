package ru.megateam.lab.persistence;

import ru.megateam.lab.domain.Comment;
import ru.megateam.lab.domain.Incident;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileValidator {

    public List<String> validate(AppState state) {
        List<String> errors = new ArrayList<>();

        if (state == null) {
            errors.add("Файл пустой или не удалось прочитать данные");
            return errors;
        }

        if (state.getIncidents() == null) {
            errors.add("Отсутствует список incidents");
        }

        if (state.getSamples() == null) {
            errors.add("Отсутствует список samples");
        }

        if (state.getComments() == null) {
            errors.add("Отсутствует список comments");
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        validateIncidents(state.getIncidents(), errors);
        validateSamples(state.getSamples(), errors);
        validateComments(state.getComments(), errors);

        validateUniqueIncidentIds(state.getIncidents(), errors);
        validateUniqueSampleIds(state.getSamples(), errors);
        validateUniqueCommentIds(state.getComments(), errors);

        validateIncidentSampleLinks(state.getIncidents(), state.getSamples(), errors);

        return errors;
    }

    private void validateIncidents(List<Incident> incidents, List<String> errors) {
        for (Incident incident : incidents) {
            if (incident == null) {
                errors.add("Обнаружен null в списке incidents");
                continue;
            }

            if (incident.getId() <= 0) {
                errors.add("Некорректный incident id=" + incident.getId());
            }

            if (isBlank(incident.getTitle())) {
                errors.add("Пустое поле title у incident id=" + incident.getId());
            }

            if (isBlank(incident.getDescription())) {
                errors.add("Пустое поле description у incident id=" + incident.getId());
            }

            if (incident.getSeverity() == null) {
                errors.add("Пустое поле severity у incident id=" + incident.getId());
            }

            if (incident.getStatus() == null) {
                errors.add("Пустое поле status у incident id=" + incident.getId());
            }

            if (isBlank(incident.getOwnerUsername())) {
                errors.add("Пустое поле ownerUsername у incident id=" + incident.getId());
            }

            if (incident.getSampleId() <= 0) {
                errors.add("Некорректный sampleId у incident id=" + incident.getId());
            }

            if (incident.getInstrumentId() <= 0) {
                errors.add("Некорректный instrumentId у incident id=" + incident.getId());
            }

            if (incident.getCreatedAt() == null) {
                errors.add("Пустое поле createdAt у incident id=" + incident.getId());
            }

            if (incident.getUpdatedAt() == null) {
                errors.add("Пустое поле updatedAt у incident id=" + incident.getId());
            }
        }
    }

    private void validateSamples(List<Sample> samples, List<String> errors) {
        for (Sample sample : samples) {
            Long id = sample.getId();
            String name = sample.getName();

            if (id == null || id <= 0) {
                errors.add("Некорректный sample id=" + id);
            }

            if (isBlank(name)) {
                errors.add("Пустое имя sample id=" + id);
            }
        }
    }

    private void validateComments(Map<Long, List<Comment>> comments, List<String> errors) {
        for (Map.Entry<Long, List<Comment>> entry : comments.entrySet()) {
            Long incidentId = entry.getKey();
            List<Comment> commentList = entry.getValue();

            if (incidentId == null || incidentId <= 0) {
                errors.add("Некорректный incidentId в comments: " + incidentId);
            }

            if (commentList == null) {
                errors.add("Список comments равен null для incidentId=" + incidentId);
                continue;
            }

            for (Comment comment : commentList) {
                if (comment == null) {
                    errors.add("Обнаружен null comment у incidentId=" + incidentId);
                    continue;
                }

                if (comment.getId() <= 0) {
                    errors.add("Некорректный comment id=" + comment.getId());
                }

                if (isBlank(comment.getText())) {
                    errors.add("Пустое поле text у comment id=" + comment.getId());
                }

                if (isBlank(comment.getOwnerUsername())) {
                    errors.add("Пустое поле ownerUsername у comment id=" + comment.getId());
                }

                if (comment.getCreatedAt() == null) {
                    errors.add("Пустое поле createdAt у comment id=" + comment.getId());
                }
            }
        }
    }

    private void validateUniqueIncidentIds(List<Incident> incidents, List<String> errors) {
        Set<Long> ids = new HashSet<>();

        for (Incident incident : incidents) {
            if (incident != null && !ids.add(incident.getId())) {
                errors.add("Дублирующийся incident id=" + incident.getId());
            }
        }
    }

    private void validateUniqueSampleIds(Map<Long, String> samples, List<String> errors) {
        Set<Long> ids = new HashSet<>();

        for (Long id : samples.keySet()) {
            if (id != null && !ids.add(id)) {
                errors.add("Дублирующийся sample id=" + id);
            }
        }
    }

    private void validateUniqueCommentIds(Map<Long, List<Comment>> comments, List<String> errors) {
        Set<Long> ids = new HashSet<>();

        for (List<Comment> commentList : comments.values()) {
            if (commentList == null) {
                continue;
            }

            for (Comment comment : commentList) {
                if (comment != null && !ids.add(comment.getId())) {
                    errors.add("Дублирующийся comment id=" + comment.getId());
                }
            }
        }
    }

    private void validateIncidentSampleLinks(List<Incident> incidents, Map<Long, String> samples, List<String> errors) {
        Set<Long> sampleIds = samples.keySet();

        for (Incident incident : incidents) {
            if (incident != null && !sampleIds.contains(incident.getSampleId())) {
                errors.add("Incident id=" + incident.getId()
                        + " ссылается на несуществующий sampleId=" + incident.getSampleId());
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
