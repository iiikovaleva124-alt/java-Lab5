package ru.megateam.lab.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ru.megateam.lab.domain.User;
import ru.megateam.lab.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class UserFileStorage {
    private final ObjectMapper objectMapper;
    private final String filePath;
    private final UserRepository userRepository;

    public UserFileStorage(String filePath, UserRepository userRepository) {
        this.filePath = filePath;
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void loadUsers() {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("User file does not exist. Will create on first registration.");
            return;
        }

        try {
            List<User> users = objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));

            for (User user : users) {
                userRepository.save(user);
            }
            System.out.println("Loaded " + users.size() + " users from file.");
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }
    
    public void saveUsers() {
        try {
            // Получаем всех пользователей (это сложно, нужен метод в репозитории)
            // Для простоты сохраняем при каждой регистрации/изменении
            // В реальной системе лучше использовать кэш
        } catch (Exception e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
}