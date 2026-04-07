package ru.megateam.lab.cli;

import ru.megateam.lab.cli.IncidentCli;
import ru.megateam.lab.repository.InMemoryIncidentRepository;
import ru.megateam.lab.service.IncidentService;
import ru.megateam.lab.service.SampleService;
import ru.megateam.lab.service.InstrumentService;

public class Main {
    public static void main(String[] args) {
        InMemoryIncidentRepository incidentRepository = new InMemoryIncidentRepository();

        SampleService sampleService = new SampleService();
        InstrumentService instrumentService = new InstrumentService();
        IncidentService incidentService = new IncidentService(
                incidentRepository, sampleService, instrumentService
        );

        IncidentCli cli = new IncidentCli(incidentService, sampleService, instrumentService);
        cli.run();
    }
}