package com.payroll.exception;

/** Thrown when you look up or remove an id that isn't in the workforce. */
public class EmployeeNotFoundException extends Exception {
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
