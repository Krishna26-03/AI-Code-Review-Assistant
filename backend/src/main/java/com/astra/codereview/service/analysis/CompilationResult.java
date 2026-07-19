package com.astra.codereview.service.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of Java compilation validation.
 * Used to determine if code can be analyzed or should be rejected.
 */
public class CompilationResult {

    private final boolean successful;
    private final List<String> errors;
    private final List<String> warnings;

    private CompilationResult(boolean successful, List<String> errors, List<String> warnings) {
        this.successful = successful;
        this.errors = errors;
        this.warnings = warnings;
    }

    public static CompilationResult success() {
        return new CompilationResult(true, new ArrayList<>(), new ArrayList<>());
    }

    public static CompilationResult failure(List<String> errors) {
        return new CompilationResult(false, errors, new ArrayList<>());
    }

    public static CompilationResult withWarnings(List<String> warnings) {
        return new CompilationResult(true, new ArrayList<>(), warnings);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public String getErrorsAsString() {
        return String.join("\n", errors);
    }

    public String getWarningsAsString() {
        return String.join("\n", warnings);
    }

    @Override
    public String toString() {
        return "CompilationResult{" +
                "successful=" + successful +
                ", errors=" + errors.size() +
                ", warnings=" + warnings.size() +
                '}';
    }
}