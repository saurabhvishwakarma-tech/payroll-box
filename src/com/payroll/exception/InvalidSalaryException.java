package com.payroll.exception;

/** Thrown when a salary doesn't make sense. Right now that just means negative. */
public class InvalidSalaryException extends Exception {
    public InvalidSalaryException(String message) {
        super(message);
    }
}
