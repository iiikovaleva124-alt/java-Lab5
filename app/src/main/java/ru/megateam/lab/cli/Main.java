package ru.megateam.lab.cli;

import ru.megateam.lab.cli.IncidentCli;
import ru.megateam.lab.domain.User;
import ru.megateam.lab.persistence.*;
import ru.megateam.lab.repository.InMemoryIncidentRepository;
import ru.megateam.lab.repository.InMemoryUserRepository;
import ru.megateam.lab.repository.UserRepository;
import ru.megateam.lab.service.IncidentService;
import ru.megateam.lab.service.SampleService;
import ru.megateam.lab.service.InstrumentService;
import ru.megateam.lab.service.UserService;

import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        InMemoryIncidentRepository incidentRepository = new InMemoryIncidentRepository();

        SampleService sampleService = new SampleService();
        InstrumentService instrumentService = new InstrumentService();
        FileValidator fileValidator = new FileValidator();
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        JsonUserStorage userStorage = new JsonUserStorage("users.json", userRepository);
        userStorage.load();
        UserService userService = new UserService(userRepository, userStorage);
        IncidentService incidentService = new IncidentService(incidentRepository, sampleService, instrumentService, null, fileValidator, null);
        FileStorage fileStorage = new JsonFileStorage(incidentService, sampleService, instrumentService);

        incidentService = new IncidentService(
                incidentRepository, sampleService, instrumentService, fileStorage, fileValidator, userService
        );



        FileService fileService = new FileService(
                incidentRepository, sampleService, instrumentService, fileStorage, fileValidator
        );

        IncidentCli cli = new IncidentCli(incidentService, sampleService, instrumentService, fileService, userService);
        cli.run();
    }
}