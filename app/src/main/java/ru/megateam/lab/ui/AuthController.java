package ru.megateam.lab.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.megateam.lab.service.UserService;
import ru.megateam.lab.persistence.JsonUserStorage;

public class AuthController {

    @FXML private Label statusLabel;
    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;

    private UserService userService;
    private JsonUserStorage userStorage;
    private Runnable onAuthSuccess;  // Коллбэк для перехода к основному окну

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setUserStorage(JsonUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void setOnAuthSuccess(Runnable onAuthSuccess) {
        this.onAuthSuccess = onAuthSuccess;
    }

    @FXML
    public void initialize() {
        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> handleRegister());

        passwordField.setOnAction(e -> handleLogin());

        //очистка ошибок при вводе
        loginField.textProperty().addListener((obs, old, val) -> clearError());
        passwordField.textProperty().addListener((obs, old, val) -> clearError());
    }

    private void handleLogin() {
        clearError();

        String login = loginField.getText().trim();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showError("Please enter login and password");
            return;
        }

        if (userService.login(login, password)) {
            onAuthSuccess();
        } else {
            showError("Invalid login or password");
        }
    }

    private void handleRegister() {
        clearError();

        String login = loginField.getText().trim();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showError("Please enter login and password");
            return;
        }

        if (password.length() < 4) {
            showError("Password must be at least 4 characters");
            return;
        }

        try {
            if (userService.register(login, password)) {
                statusLabel.setText("Registered! Please login");
                passwordField.clear();
                showError(""); //очищаем ошибку
            } else {
                showError("User with login '" + login + "' already exists");
            }
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void clearError() {
        errorLabel.setText("");
    }

    private void onAuthSuccess() {
        if (userStorage != null) {
            userStorage.save(); //сохраняем пользователя при успешной авторитизации
        }

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close(); //закрываем окно авторизации

        if (onAuthSuccess != null) {
            onAuthSuccess.run(); //запускаем основное окно
        }
    }
}