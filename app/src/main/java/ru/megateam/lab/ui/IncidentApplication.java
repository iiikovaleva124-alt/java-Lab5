package ru.megateam.lab.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
//import ru.megateam.lab.persistence.JsonFileStorage;
import ru.megateam.lab.repository.JdbcInstrumentRepository;
import ru.megateam.lab.repository.JdbcSampleRepository;
import ru.megateam.lab.service.IncidentService;
import ru.megateam.lab.service.SampleService;
import ru.megateam.lab.service.InstrumentService;
import ru.megateam.lab.repository.InMemoryIncidentRepository;
import ru.megateam.lab.persistence.FileValidator;
import ru.megateam.lab.persistence.DbConnectionManager;
import ru.megateam.lab.repository.JdbcIncidentRepository;
//import ru.megateam.lab.persistence.FileValidator;

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


            var dbConnectionManager = new DbConnectionManager();
            var repository = new JdbcIncidentRepository(dbConnectionManager);
            var sampleRepository = new JdbcSampleRepository(dbConnectionManager);
            var instrumentRepository = new JdbcInstrumentRepository(dbConnectionManager);
            var sampleService = new SampleService(sampleRepository);
            var instrumentService = new InstrumentService(instrumentRepository);
            var validator = new FileValidator();

            var incidentService = new IncidentService(
                    repository,
                    sampleService,
                    instrumentService,
                    null,
                    validator
            );

            Scene scene = new Scene(fxmlLoader.load(), 900, 600); //создает визуал

            IncidentController controller = fxmlLoader.getController(); //получаем созданный контроллер
            if (controller != null) {
                controller.setIncidentService(incidentService); //передаем сервисы
            }

            assert controller != null;
            controller.setSampleService(sampleService);
            controller.setInstrumentService(instrumentService);


            stage.setTitle("Incident Management System");
            stage.setScene(scene); //выводит сцену в окно
            stage.show(); //выводит на экран

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(); //все выводы, чтобы понять где упало приложение
        }
    }

        public static void main(String[] args) {
            launch(); //запускает приложение
        }

}