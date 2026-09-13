package com.payroll.model;

import com.payroll.exception.InvalidSalaryException;

import java.time.LocalDate;

/** Flat stipend, nothing else. baseSalary is the stipend, no bonus or overtime on top. */
public class Intern extends Employee {

    public Intern(String name, Department department, LocalDate dateOfJoining,
                  double baseSalary, EmployeeStatus status) throws InvalidSalaryException {
        super(name, department, dateOfJoining, baseSalary, status);
    }

    /** Used by CsvEmployeeImporter to rebuild an Intern that already has an id */
    public Intern(int id, String name, Department department, LocalDate dateOfJoining,
                  double baseSalary, EmployeeStatus status) throws InvalidSalaryException {
        super(id, name, department, dateOfJoining, baseSalary, status);
    }

    @Override
    public double calculatePay() {
        return getBaseSalary();
    }

    @Override
    public String getEmployeeType() {
        return "INTERN";
    }
}
