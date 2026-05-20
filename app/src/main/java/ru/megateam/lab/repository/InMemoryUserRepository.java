package ru.megateam.lab.repository;

import ru.megateam.lab.domain.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> users = new HashMap<>();

    @Override
    public void save(User user) {
        users.put(user.getLogin(), user);
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return Optional.ofNullable(users.get(login));
    }

    @Override
    public boolean existsByLogin(String login) {
        return users.containsKey(login);
    }
}