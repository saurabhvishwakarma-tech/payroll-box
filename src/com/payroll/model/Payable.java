package com.payroll.model;

/**
 * Anything that gets paid implements this. It's a bit redundant with Employee
 * right now, but "how pay gets calculated" is really its own concern, separate
 * from "what an employee is". If a non-Employee payable ever shows up, like a
 * contractor billed differently, there's already a place for it to plug in.
 */
public interface Payable {
    double calculatePay();
}
