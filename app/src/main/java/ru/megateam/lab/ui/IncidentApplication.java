package ru.megateam.lab.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import ru.megateam.lab.persistence.DbConnectionManager;
import ru.megateam.lab.persistence.JsonUserStorage;
import ru.megateam.lab.persistence.UserFileStorage;
import ru.megateam.lab.repository.InMemoryUserRepository;
import ru.megateam.lab.repository.JdbcInstrumentRepository;
import ru.megateam.lab.repository.JdbcSampleRepository;
import ru.megateam.lab.service.*;
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

            FXMLLoader fxmlLoader = new FXMLLoader(); //для превращения xml в кнопки и таблицы
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

            var connectionManager = new DbConnectionManager();

            var sampleRepository = new JdbcSampleRepository(connectionManager);
            var instrumentRepository = new JdbcInstrumentRepository(connectionManager);
            var repository = new InMemoryIncidentRepository();
            var sampleService = new SampleService(sampleRepository);
            var instrumentService = new InstrumentService(instrumentRepository);
            var validator = new FileValidator();
            var incidentService = new IncidentService(repository, sampleService, instrumentService, null, validator, userService);

            Parent root = fxmlLoader.load(); //parent - абстрактный класс для контейнеров
            //из fxml создаются объекты и связываются с контроллером

            IncidentController controller = fxmlLoader.getController(); //получаем созданный контроллер
            if (controller != null) {
                controller.setIncidentService(incidentService); //передаем сервисы
                controller.setSampleService(sampleService);
                controller.setInstrumentService(instrumentService);
                controller.setUserService(userService);
                controller.setUserStorage(userStorage);
            }

            assert controller != null; //проверяем что контроллер создан
            boolean authenticated = controller.showAuthDialog(); //показывает диалог входа и блокирует остальное пока не войдем

            if (!authenticated) { //если нажимаем отмена или закрываем окно
                System.exit(0);
                return;
            }

            controller.updateAuthUI(); //обновляем после входа

            Scene scene = new Scene(root, 1200, 900);

            String username = userService.getCurrentUser().getLogin(); //получаем логин вошедшего пользователя
            stage.setTitle("Incident Management System - " + username); //для заголовка окна чтобы было видно в какой учетке
            stage.setScene(scene); //выводит сцену в окно
            stage.show(); //выводит на экран

            controller.handleRefresh();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(); //все выводы, чтобы понять где упало приложение
        }
    }

        public static void main(String[] args) {
            launch(); //запускает приложение
        }

}