package com.payroll.model;

import com.payroll.exception.InvalidSalaryException;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Base info every employee has: who they are, what department they're in,
 * when they joined, their base salary, and their current status. How that
 * salary turns into an actual paycheck is up to each subclass, see
 * calculatePay() below.
 */
public abstract class Employee implements Payable, Comparable<Employee> {

    // Starting at 1001 so ids look like real employee numbers, not array indexes.
    private static int nextEmployeeId = 1001;

    private final int id;
    private String name;
    private Department department;
    private final LocalDate dateOfJoining;
    private double baseSalary;
    private EmployeeStatus status;

    protected Employee(String name, Department department, LocalDate dateOfJoining,
                        double baseSalary, EmployeeStatus status) throws InvalidSalaryException {
        this(nextEmployeeId++, name, department, dateOfJoining, baseSalary, status);
    }

    /**
     * Rebuilds an employee that already has an id. Only used by CsvEmployeeImporter,
     * when we're loading employees back in from a file and need to keep their
     * original id instead of handing out a new one. Also bumps the counter above
     * past this id, so anything added normally afterwards can't collide with it.
     */
    protected Employee(int id, String name, Department department, LocalDate dateOfJoining,
                        double baseSalary, EmployeeStatus status) throws InvalidSalaryException {
        if (baseSalary < 0) {
            throw new InvalidSalaryException("Base salary cannot be negative: " + baseSalary);
        }
        this.id = id;
        this.name = name;
        this.department = department;
        this.dateOfJoining = dateOfJoining;
        this.baseSalary = baseSalary;
        this.status = status;

        if (id >= nextEmployeeId) {
            nextEmployeeId = id + 1;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) throws InvalidSalaryException {
        if (baseSalary < 0) {
            throw new InvalidSalaryException("Base salary cannot be negative: " + baseSalary);
        }
        this.baseSalary = baseSalary;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }

    public int getTenureInYears() {
        return Period.between(dateOfJoining, LocalDate.now()).getYears();
    }

    /** Every subclass pays differently, see Manager, Developer, and Intern. */
    public abstract double calculatePay();

    /** Gives us a readable type label without every caller needing instanceof checks. */
    public abstract String getEmployeeType();

    @Override
    public int compareTo(Employee other) {
        // Natural order is tenure, whoever joined earliest comes first.
        return this.dateOfJoining.compareTo(other.dateOfJoining);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Employee)) {
            return false;
        }
        return this.id == ((Employee) other).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("#%d %-20s %-12s %-10s %-9s %,.2f",
                id, name, department, status, getEmployeeType(), calculatePay());
    }
}
