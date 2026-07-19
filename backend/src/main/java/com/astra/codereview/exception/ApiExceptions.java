package com.astra.codereview.exception;

/** Thrown when a resource (project, review, finding) isn't found or isn't owned by the caller. */
public class ApiExceptions {

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) { super(message); }
    }

    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String message) { super(message); }
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) { super(message); }
    }

    public static class UnsupportedUploadException extends RuntimeException {
        public UnsupportedUploadException(String message) { super(message); }
    }

    public static class AnalysisFailedException extends RuntimeException {
        public AnalysisFailedException(String message) {
            super(message);
        }

        public AnalysisFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
