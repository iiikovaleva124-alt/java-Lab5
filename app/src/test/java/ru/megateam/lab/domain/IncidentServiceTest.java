package ru.megateam.lab.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.megateam.lab.service.*;
import ru.megateam.lab.repository.InMemoryIncidentRepository;
import ru.megateam.lab.persistence.*;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class IncidentServiceTest {

    private IncidentService incidentService;
    private SampleService sampleService;
    private InstrumentService instrumentService;

    @BeforeEach
    void setUp() {
        InMemoryIncidentRepository repository = new InMemoryIncidentRepository();
        FileValidator fileValidator = new FileValidator();
        incidentService = new IncidentService(repository, sampleService, instrumentService, null, fileValidator);
        sampleService = new SampleService();
        instrumentService = new InstrumentService();
        FileStorage fileStorage = new JsonFileStorage(incidentService, sampleService, instrumentService);

        incidentService = new IncidentService(
                repository,
                sampleService,
                instrumentService,
                fileStorage,
                fileValidator
        );
    }

    @Test
    void testAddIncident() {
        Incident incident = incidentService.add(
                "Test Incident",
                IncidentSeverity.HIGH,
                "Test Description",
                "USER"
        );

        assertNotNull(incident);
        assertEquals("Test Incident", incident.getTitle());
        assertEquals(1L, incident.getId());
    }

    @Test
    void testGetByIdFound() {
        Incident incident = incidentService.add(
                "Test", IncidentSeverity.LOW, "Desc", "USER"
        );

        Optional<Incident> found = incidentService.getById(incident.getId());

        assertTrue(found.isPresent());
        assertEquals("Test", found.get().getTitle());
    }

    @Test
    void testGetByIdNotFound() {
        Optional<Incident> found = incidentService.getById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void testCloseIncident() {
        Incident incident = incidentService.add(
                "Test", IncidentSeverity.LOW, "Desc", "USER"
        );


        Optional<Incident> closed = incidentService.close(incident.getId());

        assertTrue(closed.isPresent());

        Optional<Incident> updated = incidentService.getById(incident.getId());
        assertEquals(IncidentStatus.CLOSED, updated.get().getStatus());
    }

    @Test
    void testUpdateField() {
        Incident incident = incidentService.add(
                "Test", IncidentSeverity.LOW, "Desc", "USER"
        );

        Optional<Incident> updated = incidentService.update(
                incident.getId(), "title", "New Title"
        );

        assertTrue(updated.isPresent());
        assertEquals("New Title", updated.get().getTitle());
    }
}