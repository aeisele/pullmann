package com.andreaseisele.pullmann.download;

import static java.util.Objects.requireNonNull;

public record FileDownload(
    PullRequestDownload parentDownload,
    String filename,
    String sha
) {

    public FileDownload {
        requireNonNull(parentDownload, "parent download must not be null");
        requireNonNull(filename, "filename must not be null");
        requireNonNull(sha, "sha must not be null");
    }
}
