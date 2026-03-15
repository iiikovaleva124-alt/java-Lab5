package ru.megateam.lab.cli;

import ru.megateam.lab.domain.*;
import ru.megateam.lab.service.*;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class IncidentCLI {
    private final IncidentService incidentService;
    private final SampleService sampleService;
    private final InstrumentService instrumentService;
    private final Scanner scanner;
    private String currentUser = "SYSTEM";

    public IncidentCLI(IncidentService incidentService,
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

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            if (input.equals("exit")) {
                System.out.println("До свидания!");
                break;
            }

            if (input.equals("help")) {
                printHelp();
                continue;
            }

            try {
                executeCommand(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (DateTimeParseException e) {
                System.out.println("Incorrect date (используйте YYYY-MM-DD)");
            } catch (Exception e) {
                System.out.println("Sorry, we can not do this");
            }
        }
    }

    private void executeCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "inc_add" -> handleIncAdd();
            case "inc_list" -> handleIncList(args); // вот эти команды надо прописать
            case "inc_show" -> handleIncShow(args);
            case "inc_update" -> handleIncUpdate(args);
            case "inc_link_sample" -> handleLinkSample(args);
            case "inc_link_instrument" -> handleLinkInstrument(args);
            case "inc_comment_add" -> handleCommentAdd(args);
            case "inc_comment_list" -> handleCommentList(args);
            case "inc_close" -> handleIncClose(args);
            case "inc_report" -> handleIncReport(args);
            case "sample_add" -> handleSampleAdd();
            case "inst_add" -> handleInstAdd();

            default -> throw new IllegalArgumentException("Неизвестная команда: " + command);
        }
    }

    private void handleIncAdd() {
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

    private void printHelp() {
        System.out.println("\nIncident methods");
        System.out.println("  inc_add                     - add an incident");
        System.out.println("  inc_list [--status STATUS] [--last N] - list of incidents by status and date");
        System.out.println("  inc_show <id>               - show incident by id");
        System.out.println("  inc_update <id> field=value - update field in incident");
        System.out.println("  inc_link_sample <inc_id> <sample_id> - link a sample");
        System.out.println("  inc_link_instrument <inc_id> <inst_id> - link an instrument");
        System.out.println("  inc_comment_add <inc_id>    - add a comment");
        System.out.println("  inc_comment_list <inc_id>   - list of comments");
        System.out.println("  inc_close <id>              - close incident");
        System.out.println("  inc_report [--from] [--to]  - report of incidents between dates");
        System.out.println("  sample_add                  - create a sample");
        System.out.println("  inst_add                    - add an instrument");
        System.out.println();
        System.out.println("  help                        - эта справка");
        System.out.println("  exit                        - выход");
        System.out.println();
    }
}