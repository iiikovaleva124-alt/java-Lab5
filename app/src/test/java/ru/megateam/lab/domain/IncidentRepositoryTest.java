package ru.megateam.lab.domain;

import ru.megateam.lab.domain.Incident;
import ru.megateam.lab.domain.IncidentSeverity;
import ru.megateam.lab.domain.IncidentStatus;
import ru.megateam.lab.persistence.DbConnectionManager;
import ru.megateam.lab.repository.IncidentRepository;
import ru.megateam.lab.repository.JdbcIncidentRepository;

import java.time.Instant;

public class IncidentRepositoryTest {
    public static void main(String[] args) {
        DbConnectionManager connectionManager = new DbConnectionManager();
        IncidentRepository repository = new JdbcIncidentRepository(connectionManager);

        Incident incident = new Incident();
        incident.setTitle("JDBC test incident");
        incident.setDescription("Created from IncidentRepositoryTestMain");
        incident.setSeverity(IncidentSeverity.MEDIUM);
        incident.setStatus(IncidentStatus.NEW);
        incident.setSampleId(1L);
        incident.setInstrumentId(1L);
        incident.setOwnerId(1);
        incident.setOwnerUsername("alice");
        incident.setCreatedAt(Instant.now());
        incident.setUpdatedAt(Instant.now());

        System.out.println("=== ADD ===");
        Incident saved = repository.add(incident);
        System.out.println("Saved id = " + saved.getId());

        System.out.println("\n=== GET ALL AFTER ADD ===");
        for (Incident i : repository.getAll()) {
            System.out.println(i);
            System.out.println("-------------------");
        }

        System.out.println("\n=== UPDATE ===");
        saved.setTitle("Updated JDBC incident");
        saved.setDescription("Updated description from test");
        saved.setStatus(IncidentStatus.NEW);
        repository.update(saved);

        repository.getById(saved.getId()).ifPresentOrElse(
                i -> {
                    System.out.println("Updated incident:");
                    System.out.println(i);
                },
                () -> System.out.println("Incident not found after update")
        );

        System.out.println("\n=== REMOVE ===");
        boolean removed = repository.remove(saved.getId());
        System.out.println("Removed = " + removed);

        System.out.println("\n=== GET BY ID AFTER REMOVE ===");
        System.out.println(repository.getById(saved.getId()));
    }
}
