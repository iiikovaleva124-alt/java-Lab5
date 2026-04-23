package ru.megateam.lab.persistence;

import ru.megateam.lab.domain.Sample;
import ru.megateam.lab.repository.IncidentRepository;
import ru.megateam.lab.service.InstrumentService;
import ru.megateam.lab.service.SampleService;

import java.util.List;
import java.util.Map;

public class FileService {
    private final IncidentRepository incidentRepository;
    private final SampleService sampleService;
    private final InstrumentService instrumentService;
    private final FileStorage fileStorage;
    private final FileValidator fileValidator;

    public FileService(IncidentRepository incidentRepository,
                       SampleService sampleService,
                       InstrumentService instrumentService,
                       FileStorage fileStorage,
                       FileValidator fileValidator) {
        this.incidentRepository = incidentRepository;
        this.sampleService = sampleService;
        this.instrumentService = instrumentService;
        this.fileStorage = fileStorage;
        this.fileValidator = fileValidator;
    }

    public void save(String path) {
        AppState state = new AppState(
                incidentRepository.getAll(),
                sampleService.getAll(),
                instrumentService.getAll(),
                incidentRepository.getAllCommentsMap()
        );

        fileStorage.save(path, state);
    }

    public void load(String path) {
        AppState loaded = fileStorage.load(path);

        List<String> errors = fileValidator.validate(loaded);

        if (!errors.isEmpty()) {
            String message = String.join("; ", errors);
            throw new IllegalArgumentException("Файл невалиден: " + message);
        }

        incidentRepository.replaceAll(loaded.getIncidents());
        sampleService.replaceAll(loaded.getSamples());
        instrumentService.replaceAll(loaded.getInstruments());
        incidentRepository.replaceAllComments(loaded.getComments());
    }
}