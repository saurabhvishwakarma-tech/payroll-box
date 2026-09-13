package com.payroll.model;

import com.payroll.exception.InvalidSalaryException;

import java.time.LocalDate;

/**
 * Pay = base salary plus overtime. There's no separate "overtime rate" field,
 * the hourly rate is worked out from base salary itself (salary over a standard
 * 160 hour month), then paid at 1.5x for every overtime hour logged. Keeps
 * everything tied to one number instead of two salary fields that could drift
 * apart.
 */
public class Developer extends Employee {

    private static final double STANDARD_MONTHLY_HOURS = 160.0;
    private static final double OVERTIME_MULTIPLIER = 1.5;

    private double overtimeHours;

    public Developer(String name, Department department, LocalDate dateOfJoining,
                      double baseSalary, EmployeeStatus status, double overtimeHours) throws InvalidSalaryException {
        super(name, department, dateOfJoining, baseSalary, status);
        this.overtimeHours = Math.max(overtimeHours, 0);
    }

    /** Used by CsvEmployeeImporter to rebuild a Developer that already has an id */
    public Developer(int id, String name, Department department, LocalDate dateOfJoining,
                      double baseSalary, EmployeeStatus status, double overtimeHours) throws InvalidSalaryException {
        super(id, name, department, dateOfJoining, baseSalary, status);
        this.overtimeHours = Math.max(overtimeHours, 0);
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    /** Hours get logged bit by bit over a pay period rather than set all at once. */
    public void logOvertimeHours(double additionalHours) {
        if (additionalHours > 0) {
            this.overtimeHours += additionalHours;
        }
    }

    /** Call this after payroll runs so the next period starts at zero. */
    public void resetOvertimeHours() {
        this.overtimeHours = 0;
    }

    private double getHourlyRate() {
        return getBaseSalary() / STANDARD_MONTHLY_HOURS;
    }

    @Override
    public double calculatePay() {
        return getBaseSalary() + (overtimeHours * getHourlyRate() * OVERTIME_MULTIPLIER);
    }

    @Override
    public String getEmployeeType() {
        return "DEVELOPER";
    }
}
