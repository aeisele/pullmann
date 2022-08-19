package com.andreaseisele.pullmann.github.error;

public class GitHubException extends RuntimeException {

    public GitHubException(String message) {
        super(message);
    }

    public GitHubException(String message, Throwable cause) {
        super(message, cause);
    }

    // lightweight exception
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
