package ru.megateam.lab.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.megateam.lab.service.IncidentService;
import ru.megateam.lab.service.InstrumentService;
import ru.megateam.lab.service.SampleService;

import java.io.File;
import java.io.IOException;

public class JsonFileStorage implements FileStorage {

    private final ObjectMapper objectMapper;
    private final IncidentService incidentService;
    private final SampleService sampleService;
    private final InstrumentService instrumentService;


    public JsonFileStorage(IncidentService incidentService,
                           SampleService sampleService,
                           InstrumentService instrumentService) {
        this.incidentService = incidentService;
        this.sampleService = sampleService;
        this.instrumentService = instrumentService;
        this.objectMapper = new ObjectMapper(); //чтобы преобразовывать объекты в текст в файле и обратно
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); //для привычного вывода времени
    }

    @Override
    public void save(String path, AppState state) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter() //отступы и переносы строк в файле
                    .writeValue(new File(path), state); //создает файл по пути и записывает свойства как нужно
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения файла: " + e.getMessage(), e);
        }
    }

    @Override
    public AppState load(String path) {
        File file = new File(path);

        if (!file.exists()) {
            throw new RuntimeException("Файл не существует: " + path);
        }

        if (!file.isFile()) {
            throw new RuntimeException("Это не файл: " + path);
        }

        if (!file.canRead()) { //проверка прав на чтение
            throw new RuntimeException("Файл нельзя прочитать: " + path);
        }

        try {
            //загружаем данные из JSON автоматически через конструкторы
            AppState state = objectMapper.readValue(file, AppState.class);

            // восстанавливаем данные в сервисы
            if (incidentService != null && state.getIncidents() != null) {
                incidentService.replaceAll(state.getIncidents());
            }
            if (sampleService != null && state.getSamples() != null) {
                sampleService.replaceAll(state.getSamples());
            }
            if (instrumentService != null && state.getInstruments() != null) {
                instrumentService.replaceAll(state.getInstruments());
            }

            return state;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки файла: " + e.getMessage(), e);
        }
    }
    }

