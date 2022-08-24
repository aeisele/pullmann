package com.andreaseisele.pullmann.github.error;

public class GitHubDownloadException extends GitHubException {

    public GitHubDownloadException(String message) {
        super(message);
    }

    public GitHubDownloadException(String message, Throwable cause) {
        super(message, cause);
    }

}
