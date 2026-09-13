package com.payroll.service;

import com.payroll.exception.DuplicateEmployeeIdException;
import com.payroll.exception.EmployeeNotFoundException;
import com.payroll.model.Employee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Holds the workforce and does the basic CRUD, but doesn't store anything
 * itself anymore. That job now belongs to a generic Repository<Employee>
 * underneath, keyed off Employee::getId. This class has turned into a thin,
 * Employee-flavored wrapper around that repository: it still throws the same
 * DuplicateEmployeeIdException / EmployeeNotFoundException as before, and
 * every method signature is unchanged, so nothing outside this class needed
 * to know the refactor happened at all.
 */
public class WorkforceService {

    private final Repository<Employee> repository = new Repository<>(Employee::getId);

    public void addEmployee(Employee employee) throws DuplicateEmployeeIdException {
        boolean added = repository.add(employee);
        if (!added) {
            throw new DuplicateEmployeeIdException(
                    "An employee with id " + employee.getId() + " already exists.");
        }
    }

    public Employee removeEmployee(int id) throws EmployeeNotFoundException {
        Employee removed = repository.remove(id);
        if (removed == null) {
            throw new EmployeeNotFoundException("No employee found with id " + id);
        }
        return removed;
    }

    public Employee findById(int id) throws EmployeeNotFoundException {
        Employee employee = repository.findById(id);
        if (employee == null) {
            throw new EmployeeNotFoundException("No employee found with id " + id);
        }
        return employee;
    }

    public List<Employee> listAll() {
        return Collections.unmodifiableList(repository.findAll());
    }

    public int size() {
        return repository.size();
    }

    /** Comparable: Employee's natural order is tenure, Collections.sort() just uses it. */
    public List<Employee> sortedByTenure() {
        List<Employee> employees = new ArrayList<>(repository.findAll());
        Collections.sort(employees);
        return employees;
    }

    /**
     * Comparator: salary isn't Employee's natural order, so this ordering lives
     * here instead of inside the class. We can add more of these later without
     * ever touching Employee itself.
     */
    public List<Employee> sortedBySalary() {
        List<Employee> employees = new ArrayList<>(repository.findAll());
        employees.sort(Comparator.comparingDouble(Employee::getBaseSalary).reversed());
        return employees;
    }
}
