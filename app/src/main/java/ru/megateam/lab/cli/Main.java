package ru.megateam.lab.cli;

import ru.megateam.lab.cli.IncidentCli;
import ru.megateam.lab.repository.InMemoryIncidentRepository;
import ru.megateam.lab.service.IncidentService;
import ru.megateam.lab.service.SampleService;
import ru.megateam.lab.service.InstrumentService;
import ru.megateam.lab.persistence.FileStorage;
import ru.megateam.lab.persistence.FileValidator;
import ru.megateam.lab.persistence.JsonFileStorage;
import ru.megateam.lab.persistence.FileService;

public class Main {
    public static void main(String[] args) {
        InMemoryIncidentRepository incidentRepository = new InMemoryIncidentRepository();

        SampleService sampleService = new SampleService();
        InstrumentService instrumentService = new InstrumentService();
        FileStorage fileStorage = new JsonFileStorage();
        FileValidator fileValidator = new FileValidator();
        IncidentService incidentService = new IncidentService(
                incidentRepository, sampleService, instrumentService, fileStorage, fileValidator
        );

        FileService fileService = new FileService(
                incidentRepository, sampleService, instrumentService, fileStorage, fileValidator
        );

        IncidentCli cli = new IncidentCli(incidentService, sampleService, instrumentService, fileService);
        cli.run();
    }
}