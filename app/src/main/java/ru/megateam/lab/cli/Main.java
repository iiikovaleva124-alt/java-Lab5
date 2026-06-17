package ru.megateam.lab.cli;

import ru.megateam.lab.cli.IncidentCli;
import ru.megateam.lab.domain.User;
import ru.megateam.lab.persistence.*;
import ru.megateam.lab.repository.*;
import ru.megateam.lab.service.IncidentService;
import ru.megateam.lab.service.*;

import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        InMemoryIncidentRepository incidentRepository = new InMemoryIncidentRepository();

        DbConnectionManager connectionManager = new DbConnectionManager();
        JdbcSampleRepository sampleRepository = new JdbcSampleRepository(connectionManager);
        JdbcInstrumentRepository instrumentRepository = new JdbcInstrumentRepository(connectionManager);

        SampleService sampleService = new SampleService(sampleRepository);
        InstrumentService instrumentService = new InstrumentService(instrumentRepository);
        FileValidator fileValidator = new FileValidator();
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        JsonUserStorage userStorage = new JsonUserStorage("users.json", userRepository);
        userStorage.load();
        UserService userService = new UserService(userRepository);
        IncidentService incidentService = new IncidentService(incidentRepository, sampleService, instrumentService, null, fileValidator, null);

        incidentService = new IncidentService(
                incidentRepository, sampleService, instrumentService, fileValidator, userService
        );



        FileService fileService = new FileService(
                incidentRepository, sampleService, instrumentService
        );

        IncidentCli cli = new IncidentCli(incidentService, sampleService, instrumentService, fileService, userService);
        cli.run();
    }
}