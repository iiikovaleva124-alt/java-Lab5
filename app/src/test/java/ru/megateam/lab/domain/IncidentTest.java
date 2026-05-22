package ru.megateam.lab.domain;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class IncidentTest {

    @Test
    //создается новый инциден для тестов
    void testValidIncident() {
        Incident incident = new Incident(
                1L,
                "Test Title",
                "Test Description",
                IncidentSeverity.HIGH,
                IncidentStatus.NEW,
                0L, 0L,
                1L,
                "SYSTEM",
                Instant.now(),
                Instant.now()
        );

        assertEquals("Test Title", incident.getTitle()); //метод из junit, сравнивает
        assertEquals(IncidentSeverity.HIGH, incident.getSeverity());
    }

    @Test
    void testTitleTooLong() {
        String longTitle = "T".repeat(130);

        assertThrows(IllegalArgumentException.class, () -> {
            new Incident(1L, longTitle, "Description", IncidentSeverity.LOW,
                    IncidentStatus.NEW, 0L, 0L, 1L, "SYSTEM",
                    Instant.now(), Instant.now());
        });
    }

    @Test
    void testEmptyTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Incident(1L, "", "Description", IncidentSeverity.LOW,
                    IncidentStatus.NEW, 0L, 0L, 1L, "SYSTEM",
                    Instant.now(), Instant.now());
        });
    }

    @Test
    void testNegativeSampleId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Incident(1L, "Title", "Description", IncidentSeverity.LOW,
                    IncidentStatus.NEW, -1L, 0L, 1L, "SYSTEM",
                    Instant.now(), Instant.now());
        });
    }
}