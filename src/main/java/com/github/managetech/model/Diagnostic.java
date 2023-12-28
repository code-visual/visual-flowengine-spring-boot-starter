package com.github.managetech.model;

public class Diagnostic {
    private int startLineNumber;
    private int startColumn;
    private int endLineNumber;
    private int endColumn;
    private String message;


    // Getters and Setters
    public int getStartLineNumber() {
        return startLineNumber;
    }

    public void setStartLineNumber(int startLineNumber) {
        this.startLineNumber = startLineNumber;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }

    public int getEndLineNumber() {
        return endLineNumber;
    }

    public void setEndLineNumber(int endLineNumber) {
        this.endLineNumber = endLineNumber;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Optional: Override toString method for easy printing
    @Override
    public String toString() {
        return "Diagnostic{" +
                "startLineNumber=" + startLineNumber +
                ", startColumn=" + startColumn +
                ", endLineNumber=" + endLineNumber +
                ", endColumn=" + endColumn +
                ", message='" + message + '\'' +
                '}';
    }
}
