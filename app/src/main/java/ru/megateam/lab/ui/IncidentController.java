package ru.megateam.lab.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.megateam.lab.domain.*;
import ru.megateam.lab.persistence.*;
import ru.megateam.lab.service.IncidentService;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import ru.megateam.lab.service.InstrumentService;
import ru.megateam.lab.service.SampleService;
import ru.megateam.lab.service.UserService;

import java.util.Map;
import java.util.Objects;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class IncidentController {

    //таблица для инцидентов
    @FXML private TableView<Incident> incidentTable; //добавить колонки на описание sample и инструмент - по названию
    @FXML private TableColumn<Incident, Long> idColumn;
    @FXML private TableColumn<Incident, String> titleColumn;
    @FXML private TableColumn<Incident, IncidentSeverity> severityColumn; //окрасить
    @FXML private TableColumn<Incident, IncidentStatus> statusColumn; // окрасить
    @FXML private TableColumn<Incident, String> ownerColumn;
    @FXML private TableColumn<Incident, String> descriptionColumn;
    @FXML private TableColumn<Incident, String> sampleColumn;
    @FXML private TableColumn<Incident, String> instrumentColumn;
    @FXML private Button logoutButton;


    //доп 3
    //последняя папка
    //сохранить и сохранить как

    //кнопки на панели
    @FXML private Button refreshButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button addSampleButton;
    @FXML private Button addInstrumentButton;
    @FXML private Button saveButton;
    @FXML private Button loadButton;
    @FXML private Button saveAsButton;

    //таблица для образцов
    @FXML private TableView<Sample> sampleTable;
    @FXML private TableColumn<Sample, Long> sampleIdColumn;
    @FXML private TableColumn<Sample, String> sampleNameColumn;

    //таблица для инструментов
    @FXML private TableView<Instrument> instrumentTable;
    @FXML private TableColumn<Instrument, Long> instrumentIdColumn;
    @FXML private TableColumn<Instrument, String> instrumentNameColumn;

    //передает слушателям когда списки меняются
    private ObservableList<Incident> incidentList;
    private ObservableList<Sample> sampleList;
    private ObservableList<Instrument> instrumentList;

    private IncidentService incidentService;
    private FileStorage fileStorage;
    private SampleService sampleService;
    private InstrumentService instrumentService;
    private UserService userService;
    private JsonUserStorage userStorage;

    private String currentFilePath;
    private boolean hasUnsavedChanges = false;

    @FXML
    public void initialize() {
        //инициализация колонок таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id")); //размещает значения по столбцам
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        severityColumn.setCellValueFactory(new PropertyValueFactory<>("severity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("ownerUsername"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        sampleColumn.setCellValueFactory(cellData -> {
            long sampleId = cellData.getValue().getSampleId();

            if (sampleId == 0) {
                return new javafx.beans.property.SimpleStringProperty("No sample");
            }
            Sample sample = sampleService.getById(sampleId);
            if (sample != null) {
                return new javafx.beans.property.SimpleStringProperty(sample.getName());
            }
            return new javafx.beans.property.SimpleStringProperty("Unknown");
        });
        instrumentColumn.setCellValueFactory(cellData -> {
            long instrId = cellData.getValue().getInstrumentId();

            if (instrId == 0) {
                return new javafx.beans.property.SimpleStringProperty("No instrument");
            }
            Instrument instrument = instrumentService.getById(instrId);
            if (instrument != null) {
                return new SimpleStringProperty(instrument.getName());
            }
            return new javafx.beans.property.SimpleStringProperty("Unknown");
        });

        //наблюдаемый список
        incidentList = FXCollections.observableArrayList();
        incidentTable.setItems(incidentList);

        sampleIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        sampleNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        sampleList = FXCollections.observableArrayList();
        sampleTable.setItems(sampleList);

        instrumentIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        instrumentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        instrumentList = FXCollections.observableArrayList();
        instrumentTable.setItems(instrumentList);

        //что делают при нажатии
        refreshButton.setOnAction(e -> handleRefresh());
        addButton.setOnAction(e -> handleAdd());
        editButton.setOnAction(e -> handleEdit());
        deleteButton.setOnAction(e -> handleDelete());
        saveButton.setOnAction(e -> handleSave());
        saveAsButton.setOnAction(e -> handleSaveAs());
        loadButton.setOnAction(e -> handleLoad());
        addSampleButton.setOnAction(e -> handleAddSample());
        addInstrumentButton.setOnAction(e -> handleAddInstrument());
        logoutButton.setOnAction(e -> handleLogout());
        updateAuthUI();
    }

    void handleRefresh() { //обновление таблицы
        new Thread(() -> { //отдельный поток для выполнения, чтобы не блокать во время загрузки данных
            try {
                List<Incident> incidents = incidentService.getAllIncidents();//все инциденты
                List<Sample> samples = sampleService.getAll();
                List<Instrument> instruments = instrumentService.getAll();

                Platform.runLater(() -> { //код выполняется в ui, требование javafx
                    setAllIncidents(incidents); //передает данные в таблицу
                    setAllSamples(samples);
                    setAllInstruments(instruments);
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        showError("Error loading incidents", e.getMessage())
                );
            }
        }).start(); //запуск этого потока
    }


    private void saveToFile(String path) {
        // получаем данные из сервисов
        List<Incident> incidents = incidentService.getAllIncidents();
        List<Sample> samples = sampleService.getAll();
        List<Instrument> instruments = instrumentService.getAll();
        Map<Long, List<Comment>> comments = incidentService.getAllComments();

        AppState state = new AppState(incidents, samples, instruments, comments);

        fileStorage.save(path, state); // сохраняем через fileStorage

        String fileName = new File(path).getName();
        showInfo("Success", "Data saved to " + fileName);
    }

    private void handleLogout() {
        if (userService != null) {
            userService.logout();
        }

        if (userStorage != null) {
            userStorage.save();
        }

        showAuthWindow();

        Stage stage = (Stage) incidentTable.getScene().getWindow();
        stage.close();
    }

    public void updateAuthUI() {
        if (userService != null && userService.isLoggedIn()) {
            logoutButton.setVisible(true);
            logoutButton.setDisable(false);

            Stage stage = (Stage) incidentTable.getScene().getWindow();
            if (stage != null) {
                stage.setTitle("Incident Management System - " +
                        userService.getCurrentUser().getLogin());
            }
        } else {
            logoutButton.setVisible(false);
            logoutButton.setDisable(true);
        }
    }

    private void showAuthWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ru/megateam/lab/ui/AuthView.fxml")
            );
            loader.setControllerFactory(clazz -> {
                if (clazz == AuthController.class) {
                    return new AuthController();
                }
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            javafx.scene.Parent root = loader.load();
            AuthController authController = loader.getController();
            authController.setUserService(userService);
            authController.setUserStorage(userStorage);

            //после успешной авторизации — перезагрузить главное окно
            authController.setOnAuthSuccess(() -> {
                System.out.println("User logged in: " + userService.getCurrentUser().getLogin());
            });

            Stage authStage = new Stage();
            authStage.setTitle("Login");
            authStage.setScene(new Scene(root, 400, 350));
            authStage.setResizable(false);
            authStage.showAndWait();  // Ждём пока пользователь авторизуется

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error opening login window", e.getMessage());
        }
    }

    boolean showAuthDialog() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Login");
        dialog.setHeaderText("Please login or register");
        dialog.setResizable(true);

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, registerButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField loginField = new TextField();
        loginField.setPromptText("Login");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        grid.add(new Label("Login:"), 0, 0);
        grid.add(loginField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(errorLabel, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        loginField.textProperty().addListener((obs, old, val) ->
                loginButton.setDisable(val.trim().isEmpty() || passwordField.getText().isEmpty()));
        passwordField.textProperty().addListener((obs, old, val) ->
                loginButton.setDisable(loginField.getText().trim().isEmpty() || val.isEmpty()));

        passwordField.setOnAction(e -> {
            if (!loginButton.isDisabled()) {
                dialog.setResult(true);
                dialog.close();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                String login = loginField.getText().trim();
                String password = passwordField.getText();

                if (userService.login(login, password)) {
                    if (userStorage != null) userStorage.save();
                    return true;
                } else {
                    errorLabel.setText("Invalid login or password");
                    return null;
                }
            } else if (dialogButton == registerButtonType) {
                String login = loginField.getText().trim();
                String password = passwordField.getText();

                if (login.isEmpty() || password.isEmpty()) {
                    errorLabel.setText("Please enter login and password");
                    return null;
                }
                if (password.length() < 4) {
                    errorLabel.setText("Password must be at least 4 characters");
                    return null;
                }

                try {
                    if (userService.register(login, password)) {
                        if (userStorage != null) userStorage.save();
                        errorLabel.setText("Registered! Please login");
                        return null;
                    } else {
                        errorLabel.setText("User already exists");
                        return null;
                    }
                } catch (IllegalArgumentException e) {
                    errorLabel.setText(e.getMessage());
                    return null;
                }
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();
        return result.isPresent() && result.get();
    }

    private void handleAdd() {

        if (userService == null || !userService.isLoggedIn()) {
            showError("Authentication Required",
                    "Please login first to add incidents");
            return;
        }

        Optional<Incident> result = showIncidentDialog(null); //диалог в режиме создания
        result.ifPresent(incident -> { //если не отмена
            try {
                incidentService.add(
                        incident.getTitle(),
                        incident.getSeverity(),
                        incident.getDescription(),
                        userService.getCurrentUser().getLogin(),
                        incident.getSampleId(),
                        incident.getInstrumentId()
                );
                handleRefresh();
                showInfo("Success", "Incident added successfully");
            } catch (Exception e) {
                showError("Error adding incident", e.getMessage());
            }
        });
    }

    private void handleEdit() {

        if (userService == null || !userService.isLoggedIn()) {
            showError("Authentication Required",
                    "Please login first to edit incidents");
            return;
        }

        Incident selected = incidentTable.getSelectionModel().getSelectedItem(); //выбранный в таблице инцидент
        if (selected == null) { //проверка что выбран
            showError("No selection", "Please select an incident to edit");
            return;
        }

        if (!selected.getOwnerUsername().equals(userService.getCurrentUser().getLogin())) {
            showError("Access Denied",
                    "You can only edit your own incidents (owner: " +
                            selected.getOwnerUsername() + ")");
            return;
        }

        Optional<Incident> result = showIncidentDialog(selected); //диалог в режиме редактирования
        result.ifPresent(incident -> { //если сохранили изменения
            try {
                incidentService.updateFull( //обновление всех полей
                        incident.getId(),
                        incident.getTitle(),
                        incident.getDescription(),
                        incident.getSeverity(),
                        incident.getStatus(),
                        incident.getSampleId(),
                        incident.getInstrumentId()
                ).orElseThrow(() -> new IllegalArgumentException("Incident not found"));
                handleRefresh();
                showInfo("Success", "Incident updated successfully");
            } catch (Exception e) {
                showError("Error updating incident", e.getMessage());
            }
        });
    }

    private void handleDelete() {

        if (userService == null || !userService.isLoggedIn()) {
            showError("Authentication Required",
                    "Please login first to delete incidents");
            return;
        }

        Incident selected = incidentTable.getSelectionModel().getSelectedItem(); //выбранный инцидент
        if (selected == null) {
            showError("No selection", "Please select an incident to delete");
            return;
        }

        if (!selected.getOwnerUsername().equals(userService.getCurrentUser().getLogin())) {
            showError("Access Denied",
                    "You can only delete your own incidents (owner: " +
                            selected.getOwnerUsername() + ")");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); //диалоговое окно для подтверждения
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete incident #" + selected.getId());
        alert.setContentText("Are you sure you want to delete this incident?");

        Optional<ButtonType> result = alert.showAndWait(); //показывает и ждет ответа
        if (result.isPresent() && result.get() == ButtonType.OK) { //нажали на кнопку и это ОК
            try {
                incidentService.deleteIncident(selected.getId());
                handleRefresh();
                showInfo("Success", "Incident deleted successfully"); //диалог с информацией
            } catch (Exception e) {
                showError("Error deleting incident", e.getMessage());
            }
        }
    }

    private void handleSave() {
        if (currentFilePath == null) {
            showInfo("No file", "Please use 'Save As...' to select a file first");
            return;
        }

        try {
            // сохраняем в запомненный путь
            saveToFile(currentFilePath);
            hasUnsavedChanges = false;  // Сбрасываем флаг изменений

        } catch (Exception e) {
            showError("Error saving file", e.getMessage());
        }
    }

    private void handleSaveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Incidents As...");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );

        // если есть текущий файл — предложить ту же папку и имя
        if (currentFilePath != null) {
            File currentFile = new File(currentFilePath);
            fileChooser.setInitialDirectory(currentFile.getParentFile());
            fileChooser.setInitialFileName(currentFile.getName());
        }

        File file = fileChooser.showSaveDialog(incidentTable.getScene().getWindow());
        if (file != null) {
            try {
                // запоминаем новый путь и сохраняем
                currentFilePath = file.getAbsolutePath();
                saveToFile(currentFilePath);
                hasUnsavedChanges = false;

            } catch (Exception e) {
                showError("Error saving file", e.getMessage());
            }
        }
    }

    private void handleLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Incidents");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );

        File file = fileChooser.showOpenDialog(incidentTable.getScene().getWindow());
        if (file != null) {
            try {
                fileStorage.load(file.getAbsolutePath());

                // ✅ Запоминаем путь к загруженному файлу
                currentFilePath = file.getAbsolutePath();
                hasUnsavedChanges = false;

                handleRefresh();

                showInfo("Success", "Data loaded from " + file.getName());

            } catch (Exception e) {
                showError("Error loading file", e.getMessage());
            }
        }
    }

    private void handleAddSample() {
        Dialog<Sample> dialog = new Dialog<>(); //диалоговое окно, возвращает Sample
        dialog.setTitle("Add Sample");
        dialog.setHeaderText("Create new sample");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE); //кнопка для подтверждения действия
        //OK_DONE реагирует на enter, автоматическое позиционирование, при нажатии автоматически закрывает диалог и возвращает результат
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        //getDialogPane() - получает панель диалога, getButtonTypes() - получает список типов кнопок,
        //addAll - добавляет уже прописанную кнопку сохранения и кнопку отмены

        GridPane grid = new GridPane(); //создает сетку для размещения элементов
        grid.setHgap(10); //горизонтальный отступ между колонками = 10 пикселей
        grid.setVgap(10); //вертикальный отступ между строками
        grid.setPadding(new Insets(20, 150, 10, 10)); //отступы от краев ячейки до элементов

        Label nameLabel = new Label("Name:"); //что вводить
        TextField nameField = new TextField(); //поле для ввода
        nameField.setPromptText("Enter sample name"); //подсказка внутри поля

        grid.add(nameLabel, 0, 0); //добавляет текст в сетку
        grid.add(nameField, 1, 0); //добавляет поле в сетку справа от текста

        dialog.getDialogPane().setContent(grid); //добавляет сетку в диалоговое окно


        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType); //находит кнопку сохранения
        saveButton.setDisable(true); //кнопка заблокирована

        nameField.textProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(newVal == null || newVal.trim().isEmpty())
        ); //textProperty() - свойство текста в поле, addListener - выполняет код при каждом изменении текста
        //если ввод не null или не пустой - возвращает false - кнопка активна


        dialog.setResultConverter(dialogButton -> { //setResultConverter вызывается когда нажимаем на кнопку
            if (dialogButton == saveButtonType) { //нажали сохранение
                long id = sampleService.SampleAdd(nameField.getText().trim());
                handleRefresh();
                return new Sample(id, nameField.getText().trim()); //создается образец с введенным названием
            }
            return null;
        });

        Optional<Sample> result = dialog.showAndWait(); //нельзя взаимодействовать с основным окном пока отрыт диалог
        //возвращает Sample или пустой optional при отмене
        result.ifPresent(sample ->
                showInfo("Success", "Sample #" + sample.getId() + " created successfully")
        );
    }

    private void handleAddInstrument() { //как sample
        Dialog<Instrument> dialog = new Dialog<>();
        dialog.setTitle("Add Instrument");
        dialog.setHeaderText("Create new instrument");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter instrument name");

        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);


        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        nameField.textProperty().addListener((obs, oldVal, newVal) ->
                saveButton.setDisable(newVal == null || newVal.trim().isEmpty())
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                long id = instrumentService.InstAdd(nameField.getText().trim());
                handleRefresh();
                return new Instrument(id, nameField.getText().trim());
            }
            return null;
        });

        Optional<Instrument> result = dialog.showAndWait();
        result.ifPresent(instrument ->
                showInfo("Success", "Instrument #" + instrument.getId() + " created successfully")
        );
    }


    private Optional<Incident> showIncidentDialog(Incident incident) {

        Dialog<Incident> dialog = new Dialog<>(); //новый диалог
        dialog.setTitle(incident == null ? "Add Incident" : "Edit Incident"); //если не существует, то создаем, иначе редактируем
        dialog.setHeaderText(incident == null ? "Create new incident" : "Edit incident #" + incident.getId());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE); //enter по умолчанию для сохранения
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL); //в панель диалога передается список типов кнопок, добавляются прописанное сохранить и отмена

        GridPane grid = new GridPane(); //сетка для элементов
        grid.setHgap(10); //горизонтальный отступ между колонками
        grid.setVgap(10); //вертикальный отступ между строками
        grid.setPadding(new Insets(20, 150, 10, 10)); //отступы от краев ячейки до элемента

        Label titleLabel = new Label("Title:"); //текст для ввода
        TextField titleField = new TextField(incident != null ? incident.getTitle() : ""); //поле ввода
        // если уже сущаествует, то название вызывается
        titleField.setPromptText("Enter title (1-128 chars)"); //подсказка внутри поля

        Label descLabel = new Label("Description:");
        TextArea descArea = new TextArea(incident != null ? incident.getDescription() : ""); //многострочное поле ввода
        descArea.setPromptText("Enter description (optional)");
        descArea.setPrefRowCount(3); //высота поля 3 строки

        Label severityLabel = new Label("Severity:");
        ChoiceBox<IncidentSeverity> severityChoice = new ChoiceBox<>(); //новый выпадающий список
        severityChoice.getItems().addAll(IncidentSeverity.values()); //список заполняется значениями из enum
        severityChoice.setValue(incident != null ? incident.getSeverity() : IncidentSeverity.LOW); //по умолчанию при создании low

        Label statusLabel = new Label("Status:");
        ChoiceBox<IncidentStatus> statusChoice = new ChoiceBox<>();
        statusChoice.getItems().addAll(IncidentStatus.values());
        statusChoice.setValue(incident != null ? incident.getStatus() : IncidentStatus.NEW);

        Label ownerLabel = new Label("Owner:");
        TextField ownerField = new TextField(incident != null ? incident.getOwnerUsername() : "");
        ownerField.setPromptText("Enter owner (or leave empty for SYSTEM)"); //если пустое то при создании присвоится system

        Label sampleIdLabel = new Label("Sample ID:");
        TextField sampleIdField = new TextField(
                incident != null ? String.valueOf(incident.getSampleId()) : "0"
        ); //преобразует long в string для отображения в текстовом поле
        sampleIdField.setPromptText("0 = no sample");

        Button selectSampleButton = new Button("Select..."); //кнопка для выбора существующих образцов из таблицы
        selectSampleButton.setOnAction(e -> {
            //список существующих образцов
            showSampleSelectionDialog(sampleIdField); //передает поле выбранного образца
        });

        HBox sampleIdBox = new HBox(5, sampleIdField, selectSampleButton); //элементы размещаются в одной строке, 5 - отступ

        Label instIdLabel = new Label("Instrument ID:");
        TextField instIdField = new TextField(incident != null ? String.valueOf(incident.getInstrumentId()) : "0");
        instIdField.setPromptText("Enter instrument ID (0 if none)");

        Button selectInstButton = new Button("Select...");
        selectInstButton.setOnAction(e -> {
            //список существующих инструментов
            showInstrumentSelectionDialog(instIdField);
        });

        HBox instrIdBox = new HBox(5, instIdField, selectInstButton);

        //добавление элементов в сетку
        //колонка 0 - что ввести
        //колонка 1 - поле ввода
        grid.add(titleLabel, 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(descLabel, 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(severityLabel, 0, 2);
        grid.add(severityChoice, 1, 2);
        grid.add(statusLabel, 0, 3);
        grid.add(statusChoice, 1, 3);
        grid.add(ownerLabel, 0, 4);
        grid.add(ownerField, 1, 4);
        grid.add(sampleIdLabel, 0, 5);
        grid.add(sampleIdBox, 1, 5);
        grid.add(instIdLabel, 0, 6);
        grid.add(instrIdBox, 1, 6);

        dialog.getDialogPane().setContent(grid); //помещаем сетку в диалоговое окно

        Platform.runLater(() -> { //выполнение кода только с полностью готовым диалогом
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType); //находит кнопку сохранения
            //без runLater вернет null

            //разблокирует кнопку когда заголовок валидный
            titleField.textProperty().addListener((obs, oldVal, newVal) -> {
                boolean valid = newVal != null && !newVal.trim().isEmpty() && newVal.length() <= 128;
                saveButton.setDisable(!valid); //блокирует если требования не выполнены
            }); //слушатель запускает код при изменении текста

            //инициализирует состояние при открытии диалога, тк при редактированиии поле уже заполнено
            String currentTitle = titleField.getText();
            boolean valid = currentTitle != null && !currentTitle.trim().isEmpty() && currentTitle.length() <= 128;
            saveButton.setDisable(!valid);
        });

        dialog.setResultConverter(dialogButton -> { //код срабатывает при нажатии кнопки
            if (dialogButton == saveButtonType) { //нажали сохранить
                try {
                    long sampleId = Long.parseLong(sampleIdField.getText().trim()); //получает строку из поля, удаляет пробелы, преобразует в число
                    long instId = Long.parseLong(instIdField.getText().trim());

                    if (sampleId < 0 || instId < 0) {
                        showError("Validation Error", "Sample ID and Instrument ID must be >= 0");
                        return null;
                    }

                    String owner = ownerField.getText().trim();
                    if (owner.isEmpty()) {
                        owner = "SYSTEM"; //при пустом вводе присваивает system
                    }

                    if (sampleId > 0 && !sampleService.exists(sampleId)) { //проверка существования образца
                        showError("Validation Error",
                                "Sample with ID=" + sampleId + " does not exist.\n" +
                                        "Please create the sample first or use 0 for no sample.");
                        return null;
                    }

                    if (instId > 0 && !instrumentService.exists(instId)) {
                        showError("Validation Error",
                                "Instrument with ID=" + instId + " does not exist.\n" +
                                        "Please create the instrument first or use 0 for no instrument.");
                        return null;
                    }

                    //создание инцидента
                    if (incident == null) {
                        return new Incident(
                                0, //временный id
                                titleField.getText().trim(),
                                descArea.getText().trim(),
                                severityChoice.getValue(),
                                statusChoice.getValue(),
                                sampleId,
                                instId,
                                owner,
                                java.time.Instant.now(),
                                java.time.Instant.now()
                        );
                    } else {
                        //создаём новый объект с обновлёнными данными
                        return new Incident(
                                incident.getId(),                          // тот же ID
                                titleField.getText().trim(),
                                descArea.getText().trim(),
                                severityChoice.getValue(),
                                statusChoice.getValue(),
                                sampleId,
                                instId,
                                owner,
                                incident.getCreatedAt(),
                                java.time.Instant.now()
                        );
                    }
                } catch (NumberFormatException e) {
                    showError("Validation Error", "Sample ID and Instrument ID must be numbers");
                    return null;
                }
            }
            return null; //если нажали отмена
        });

        return dialog.showAndWait().filter(Objects::nonNull);} //показывает диалог и блокирует пока не закроют
    //возвращает результат из setResultConverter
    //Optional<Incident> с объектом если сохранено, или пустой если отменили

    private void showSampleSelectionDialog(TextField targetField) { //targetField - поле куда записать id выбранного образца
        Dialog<Sample> dialog = new Dialog<>(); //новое диалоговое окно, возвращает Sample
        dialog.setTitle("Select Sample");
        dialog.setHeaderText("Choose a sample:");

        TableView<Sample> table = new TableView<>();
        table.setPrefSize(400, 300);

        TableColumn<Sample, Long> idCol = new TableColumn<>("ID"); //колонка для id
        idCol.setCellValueFactory(new PropertyValueFactory<>("id")); //вызывает getId для каждой строки
        idCol.setPrefWidth(50);

        TableColumn<Sample, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name")); //вызывает getName
        nameCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, nameCol); //добавляем столбцы в таблицу по порядку

        List<Sample> samples = sampleService.getAll(); //получает все образцы из сервиса
        table.getItems().addAll(samples); //добавляет образцы в таблицу

        dialog.getDialogPane().setContent(table); //таблица внутрь диалога
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL); //подтверждение выбора и отмена

        table.setRowFactory(tv -> { //меняем поведение строк
            TableRow<Sample> row = new TableRow<>(); //новая строка с объектом
            row.setOnMouseClicked(event -> { //вызывается при клике
                if (event.getClickCount() == 2 && (!row.isEmpty())) { //двойной клик по непустой строке
                    targetField.setText(String.valueOf(row.getItem().getId())); //получает sample из строки, получает id, преобразует в string, вставляет в поле
                    dialog.setResult(row.getItem()); //sample как результат диалога
                    dialog.close();
                }
            });
            return row;
        });

        dialog.setResultConverter(dialogButton -> { //вместо двойного клика кажатие ОК
            if (dialogButton == ButtonType.OK) {
                return table.getSelectionModel().getSelectedItem(); //получает то, что выделено, возвращает выделенный sample
            }
            return null;
        });

        Optional<Sample> result = dialog.showAndWait(); //блокирует все кроме открытого диалога, optional<sample> или пустой
        result.ifPresent(sample -> //если выбрали sample
                targetField.setText(String.valueOf(sample.getId())) //записывает в поле
        );
    }

    private void showInstrumentSelectionDialog(TextField targetField) { //аналогично sample
        Dialog<Instrument> dialog = new Dialog<>();
        dialog.setTitle("Select Instrument");
        dialog.setHeaderText("Choose an instrument:");

        TableView<Instrument> table = new TableView<>();
        table.setPrefSize(400, 300);

        TableColumn<Instrument, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);

        TableColumn<Instrument, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(300);

        table.getColumns().addAll(idCol, nameCol);

        List<Instrument> instruments = instrumentService.getAll();
        table.getItems().addAll(instruments);

        dialog.getDialogPane().setContent(table);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        table.setRowFactory(tv -> {
            TableRow<Instrument> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    targetField.setText(String.valueOf(row.getItem().getId()));
                    dialog.setResult(row.getItem());
                    dialog.close();
                }
            });
            return row;
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return table.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<Instrument> result = dialog.showAndWait();

        result.ifPresent(instrument ->
                targetField.setText(String.valueOf(instrument.getId()))
        );
    }


    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setIncidentService(IncidentService service) {
        this.incidentService = service;
    }

    public void setSampleService(SampleService service) {
        this.sampleService = service;
    }

    public void setInstrumentService(InstrumentService service) {
        this.instrumentService = service;
    }


    public void setFileStorage(JsonFileStorage storage) {
        this.fileStorage = storage;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setUserStorage(JsonUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private void setAllIncidents(List<Incident> incidents) {//обнвление таблицы
        if (incidentList != null) {
            incidentList.setAll(incidents);
            incidentTable.refresh();
        }
    }

    private void setAllSamples(List<Sample> samples) {
        if (sampleList != null) {
            sampleList.setAll(samples);
            sampleTable.refresh();
        }
    }

    private void setAllInstruments(List<Instrument> instruments) {
        if (instrumentList != null) {
            instrumentList.setAll(instruments);
            instrumentTable.refresh();
        }
    }
}