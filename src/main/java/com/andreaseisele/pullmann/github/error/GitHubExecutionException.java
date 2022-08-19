package com.andreaseisele.pullmann.github.error;

public class GitHubExecutionException extends GitHubException {

    public GitHubExecutionException(String message) {
        super(message);
    }

    public GitHubExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

}
