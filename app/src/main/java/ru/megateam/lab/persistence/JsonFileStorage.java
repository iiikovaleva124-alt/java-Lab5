package ru.megateam.lab.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;

public class JsonFileStorage implements FileStorage {

    private final ObjectMapper objectMapper;


    public JsonFileStorage() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void save(String path, AppState state) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(path), state);
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

        if (!file.canRead()) {
            throw new RuntimeException("Файл нельзя прочитать: " + path);
        }

        try {
            return objectMapper.readValue(file, AppState.class);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки файла: " + e.getMessage(), e);
        }
    }
    }

