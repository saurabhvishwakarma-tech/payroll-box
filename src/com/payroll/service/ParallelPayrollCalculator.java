package com.payroll.service;

import com.payroll.model.Department;
import com.payroll.model.Employee;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Works out total pay per department using one thread per department instead
 * of doing them one after another. Each thread only reads employee data,
 * calculatePay() doesn't change anything, and hands its own answer back
 * through a Future instead of writing into some shared map. That's what keeps
 * this safe without needing a single synchronized block: there's nothing
 * multiple threads are writing to at the same time.
 *
 * If this were written differently, say every thread writing straight into
 * one shared HashMap as it finished, that map would need to be a
 * ConcurrentHashMap or wrapped in synchronized, otherwise two threads writing
 * at once can corrupt it. The other real piece of shared state in this
 * project is Employee's static nextEmployeeId counter. Only the single
 * threaded CLI creates employees right now, so it's fine as is, but if
 * employees were ever created from multiple threads at once, that counter
 * would need to become an AtomicInteger (or be wrapped in synchronized) so
 * two threads can't hand out the same id at the same time.
 */
public class ParallelPayrollCalculator {

    public Map<Department, Double> calculateTotalPayByDepartment(WorkforceService workforceService)
            throws InterruptedException, ExecutionException {

        Map<Department, List<Employee>> byDepartment = workforceService.listAll().stream()
                .collect(Collectors.groupingBy(Employee::getDepartment));

        if (byDepartment.isEmpty()) {
            return new EnumMap<>(Department.class);
        }

        ExecutorService executor = Executors.newFixedThreadPool(byDepartment.size());
        try {
            Map<Department, Future<Double>> pendingTotals = new EnumMap<>(Department.class);

            for (Map.Entry<Department, List<Employee>> entry : byDepartment.entrySet()) {
                List<Employee> employeesInDepartment = entry.getValue();
                Callable<Double> sumPayForDepartment = () -> employeesInDepartment.stream()
                        .mapToDouble(Employee::calculatePay)
                        .sum();
                pendingTotals.put(entry.getKey(), executor.submit(sumPayForDepartment));
            }

            Map<Department, Double> totals = new EnumMap<>(Department.class);
            for (Map.Entry<Department, Future<Double>> entry : pendingTotals.entrySet()) {
                totals.put(entry.getKey(), entry.getValue().get());
            }
            return totals;
        } finally {
            executor.shutdown();
        }
    }
}
