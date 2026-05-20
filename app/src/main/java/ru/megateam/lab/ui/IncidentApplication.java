package ru.megateam.lab.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.megateam.lab.persistence.JsonFileStorage;
import ru.megateam.lab.persistence.JsonUserStorage;
import ru.megateam.lab.persistence.UserFileStorage;
import ru.megateam.lab.repository.InMemoryUserRepository;
import ru.megateam.lab.service.IncidentService;
import ru.megateam.lab.service.SampleService;
import ru.megateam.lab.service.InstrumentService;
import ru.megateam.lab.repository.InMemoryIncidentRepository;
import ru.megateam.lab.persistence.FileValidator;
import ru.megateam.lab.service.UserService;

import java.io.File;

public class IncidentApplication extends Application {

    @Override
    public void start(Stage stage) {
        try {

            String fxmlPath = "src/main/resources/ru/megateam/lab/ui/IncidentView.fxml"; //путь к файлу разметки интерфейса
            File fxmlFile = new File(fxmlPath);

            if (!fxmlFile.exists()) {
                System.err.println("FXML does not found: " + fxmlPath);
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(fxmlFile.toURI().toURL());
            fxmlLoader.setControllerFactory(clazz -> { //создается контроллер
                if (clazz == IncidentController.class) {
                    return new IncidentController(); //возвращает экземляр контроллера
                }
                try {
                    return clazz.getDeclaredConstructor().newInstance(); //находит конструктор без параметров, создает объект
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            InMemoryUserRepository userRepository = new InMemoryUserRepository();
            JsonUserStorage userStorage = new JsonUserStorage("users.json", userRepository);
            userStorage.load(); //загружаем при старте пользователей при запуске
            UserService userService = new UserService(userRepository, userStorage);

            var repository = new InMemoryIncidentRepository();
            var sampleService = new SampleService();
            var instrumentService = new InstrumentService();
            var validator = new FileValidator();
            var incidentService = new IncidentService(repository, sampleService, instrumentService, null, validator, userService);
            var fileStorage = new JsonFileStorage(incidentService, sampleService, instrumentService);

            Scene scene = new Scene(fxmlLoader.load(), 900, 600); //создает визуал

            IncidentController controller = fxmlLoader.getController(); //получаем созданный контроллер
            if (controller != null) {
                controller.setIncidentService(incidentService); //передаем сервисы
                controller.setFileStorage(fileStorage);
                controller.setSampleService(sampleService);
                controller.setInstrumentService(instrumentService);
                controller.setUserService(userService);
                controller.setUserStorage(userStorage);
            }

            assert controller != null;
            boolean authenticated = controller.showAuthDialog();

            controller.setSampleService(sampleService);
            controller.setInstrumentService(instrumentService);
            if (!authenticated) {
                System.exit(0);
                return;
            }

            String username = userService.getCurrentUser().getLogin();
            stage.setTitle("Incident Management System - " + username);
            stage.setScene(scene); //выводит сцену в окно
            stage.show(); //выводит на экран

            if (controller != null) {
                controller.handleRefresh();
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(); //все выводы, чтобы понять где упало приложение
        }
    }

        public static void main(String[] args) {
            launch(); //запускает приложение
        }

}