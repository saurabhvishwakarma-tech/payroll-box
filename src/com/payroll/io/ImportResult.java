package com.payroll.io;

import com.payroll.model.Employee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Everything that happened during one CSV import: the employees that parsed
 * fine, and a plain-English reason for every row that didn't. Nothing here
 * throws, a bad row just gets recorded instead of blowing up the whole import.
 */
public class ImportResult {

    private final List<Employee> importedEmployees = new ArrayList<>();
    private final List<String> failures = new ArrayList<>();

    void addImported(Employee employee) {
        importedEmployees.add(employee);
    }

    void addFailure(String reason) {
        failures.add(reason);
    }

    public List<Employee> getImportedEmployees() {
        return Collections.unmodifiableList(importedEmployees);
    }

    public List<String> getFailures() {
        return Collections.unmodifiableList(failures);
    }

    public int successCount() {
        return importedEmployees.size();
    }

    public int failureCount() {
        return failures.size();
    }
}
