package ru.megateam.lab.service;

import ru.megateam.lab.domain.User;
import ru.megateam.lab.persistence.JsonUserStorage;
import ru.megateam.lab.repository.UserRepository;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;
    private final JsonUserStorage userStorage;
    private User currentUser; // текущий авторизованный пользователь

    public UserService(UserRepository userRepository, JsonUserStorage userStorage) {
        this.userRepository = userRepository;
        this.userStorage = userStorage;
    }

    public boolean register(String login, String password) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Login cannot be empty");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }
        if (userRepository.existsByLogin(login)) {
            return false;
        }

        User user = new User(login, password);
        userRepository.save(user);

        if (userStorage != null) {
            userStorage.save();
        }

        return true;
    }

    public boolean login(String login, String password) {
        Optional<User> userOpt = userRepository.findByLogin(login);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.checkPassword(password)) {
                currentUser = user;
                return true;
            }
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("No user logged in");
        }
        return currentUser;
    }

    public void requireAuth() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Authentication required. Please login first.");
        }
    }
}