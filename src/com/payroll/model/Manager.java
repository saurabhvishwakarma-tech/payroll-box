package com.payroll.model;

import com.payroll.exception.InvalidSalaryException;

import java.time.LocalDate;

/** Pay = base salary plus a flat bonus per person on the team. */
public class Manager extends Employee {

    private static final double BONUS_PER_TEAM_MEMBER = 150.0;

    private int teamSize;

    public Manager(String name, Department department, LocalDate dateOfJoining,
                   double baseSalary, EmployeeStatus status, int teamSize) throws InvalidSalaryException {
        super(name, department, dateOfJoining, baseSalary, status);
        this.teamSize = Math.max(teamSize, 0);
    }

    /** Used by CsvEmployeeImporter to rebuild a Manager that already has an id */
    public Manager(int id, String name, Department department, LocalDate dateOfJoining,
                   double baseSalary, EmployeeStatus status, int teamSize) throws InvalidSalaryException {
        super(id, name, department, dateOfJoining, baseSalary, status);
        this.teamSize = Math.max(teamSize, 0);
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = Math.max(teamSize, 0);
    }

    @Override
    public double calculatePay() {
        return getBaseSalary() + (teamSize * BONUS_PER_TEAM_MEMBER);
    }

    @Override
    public String getEmployeeType() {
        return "MANAGER";
    }
}
