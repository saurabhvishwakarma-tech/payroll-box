package com.payroll.io;

import com.payroll.model.Developer;
import com.payroll.model.Employee;
import com.payroll.model.Manager;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Writes the workforce out as plain CSV. Manager and Developer each have one
 * extra number (team size, overtime hours), so there's a single "extra" column
 * at the end that means something different depending on employeeType. It's
 * left empty for an Intern.
 */
public class CsvEmployeeExporter {

    private static final String HEADER = "id,name,department,dateOfJoining,baseSalary,status,employeeType,extra";

    public void exportToFile(Collection<Employee> employees, String filePath) throws IOException {
        Path path = Path.of(filePath);
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
            writer.println(HEADER);
            for (Employee employee : employees) {
                writer.println(toCsvRow(employee));
            }
        }
    }

    private String toCsvRow(Employee employee) {
        String extra = "";
        if (employee instanceof Manager manager) {
            extra = String.valueOf(manager.getTeamSize());
        } else if (employee instanceof Developer developer) {
            extra = String.valueOf(developer.getOvertimeHours());
        }

        return String.join(",",
                String.valueOf(employee.getId()),
                employee.getName(),
                employee.getDepartment().name(),
                employee.getDateOfJoining().toString(),
                String.valueOf(employee.getBaseSalary()),
                employee.getStatus().name(),
                employee.getEmployeeType(),
                extra);
    }
}
