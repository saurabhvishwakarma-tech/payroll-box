package com.payroll;

import com.payroll.exception.DuplicateEmployeeIdException;
import com.payroll.exception.EmployeeNotFoundException;
import com.payroll.exception.InvalidSalaryException;
import com.payroll.io.CsvEmployeeExporter;
import com.payroll.io.CsvEmployeeImporter;
import com.payroll.io.ImportResult;
import com.payroll.model.Department;
import com.payroll.model.Developer;
import com.payroll.model.Employee;
import com.payroll.model.EmployeeStatus;
import com.payroll.model.Intern;
import com.payroll.model.Manager;
import com.payroll.service.ReportingService;
import com.payroll.service.WorkforceService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/** Scanner-based menu tying the whole thing together. */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final WorkforceService workforceService = new WorkforceService();
    private static final ReportingService reportingService = new ReportingService(workforceService);

    public static void main(String[] args) {
        seedSampleData();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addEmployee();
                case "2" -> removeEmployee();
                case "3" -> findEmployee();
                case "4" -> listAllEmployees();
                case "5" -> sortMenu();
                case "6" -> reportsMenu();
                case "7" -> exportToCsv();
                case "8" -> importFromCsv();
                case "9" -> running = false;
                default -> System.out.println("That's not one of the options - try again.");
            }
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("===== Payroll Box =====");
        System.out.println("1. Add employee");
        System.out.println("2. Remove employee");
        System.out.println("3. Find employee by id");
        System.out.println("4. List all employees");
        System.out.println("5. Sort employees");
        System.out.println("6. Reports");
        System.out.println("7. Export to CSV");
        System.out.println("8. Import from CSV");
        System.out.println("9. Exit");
        System.out.print("Choose an option: ");
    }

    // ---------- CRUD ----------

    private static void addEmployee() {
        System.out.println();
        System.out.println("What type of employee? 1) Manager  2) Developer  3) Intern");
        String typeChoice = scanner.nextLine().trim();

        String name = readNonBlankLine("Name: ");
        Department department = readDepartment();
        LocalDate dateOfJoining = readDate("Date of joining (yyyy-MM-dd): ");
        double baseSalary = readDouble("Base salary: ");

        try {
            Employee employee = switch (typeChoice) {
                case "1" -> {
                    int teamSize = readInt("Team size: ");
                    yield new Manager(name, department, dateOfJoining, baseSalary, EmployeeStatus.ACTIVE, teamSize);
                }
                case "2" -> {
                    double overtimeHours = readDouble("Overtime hours logged so far: ");
                    yield new Developer(name, department, dateOfJoining, baseSalary, EmployeeStatus.ACTIVE, overtimeHours);
                }
                case "3" -> new Intern(name, department, dateOfJoining, baseSalary, EmployeeStatus.ACTIVE);
                default -> null;
            };

            if (employee == null) {
                System.out.println("Not a valid employee type - nothing was added.");
                return;
            }

            workforceService.addEmployee(employee);
            System.out.println("Added " + employee.getName() + " with id " + employee.getId());
        } catch (InvalidSalaryException | DuplicateEmployeeIdException e) {
            System.out.println("Could not add employee: " + e.getMessage());
        }
    }

    private static void removeEmployee() {
        int id = readInt("Id to remove: ");
        try {
            Employee removed = workforceService.removeEmployee(id);
            System.out.println("Removed " + removed.getName());
        } catch (EmployeeNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void findEmployee() {
        int id = readInt("Id to find: ");
        try {
            System.out.println(workforceService.findById(id));
        } catch (EmployeeNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void listAllEmployees() {
        List<Employee> employees = workforceService.listAll();
        if (employees.isEmpty()) {
            System.out.println("No employees yet.");
            return;
        }
        employees.forEach(System.out::println);
    }

    // ---------- Sorting ----------

    private static void sortMenu() {
        System.out.println();
        System.out.println("1) By tenure (longest-serving first)   2) By salary (highest first)");
        String choice = scanner.nextLine().trim();
        List<Employee> sorted = choice.equals("2")
                ? workforceService.sortedBySalary()
                : workforceService.sortedByTenure();
        sorted.forEach(System.out::println);
    }

    // ---------- Reports ----------

    private static void reportsMenu() {
        System.out.println();
        System.out.println("1) Total salary by department");
        System.out.println("2) Average salary by department");
        System.out.println("3) Top earners overall");
        System.out.println("4) Top earners by department");
        System.out.println("5) Group by status");
        System.out.println("6) Group by tenure bracket");
        System.out.println("7) Top active earners (filter -> sort -> limit)");
        System.out.print("Choose a report: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> reportingService.totalSalaryByDepartment()
                    .forEach((dept, total) -> System.out.printf("%-12s %,.2f%n", dept, total));
            case "2" -> reportingService.averageSalaryByDepartment()
                    .forEach((dept, avg) -> System.out.printf("%-12s %,.2f%n", dept, avg));
            case "3" -> {
                int n = readInt("How many? ");
                reportingService.topEarnersOverall(n).forEach(System.out::println);
            }
            case "4" -> {
                int n = readInt("How many per department? ");
                reportingService.topEarnersByDepartment(n).forEach((dept, employees) -> {
                    System.out.println(dept + ":");
                    employees.forEach(e -> System.out.println("  " + e));
                });
            }
            case "5" -> reportingService.groupByStatus().forEach((status, employees) -> {
                System.out.println(status + ":");
                employees.forEach(e -> System.out.println("  " + e));
            });
            case "6" -> reportingService.groupByTenureBracket().forEach((bracket, employees) -> {
                System.out.println(bracket + ":");
                employees.forEach(e -> System.out.println("  " + e));
            });
            case "7" -> {
                int n = readInt("How many? ");
                reportingService.topActiveEarners(n).forEach(System.out::println);
            }
            default -> System.out.println("Not a valid report.");
        }
    }

    // ---------- CSV ----------

    private static void exportToCsv() {
        String path = readNonBlankLine("File path to export to (e.g. workforce.csv): ");
        try {
            new CsvEmployeeExporter().exportToFile(workforceService.listAll(), path);
            System.out.println("Exported " + workforceService.size() + " employees to " + path);
        } catch (IOException e) {
            System.out.println("Could not write file: " + e.getMessage());
        }
    }

    private static void importFromCsv() {
        String path = readNonBlankLine("File path to import from: ");
        try {
            ImportResult result = new CsvEmployeeImporter().importFromFile(path);
            for (Employee employee : result.getImportedEmployees()) {
                try {
                    workforceService.addEmployee(employee);
                } catch (DuplicateEmployeeIdException e) {
                    System.out.println("Skipped: " + e.getMessage());
                }
            }
            System.out.println("Imported " + result.successCount() + " employees.");
            if (result.failureCount() > 0) {
                System.out.println(result.failureCount() + " row(s) failed:");
                result.getFailures().forEach(f -> System.out.println("  " + f));
            }
        } catch (IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
        }
    }

    // ---------- Input helpers ----------
    // By the time these return, the value is guaranteed valid, so the rest of
    // the menu code never has to think about bad input.

    private static String readNonBlankLine(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("That can't be blank - try again.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("That's not a whole number - try again.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("That's not a number - try again.");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return LocalDate.parse(line);
            } catch (DateTimeParseException e) {
                System.out.println("Use the format yyyy-MM-dd, e.g. 2024-03-17.");
            }
        }
    }

    private static Department readDepartment() {
        while (true) {
            System.out.print("Department (ENGINEERING, SALES, HR, FINANCE): ");
            String line = scanner.nextLine().trim().toUpperCase();
            try {
                return Department.valueOf(line);
            } catch (IllegalArgumentException e) {
                System.out.println("Not a valid department - try again.");
            }
        }
    }

    // ---------- Sample data ----------

    private static void seedSampleData() {
        try {
            workforceService.addEmployee(new Manager("Aoife Byrne", Department.ENGINEERING,
                    LocalDate.of(2019, 4, 1), 68000, EmployeeStatus.ACTIVE, 5));
            workforceService.addEmployee(new Developer("Conor Walsh", Department.ENGINEERING,
                    LocalDate.of(2022, 9, 12), 52000, EmployeeStatus.ACTIVE, 14));
            workforceService.addEmployee(new Intern("Meera Iyer", Department.HR,
                    LocalDate.of(2025, 6, 2), 1800, EmployeeStatus.ACTIVE));
        } catch (InvalidSalaryException | DuplicateEmployeeIdException e) {
            // This is our own hardcoded sample data, so this should never actually
            // fire. The compiler still makes us handle it either way.
            System.out.println("Could not seed sample data: " + e.getMessage());
        }
    }
}
