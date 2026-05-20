package ru.megateam.lab.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.megateam.lab.domain.User;
import ru.megateam.lab.repository.InMemoryUserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class JsonUserStorage {

    private final ObjectMapper objectMapper;
    private final String filePath;
    private final InMemoryUserRepository userRepository;

    public JsonUserStorage(String filePath, InMemoryUserRepository userRepository) {
        this.filePath = filePath; //путь к файлу пользователей
        this.userRepository = userRepository; //репозиторий для сохранения загруженных пользователей
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // для Instant
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // читаемые даты
    }

    public void load() {
        File file = new File(filePath);

        if (!file.exists()) { //если файла нет, то создаем при первой регистрации
            System.out.println("User file does not exist: " + filePath + " (will create on first registration)");
            return;
        }

        try {
            List<User> users = objectMapper.readValue( //readValue читает json и преобразует в java объект
                    file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, User.class)
            ); //objectMapper.getTypeFactory() - возвращает тип объекта - список пользователей
            //constructCollectionType(List.class, User.class) - создает список пользователей list<user>. коллекция типа лист, состоящая из пользователей

            for (User user : users) {
                userRepository.save(user);
            }

            System.out.println("Loaded " + users.size() + " users from " + filePath);

        } catch (IOException e) {
            System.err.println("Error loading users from " + filePath + ": " + e.getMessage());
            System.err.println("Starting with empty user list.");
        }
    }

    public void save() {
        try {
            List<User> users = userRepository.getAll(); //получаем всех пользователей из репозитория

            objectMapper.writerWithDefaultPrettyPrinter() //чтобы были переносы строк
                    .writeValue(new File(filePath), users); //из java объектов записывает в json

            System.out.println("Saved " + users.size() + " users to " + filePath);

        } catch (IOException e) {
            System.err.println("Error saving users to " + filePath + ": " + e.getMessage());
            //не выбрасываем исключение, сохранение не удалось, но приложение работает
        }
    }
}