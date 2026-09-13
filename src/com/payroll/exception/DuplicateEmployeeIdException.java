package com.payroll.exception;

/**
 * Checked on purpose, so whoever calls addEmployee() has to actually deal with
 * this instead of a duplicate id quietly overwriting someone.
 */
public class DuplicateEmployeeIdException extends Exception {
    public DuplicateEmployeeIdException(String message) {
        super(message);
    }
}
