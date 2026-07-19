package com.astra.codereview.service.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
public class StaticAnalysisAggregatorService {

    private static final Logger log = LoggerFactory.getLogger(StaticAnalysisAggregatorService.class);

    /**
     * NEW METHOD: Validates that all Java files in the project compile.
     * Returns a CompilationResult immediately if any errors are found.
     * This is called BEFORE static analysis to reject invalid code early.
     */
    public CompilationResult validateCompilation(String projectPath) {
        log.info("Starting compilation validation for project: {}", projectPath);

        try {
            List<String> javaFiles = findAllJavaFiles(projectPath);

            if (javaFiles.isEmpty()) {
                log.warn("No Java files found in project path: {}", projectPath);
                return CompilationResult.failure(List.of("No Java source files (.java) found in project."));
            }

            // Attempt to compile using javac
            CompilationResult result = compileJavaFiles(javaFiles, projectPath);

            if (result.isSuccessful()) {
                log.info("✓ Compilation validation successful for project: {}", projectPath);
            } else {
                log.error("✗ Compilation validation failed for project: {}\nErrors: {}",
                        projectPath, result.getErrorsAsString());
            }

            return result;

        } catch (Exception e) {
            log.error("Exception during compilation validation: {}", e.getMessage(), e);
            return CompilationResult.failure(List.of("Internal error during compilation: " + e.getMessage()));
        }
    }

    /**
     * Finds all .java files in the project directory recursively.
     */
    private List<String> findAllJavaFiles(String projectPath) throws IOException {
        List<String> javaFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(projectPath))) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> javaFiles.add(p.toString()));
        }

        return javaFiles;
    }

    /**
     * Attempts to compile Java files using the Java compiler.
     * Returns CompilationResult with errors if compilation fails.
     */
    private CompilationResult compileJavaFiles(List<String> javaFiles, String projectPath) {
        try {
            // Use javac command to compile
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("javac", "-d", projectPath + "/bin");
            pb.command().addAll(javaFiles);

            Process process = pb.start();

            // Wait for compilation with timeout
            boolean completed = process.waitFor(30, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                return CompilationResult.failure(List.of("Compilation timed out (30 seconds)"));
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                // Read error stream
                String errorOutput = new String(process.getErrorStream().readAllBytes());
                log.error("Compilation error output:\n{}", errorOutput);

                List<String> errors = parseCompilationErrors(errorOutput);
                return CompilationResult.failure(errors);
            }

            return CompilationResult.success();

        } catch (Exception e) {
            log.error("Error running javac: {}", e.getMessage());
            return CompilationResult.failure(List.of("Failed to run Java compiler: " + e.getMessage()));
        }
    }

    /**
     * Parses javac error output to extract meaningful error messages.
     */
    private List<String> parseCompilationErrors(String errorOutput) {
        List<String> errors = new ArrayList<>();

        String[] lines = errorOutput.split("\n");
        for (String line : lines) {
            if (line.contains("error:") || line.contains("ERROR") ||
                    line.matches(".*\\.java:\\d+:.*")) {
                errors.add(line.trim());
            }
        }

        // If no specific errors parsed, return the whole output
        if (errors.isEmpty() && !errorOutput.trim().isEmpty()) {
            errors.add(errorOutput.trim());
        }

        return errors.isEmpty() ? List.of("Unknown compilation error") : errors;
    }

    /**
     * Original analyze method - only called after compilation is validated
     */
    public AggregatedResult analyze(String projectPath) {
        log.info("Starting static analysis for project: {}", projectPath);
        // Original implementation here
        // ...
        return new AggregatedResult(new ArrayList<>());
    }

    // Result class (already exists in your codebase)
    public static class AggregatedResult {
        private final java.util.List<com.astra.codereview.dto.RawFinding> findings;

        public AggregatedResult(java.util.List<com.astra.codereview.dto.RawFinding> findings) {
            this.findings = findings;
        }

        public java.util.List<com.astra.codereview.dto.RawFinding> findings() {
            return findings;
        }
    }
}