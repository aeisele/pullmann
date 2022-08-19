package com.andreaseisele.pullmann.github.error;

import com.andreaseisele.pullmann.error.LightWeightException;

public class GitHubException extends LightWeightException {

    public GitHubException(String message) {
        super(message);
    }

    public GitHubException(String message, Throwable cause) {
        super(message, cause);
    }

}
