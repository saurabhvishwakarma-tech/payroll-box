package com.payroll.service;

import com.payroll.model.Department;
import com.payroll.model.Employee;
import com.payroll.model.EmployeeStatus;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Read-only reports over whatever WorkforceService currently holds. */
public class ReportingService {

    private final WorkforceService workforceService;

    public ReportingService(WorkforceService workforceService) {
        this.workforceService = workforceService;
    }

    public Map<Department, Double> totalSalaryByDepartment() {
        return workforceService.listAll().stream()
                .collect(Collectors.groupingBy(Employee::getDepartment,
                        Collectors.summingDouble(Employee::getBaseSalary)));
    }

    public Map<Department, Double> averageSalaryByDepartment() {
        return workforceService.listAll().stream()
                .collect(Collectors.groupingBy(Employee::getDepartment,
                        Collectors.averagingDouble(Employee::getBaseSalary)));
    }

    public List<Employee> topEarnersOverall(int n) {
        return workforceService.listAll().stream()
                .sorted(Comparator.comparingDouble(Employee::calculatePay).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    public Map<Department, List<Employee>> topEarnersByDepartment(int n) {
        Map<Department, List<Employee>> byDepartment = workforceService.listAll().stream()
                .collect(Collectors.groupingBy(Employee::getDepartment));

        Map<Department, List<Employee>> topByDepartment = new EnumMap<>(Department.class);
        for (Map.Entry<Department, List<Employee>> entry : byDepartment.entrySet()) {
            List<Employee> top = entry.getValue().stream()
                    .sorted(Comparator.comparingDouble(Employee::calculatePay).reversed())
                    .limit(n)
                    .collect(Collectors.toList());
            topByDepartment.put(entry.getKey(), top);
        }
        return topByDepartment;
    }

    public Map<EmployeeStatus, List<Employee>> groupByStatus() {
        return workforceService.listAll().stream()
                .collect(Collectors.groupingBy(Employee::getStatus));
    }

    public Map<String, List<Employee>> groupByTenureBracket() {
        return workforceService.listAll().stream()
                .collect(Collectors.groupingBy(this::tenureBracket));
    }

    private String tenureBracket(Employee employee) {
        int years = employee.getTenureInYears();
        if (years < 1) {
            return "<1yr";
        }
        if (years <= 3) {
            return "1-3yr";
        }
        return "3+yr";
    }

    /**
     * Chains a few stream operations back to back: keep only active employees,
     * order by pay (highest first), keep the top n, collect into a list.
     * filter, then sorted, then limit, then collect, all in one pipeline.
     */
    public List<Employee> topActiveEarners(int n) {
        return workforceService.listAll().stream()
                .filter(employee -> employee.getStatus() == EmployeeStatus.ACTIVE)
                .sorted(Comparator.comparingDouble(Employee::calculatePay).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }
}
