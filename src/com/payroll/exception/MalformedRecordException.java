package com.payroll.exception;

/** One bad row in a CSV import. Keeps the line number so the error actually helps. */
public class MalformedRecordException extends Exception {

    private final int lineNumber;

    public MalformedRecordException(int lineNumber, String reason) {
        super("Line " + lineNumber + ": " + reason);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
