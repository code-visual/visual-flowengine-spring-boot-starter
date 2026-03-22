/*
 * Copyright (c) 2023-2024, levi li (levi.lideng@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.code.visual.model;

public class Diagnostic implements java.io.Serializable {
    /** Monaco MarkerSeverity: 1=Hint, 2=Info, 4=Warning, 8=Error */
    public static final int SEVERITY_HINT = 1;
    public static final int SEVERITY_INFO = 2;
    public static final int SEVERITY_WARNING = 4;
    public static final int SEVERITY_ERROR = 8;

    private int startLineNumber;
    private int startColumn;
    private int endLineNumber;
    private int endColumn;
    private String message;
    private int severity = SEVERITY_ERROR;


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

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        return "Diagnostic{" +
                "startLineNumber=" + startLineNumber +
                ", startColumn=" + startColumn +
                ", endLineNumber=" + endLineNumber +
                ", endColumn=" + endColumn +
                ", message='" + message + '\'' +
                ", severity=" + severity +
                '}';
    }
}
