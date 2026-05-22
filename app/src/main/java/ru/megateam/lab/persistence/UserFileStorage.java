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

}