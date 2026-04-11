package ru.megateam.lab.domain;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class IncidentTest {

    @Test
    void testValidIncident() {
        Incident incident = new Incident(
                1L,
                "Test Title",
                "Test Description",
                IncidentSeverity.HIGH,
                IncidentStatus.NEW,
                0L, 0L,
                "SYSTEM",
                Instant.now(),
                Instant.now()
        );

        assertEquals("Test Title", incident.getTitle());
        assertEquals(IncidentSeverity.HIGH, incident.getSeverity());
    }

    @Test
    void testTitleTooLong() {
        String longTitle = "A".repeat(129);

        assertThrows(IllegalArgumentException.class, () -> {
            new Incident(1L, longTitle, "Desc", IncidentSeverity.LOW,
                    IncidentStatus.NEW, 0L, 0L, "SYSTEM",
                    Instant.now(), Instant.now());
        });
    }

    @Test
    void testEmptyTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Incident(1L, "", "Desc", IncidentSeverity.LOW,
                    IncidentStatus.NEW, 0L, 0L, "SYSTEM",
                    Instant.now(), Instant.now());
        });
    }

    @Test
    void testNegativeSampleId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Incident(1L, "Title", "Desc", IncidentSeverity.LOW,
                    IncidentStatus.NEW, -1L, 0L, "SYSTEM",
                    Instant.now(), Instant.now());
        });
    }
}