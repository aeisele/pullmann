package com.andreaseisele.pullmann.github.error;

public class GitHubHttpStatusException extends LightWeightException {

    private final int httpStatus;

    public GitHubHttpStatusException(int httpStatus, String message) {
        super("HTTP Status " + httpStatus + ": " + message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

}
