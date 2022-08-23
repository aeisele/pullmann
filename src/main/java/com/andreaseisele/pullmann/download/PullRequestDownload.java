package com.andreaseisele.pullmann.download;

import static java.util.Objects.requireNonNull;


import com.andreaseisele.pullmann.domain.PullRequestCoordinates;

public record PullRequestDownload(PullRequestCoordinates coordinates, String headSha) {

    public PullRequestDownload {
        requireNonNull(coordinates, "coordinates must not be null");
        requireNonNull(headSha, "headSha must not be null");
    }

}
