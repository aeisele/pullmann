package com.andreaseisele.pullmann.download;

import static java.util.Objects.requireNonNull;


import java.nio.file.Path;

public record DownloadedFile(
    FileDownload download,
    Path path
) {

    public DownloadedFile {
        requireNonNull(download, "download must not be null");
        requireNonNull(path, "path must not be null");
    }

}

