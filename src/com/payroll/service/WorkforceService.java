package com.payroll.service;

import com.payroll.exception.DuplicateEmployeeIdException;
import com.payroll.exception.EmployeeNotFoundException;
import com.payroll.model.Employee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the workforce in memory and does the basic CRUD.
 *
 * Uses a Map keyed by id instead of a List. Almost everything here boils down
 * to "find the employee with this id", and a Map answers that instantly instead
 * of scanning the whole list every time. LinkedHashMap specifically, so listAll()
 * comes back in the order people were added instead of random hash order.
 */
public class WorkforceService {

    private final Map<Integer, Employee> employeesById = new LinkedHashMap<>();

    public void addEmployee(Employee employee) throws DuplicateEmployeeIdException {
        if (employeesById.containsKey(employee.getId())) {
            throw new DuplicateEmployeeIdException(
                    "An employee with id " + employee.getId() + " already exists.");
        }
        employeesById.put(employee.getId(), employee);
    }

    public Employee removeEmployee(int id) throws EmployeeNotFoundException {
        Employee removed = employeesById.remove(id);
        if (removed == null) {
            throw new EmployeeNotFoundException("No employee found with id " + id);
        }
        return removed;
    }

    public Employee findById(int id) throws EmployeeNotFoundException {
        Employee employee = employeesById.get(id);
        if (employee == null) {
            throw new EmployeeNotFoundException("No employee found with id " + id);
        }
        return employee;
    }

    public List<Employee> listAll() {
        return Collections.unmodifiableList(new ArrayList<>(employeesById.values()));
    }

    public int size() {
        return employeesById.size();
    }

    /** Comparable: Employee's natural order is tenure, Collections.sort() just uses it. */
    public List<Employee> sortedByTenure() {
        List<Employee> employees = new ArrayList<>(employeesById.values());
        Collections.sort(employees);
        return employees;
    }

    /**
     * Comparator: salary isn't Employee's natural order, so this ordering lives
     * here instead of inside the class. We can add more of these later without
     * ever touching Employee itself.
     */
    public List<Employee> sortedBySalary() {
        List<Employee> employees = new ArrayList<>(employeesById.values());
        employees.sort(Comparator.comparingDouble(Employee::getBaseSalary).reversed());
        return employees;
    }
}
