package ru.megateam.lab.persistence;

public interface FileStorage {
    void save(String path, AppState state);
    AppState load(String path);
}
