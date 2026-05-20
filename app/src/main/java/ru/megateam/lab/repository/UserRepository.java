package ru.megateam.lab.repository;

import ru.megateam.lab.domain.User;
import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findByLogin(String login);
    boolean existsByLogin(String login);
}