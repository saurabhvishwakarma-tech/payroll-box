package com.payroll.io;

import com.payroll.exception.InvalidSalaryException;
import com.payroll.exception.MalformedRecordException;
import com.payroll.model.Department;
import com.payroll.model.Developer;
import com.payroll.model.Employee;
import com.payroll.model.EmployeeStatus;
import com.payroll.model.Intern;
import com.payroll.model.Manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads employees back in from a CSV file written by CsvEmployeeExporter.
 *
 * Parsing is just line.split(",", -1), no quoting or escaping. That's fine for
 * this format since none of our columns have commas in them, but it's not a
 * real CSV parser. A name like "Smith, Jane" would break it into the wrong
 * number of fields. A proper parser would handle quoted fields, that's just
 * not what this project is practicing right now.
 *
 * One bad row shouldn't sink the whole import, so each line gets parsed inside
 * its own try/catch. A failure gets recorded with its line number and the loop
 * just moves on to the next row.
 */
public class CsvEmployeeImporter {

    private static final int EXPECTED_FIELD_COUNT = 8;

    public ImportResult importFromFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filePath));
        ImportResult result = new ImportResult();
        Set<Integer> idsSeenThisImport = new HashSet<>();

        // Skip the header row, everything else is one employee per line.
        for (int lineNumber = 2; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1);
            if (line.isBlank()) {
                continue;
            }
            try {
                Employee employee = parseLine(line, lineNumber);
                if (!idsSeenThisImport.add(employee.getId())) {
                    throw new MalformedRecordException(lineNumber,
                            "duplicate id " + employee.getId() + " within this file");
                }
                result.addImported(employee);
            } catch (MalformedRecordException e) {
                result.addFailure(e.getMessage());
            }
        }
        return result;
    }

    private Employee parseLine(String line, int lineNumber) throws MalformedRecordException {
        String[] fields = line.split(",", -1);
        if (fields.length != EXPECTED_FIELD_COUNT) {
            throw new MalformedRecordException(lineNumber,
                    "expected " + EXPECTED_FIELD_COUNT + " fields but found " + fields.length);
        }

        try {
            int id = Integer.parseInt(fields[0].trim());
            String name = fields[1].trim();
            Department department = Department.valueOf(fields[2].trim().toUpperCase());
            LocalDate dateOfJoining = LocalDate.parse(fields[3].trim());
            double baseSalary = Double.parseDouble(fields[4].trim());
            EmployeeStatus status = EmployeeStatus.valueOf(fields[5].trim().toUpperCase());
            String employeeType = fields[6].trim().toUpperCase();
            String extra = fields[7].trim();

            if (name.isEmpty()) {
                throw new MalformedRecordException(lineNumber, "name cannot be blank");
            }

            return switch (employeeType) {
                case "MANAGER" -> new Manager(id, name, department, dateOfJoining, baseSalary, status,
                        parseIntOrFail(extra, lineNumber, "team size"));
                case "DEVELOPER" -> new Developer(id, name, department, dateOfJoining, baseSalary, status,
                        parseDoubleOrFail(extra, lineNumber, "overtime hours"));
                case "INTERN" -> new Intern(id, name, department, dateOfJoining, baseSalary, status);
                default -> throw new MalformedRecordException(lineNumber,
                        "unknown employee type '" + employeeType + "'");
            };
        } catch (NumberFormatException e) {
            throw new MalformedRecordException(lineNumber, "could not parse a number - " + e.getMessage());
        } catch (DateTimeParseException e) {
            throw new MalformedRecordException(lineNumber,
                    "invalid date '" + fields[3].trim() + "', expected yyyy-MM-dd");
        } catch (IllegalArgumentException e) {
            throw new MalformedRecordException(lineNumber, "invalid department or status - " + e.getMessage());
        } catch (InvalidSalaryException e) {
            throw new MalformedRecordException(lineNumber, e.getMessage());
        }
    }

    private int parseIntOrFail(String value, int lineNumber, String fieldName) throws MalformedRecordException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new MalformedRecordException(lineNumber, "invalid " + fieldName + " '" + value + "'");
        }
    }

    private double parseDoubleOrFail(String value, int lineNumber, String fieldName) throws MalformedRecordException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new MalformedRecordException(lineNumber, "invalid " + fieldName + " '" + value + "'");
        }
    }
}
