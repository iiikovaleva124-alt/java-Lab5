package ru.megateam.lab.cli;

import ru.megateam.lab.domain.IncidentSeverity;
import ru.megateam.lab.domain.IncidentStatus;
import ru.megateam.lab.repository.InMemoryIncidentRepository;
import ru.megateam.lab.service.IncidentService;

import java.time.LocalDate;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {

        var repo = new InMemoryIncidentRepository();
        var service = new IncidentService(repo);

        System.out.println("=== Тестирование IncidentService ===");

        var inc1 = service.add("Spill on bench", IncidentSeverity.HIGH,
                "acid spill near sink", "SYSTEM");
        System.out.println("1. Создан инцидент: " + inc1.id);


        System.out.println("2. Список:");
        service.list(Optional.empty(), Optional.empty())
                .forEach(inc -> System.out.println("  ID=" + inc.id + " " +
                        inc.severity + " " + inc.status + " " + inc.title));


        System.out.println("3. Показать ID=1:");
        service.getById(1L).ifPresent(inc ->
                System.out.println("  " + inc.title + " (" + inc.severity + ")"));


        service.update(1L, "status", "INVESTIGATING");
        System.out.println("4. Обновлён статус на INVESTIGATING");

        System.out.println("5. Только INVESTIGATING:");
        service.list(Optional.of(IncidentStatus.INVESTIGATING), Optional.empty())
                .forEach(inc -> System.out.println("  " + inc.id));


        service.linkSample(1L, 12L);
        System.out.println("6. Связан sampleId=12");


        service.close(1L);
        System.out.println("7. Закрыт");


        var report = service.report(LocalDate.of(2026, 2, 27),
                LocalDate.of(2026, 2, 27));
        System.out.println("8. Отчёт за день: " + report);

        System.out.println("=== Всё OK! ===");
    }
}
