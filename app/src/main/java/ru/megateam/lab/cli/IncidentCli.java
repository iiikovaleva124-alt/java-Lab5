package ru.megateam.lab.cli;

import ru.megateam.lab.domain.*;
import ru.megateam.lab.service.*;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class IncidentCli {
    private final IncidentService incidentService;
    private final SampleService sampleService;
    private final InstrumentService instrumentService;
    private final Scanner scanner;
    private final String currentUser = "SYSTEM";

    public IncidentCli(IncidentService incidentService,
                       SampleService sampleService,
                       InstrumentService instrumentService) {
        this.incidentService = incidentService;
        this.sampleService = sampleService;
        this.instrumentService = instrumentService;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("Incidents service");
        System.out.println("Type 'help' for list of methods.\n");
        System.out.println("Type 'exit' to stop.\n");

        label:
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "":
                    continue;
                case "exit":
                    System.out.println("Thanks!");
                    break label;
                case "help":
                    printHelp();
                    continue;
            }

            try {
                executeCommand(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (DateTimeParseException e) {
                System.out.println("Incorrect date (use YYYY-MM-DD)");
            } catch (Exception e) {
                System.out.println("Sorry, i can not do this");
            }
        }
    }

    private void executeCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "inc_add" -> handleIncAdd();
            case "inc_list" -> handleIncList(args); // Саша, вот эти команды надо прописать
            case "inc_show" -> handleIncShow(args);
            case "inc_update" -> handleIncUpdate(args);
            case "inc_link_sample" -> handleLinkSample(args);
            case "inc_link_instrument" -> handleLinkInstrument(args);
            case "inc_comment_add" -> handleCommentAdd(args);
            case "inc_comment_list" -> handleCommentList(args);
            case "inc_close" -> handleIncClose(args);
            case "inc_report" -> handleIncReport(args);
            case "sample_add" -> handleSampleAdd(); // есть соответствующая в sample service
            case "inst_add" -> handleInstAdd(); //есть соответствующая в instrument service

            default -> throw new IllegalArgumentException("Unknown command: " + command);
        }
    }

    private void handleIncAdd() { // интерактивная команда
        System.out.println("Creating an incident");

        System.out.print("Please type a title (1-128 chars): ");
        String title = scanner.nextLine();

        System.out.print("Severity (LOW/MEDIUM/HIGH): ");
        String severityStr = scanner.nextLine().trim().toUpperCase();
        IncidentSeverity severity;
        try {
            severity = IncidentSeverity.valueOf(severityStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Incorrect severity, please use LOW/MEDIUM/HIGH");
        }

        System.out.print("Description (can be empty, max 1024 chars): ");
        String description = scanner.nextLine();

        Incident incident = incidentService.add(title, severity, description, currentUser);
        System.out.println("OK incident_id=" + incident.getId());
    }

    private void handleIncList(String args) { //user получает таблицу всех инцидентов
        // переменные для фильтров
        IncidentStatus status = null;
        Integer lastN = null; //integral потому что int не может быть null
        // если статус не указан и число последних инцидентов то покажет все инциденты
        // далее разделяем строку по пробелам, каждый пробел отделяет элемент в созданном массиве-tokens
        String[] tokens = args.split("\\s+");
        // ищем --status и --last (проходим по всем словам в массиве)
        for (int i = 0; i < tokens.length; i++) {

            if ("--status".equals(tokens[i]) && i + 1 < tokens.length) {
                // если текущее слвоо - --status, а следующее слово есть
                try {
                    status = IncidentStatus.valueOf(tokens[i + 1].toUpperCase());// то мы берем след слово, превращаем в верхний регистр, превращаем в строку из enum
                } catch (IllegalArgumentException e) { //если user написал значения, которого нет в enum
                    throw new IllegalArgumentException(
                            "Unknown status '" + tokens[i + 1] + "'. Use: NEW, INVESTIGATING, CLOSED");
                }
                i++;  // пропускаем следующее слово, тк мы его уже прочитали

            } else if ("--last".equals(tokens[i]) && i + 1 < tokens.length) {
                // нашли --last, следующее слово должно быть числом
                try {
                    lastN = Integer.parseInt(tokens[i + 1]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("--last must be a number");
                }
                i++;  // пропускаем
            }
        }

        // вызываем сервис — он отфильтрует и вернёт список
        List<Incident> list = incidentService.list(Optional.ofNullable(status), Optional.ofNullable(lastN));

        if (list.isEmpty()) {
            System.out.println("No incidents found");
            return;
        }

        // 6. Выводим таблицу
        System.out.printf("%-4s %-8s %-15s %s%n", "ID", "Severity", "Status", "Title");
        for (Incident inc : list) {
            System.out.printf("%-4d %-8s %-15s %s%n",
                    inc.getId(), inc.getSeverity(), inc.getStatus(), inc.getTitle());
        }
    }

    private void handleIncShow(String args) { //string args- то что user написал после команды
        //проверяем что пользователь вообще ввёл id (просто inc_show)
        if (args.isEmpty()) {
            System.out.println("Error: Usage: inc_show <id>");
            return;
        }

        // Пытаемся превратить строку в число
        long id;
        try {
            id = Long.parseLong(args.trim()); // убираем пробелы по краям
        } catch (NumberFormatException e) { // проверка на число а не текст
            System.out.println("Error: id must be a number");
            return;
        }

        // ищем инцидент по id в сервисе, optional  потому что инцидента с таким id может и не существовать
        Optional<Incident> found = incidentService.getById(id);

        // как раз если нет инцидента по заданному id
        if (found.isEmpty()) {
            System.out.println("Error: incident with id=" + id + " not found");
            return;
        }

        //если нашли, то выводим поля
        Incident inc = found.get();
        System.out.println("Incident #" + inc.getId());
        System.out.println("  severity: " + inc.getSeverity());
        System.out.println("  status: " + inc.getStatus());
        System.out.println("  title: " + inc.getTitle());
        System.out.println("  description: " + (inc.getDescription() == null ? "" : inc.getDescription()));// описание может быть пустым, а null некрасиво; короткий if
        System.out.println("  sampleId: " + inc.getSampleId());
        System.out.println("  instrumentId: " + inc.getInstrumentId());
        System.out.println("  owner: " + inc.getOwnerUsername());
        System.out.println("  created: " + inc.getCreatedAt());
        System.out.println("  updated: " + inc.getUpdatedAt());
    }
    // программа меняет поле статус (обновляет)
    private void handleIncUpdate(String args) {
        // разбиваем строку на две части: id и field=value
        String[] tokens = args.split("\\s+", 2);

        //проверяем что пользователь ввёл оба значения
        if (tokens.length < 2) {
            System.out.println("Error: Usage: inc_update id field=value");
            return;
        }

        long id; //создаем переменную типа long
        try {
            id = Long.parseLong(tokens[0]); //превращаем текст в число
        } catch (NumberFormatException e) {
            System.out.println("Error: id must be a number");
            return;
        }

        // "status=INVESTIGATING" разрезаем по знаку "="
        String[] fieldValue = tokens[1].split("=", 2);

        // проверяем что есть и поле и значение
        if (fieldValue.length < 2) {
            System.out.println("Error: Usage: inc_update <id> field=value (example: status=INVESTIGATING)");
            return;
        }
        //достаем значения из массива, trim убирает лишние пробелы
        String field = fieldValue[0].trim();   // "статус"
        String value = fieldValue[1].trim();   // "значение статуса"

        // передаем в сервис, он сам проверит поле и значение
        incidentService.update(id, field, value);
        //условно: возьми инцидент с таким-то id, найди у него поле field и поменя его на значение value
        System.out.println("OK");
    }
    // привязывает номер образец к номеру инцидента
    private void handleLinkSample(String args) {
        //разбиваем строку по пробелам
        //1 12 в [1, 12]
        String[] tokens = args.split("\\s+");

        //проверяем что user ввёл два числа
        if (tokens.length < 2) {
            System.out.println("Error: Usage: inc_link_sample <incident_id> <sample_id>");
            return;
        }

        //первое число - id инцидента
        long incidentId;
        try {
            incidentId = Long.parseLong(tokens[0]); //long.parseLong превращает оба слова в числа
        } catch (NumberFormatException e) {
            System.out.println("Error: incident_id must be a number");
            return;
        }

        //второе число — id образца
        long sampleId;
        try {
            sampleId = Long.parseLong(tokens[1]);
        } catch (NumberFormatException e) {
            System.out.println("Error: sample_id must be a number");
            return;
        }

        //сервис привяжет образец к инциденту
        incidentService.linkSample(incidentId, sampleId);

        System.out.println("OK linked");
    }
    //метод привязывает id прибора к id инцидента
    private void handleLinkInstrument(String args) {

        String[] tokens = args.split("\\s+");

        if (tokens.length < 2) {
            System.out.println("Error: Usage: inc_link_instrument <incident_id> <instrument_id>");
            return;
        }

        // первое число - id инцидента
        long incidentId;
        try {
            incidentId = Long.parseLong(tokens[0]);
        } catch (NumberFormatException e) {
            System.out.println("Error: incident_id must be a number");
            return;
        }

        // второе число - id прибора
        long instrumentId;
        try {
            instrumentId = Long.parseLong(tokens[1]);
        } catch (NumberFormatException e) {
            System.out.println("Error: instrument_id must be a number");
            return;
        }

        incidentService.linkInstrument(incidentId, instrumentId);

        System.out.println("OK linked");
    }
    //интерактивный метод: спрашивает текст комментария и добавляет к инциденту по id
    private void handleCommentAdd(String args) {
        //проверяем что ввели id
        if (args.isEmpty()) {
            System.out.println("Error: Usage: inc_comment_add <incident_id>");
            return;
        }

        // превращаем в число
        long incidentId;
        try {
            incidentId = Long.parseLong(args.trim()); //args.trim убирает пробелы по краям; long.parseLong превращает текст в число
        } catch (NumberFormatException e) {
            System.out.println("Error: incident_id must be a number");
            return;
        }

        incidentService.IncidentExists(incidentId);

        //спрашиваем текст комментария у пользователя (интерактив)
        System.out.print("Comment: ");
        String text = scanner.nextLine(); //возвращает то, что написал user

        //проверяем что не пустой
        if (text.trim().isEmpty()) {
            System.out.println("Error: comment cannot be empty");
            return;
        }

        //отправляем в сервис - он сохранит комментарий и вернёт его id
        long commentId = incidentService.addComment(incidentId, text, currentUser);

        System.out.println("OK comment_id=" + commentId);
    }
    // метод показывает все комментарии к заданному инциденту
    private void handleCommentList(String args) {
        if (args.isEmpty()) {
            System.out.println("Error: Usage: inc_comment_list incident_id");
            return;
        }

        long incidentId;
        try {
            incidentId = Long.parseLong(args.trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: incident_id must be a number");
            return;
        }

        incidentService.IncidentExists(incidentId);

        // получаем список комментариев из сервиса
        List<Comment> comments = incidentService.getComments(incidentId);

        // если комментариев нет
        if (comments.isEmpty()) {
            System.out.println("No comments");
            return;
        }

        // если комментарии есть, то выводим таблицу
        System.out.printf("%-4s %-22s %s%n", "ID", "Time", "Text");
        for (Comment c : comments) { //цикл: "для каждого комментария с":"из списка comments"-выведи строку; короче достает их по одному
            System.out.printf("%-4d %-22s %s%n",
                    c.getId(), c.getCreatedAt(), c.getText());
        }
    }
    //метод закрывает инцидент -ставит статус closed
    private void handleIncClose(String args) {
        if (args.isEmpty()) {
            System.out.println("Error: Usage: inc_close id");
            return;
        }

        long id;
        try {
            id = Long.parseLong(args.trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: id must be a number");
            return;
        }

        // вызываем сервис, который закроет инцидент
        incidentService.close(id);

    }
    //показывает инциденты по введенному времени
    private void handleIncReport(String args) {
        // переменные для дат, если user что-то напишет, то мы заменим null
        LocalDate from = null;
        LocalDate to = null;

        // если аргументы есть
        if (!args.isEmpty()) {
            String[] tokens = args.split("\\s+");

            // ищем --from и --to
            for (int i = 0; i < tokens.length; i++) {

                if ("--from".equals(tokens[i]) && i + 1 < tokens.length) {
                    // следующее слово — дата начала
                    from = LocalDate.parse(tokens[i + 1]);
                    i++;  // перескакиваем значение (--to)
                } else if ("--to".equals(tokens[i]) && i + 1 < tokens.length) {
                    // следующее слово — дата конца
                    to = LocalDate.parse(tokens[i + 1]);
                    i++;
                }
            }
        }

        // сервис посчитает количество по серьёзности, которые попадают в заданные даты
        Map<IncidentSeverity, Long> report = incidentService.report(from, to);
        //отправляем в сервис две даты: from, to;
        // выводим таблицу map
        for (Map.Entry<IncidentSeverity, Long> entry : report.entrySet()) { //цикл
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
    //программа спрашивает название образца и создает его
    private void handleSampleAdd() {
        // спрашиваем название
        System.out.print("Sample name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Error: name cannot be empty");
            return;
        }

        // создает через сервис
        long id = sampleService.SampleAdd(name);

        System.out.println("OK sample_id=" + id);
    }

    private void handleInstAdd() {
        System.out.print("Instrument name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Error: name cannot be empty");
            return;
        }

        long id = instrumentService.InstAdd(name);

        System.out.println("OK instrument_id=" + id);
    }

    private void printHelp() {
        System.out.println("\nIncident methods");
        System.out.println("  inc_add                                       - add an incident");
        System.out.println("  inc_list --status STATUS --last N             - list of incidents by status and date");
        System.out.println("  inc_show id                                   - show incident by id");
        System.out.println("  inc_update id field=value                     - update field in incident");
        System.out.println("  inc_link_sample inc_id sample_id              - link a sample");
        System.out.println("  inc_link_instrument inc_id inst_id            - link an instrument");
        System.out.println("  inc_comment_add inc_id                        - add a comment");
        System.out.println("  inc_comment_list inc_id                       - list of comments");
        System.out.println("  inc_close id                                  - close incident");
        System.out.println("  inc_report --from YYYY-MM-DD --to YYYY-MM-DD  - report of incidents between dates");
        System.out.println("  sample_add                                    - create a sample");
        System.out.println("  inst_add                                      - add an instrument");
        System.out.println();
        System.out.println("  help                                          - type to get info about all commands");
        System.out.println("  exit                                          - type if you want to exit");
        System.out.println();
    }
}