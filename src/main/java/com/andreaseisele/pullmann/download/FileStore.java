package com.andreaseisele.pullmann.download;

import com.andreaseisele.pullmann.github.GitHubProperties;
import com.andreaseisele.pullmann.github.error.GitHubStorageException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

@Component
public class FileStore {

    private static final String DIR_DOWNLOAD = "downloads";
    private static final String DIR_PRS = "pulls";
    private static final String DIR_OBJECTS = "objects";
    private static final String FILE_PR_ZIP = "pr.zip";

    private final GitHubProperties gitHubProperties;

    private final Path root;

    public FileStore(GitHubProperties gitHubProperties) {
        this.gitHubProperties = gitHubProperties;
        this.root = initRootDirectory();
    }

    // <root>/downloads/<owner>/<repo>/pulls/<number>/<head sha>/objects/<file sha>/<filename>
    public Path getForFile(FileDownload fileDownload) {
        final var pullRequestDownload = fileDownload.parentDownload();

        final Path directory = resolvePrRoot(pullRequestDownload)
            .resolve(DIR_OBJECTS)
            .resolve(fileDownload.sha());

        return mkdirs(directory).resolve(fileDownload.filename());
    }

    // <root>/downloads/<owner>/<repo>/pulls/<number>/<head sha>/pr.zip
    public Path getForPullRequest(PullRequestDownload pullRequestDownload) {
        return resolvePrRoot(pullRequestDownload).resolve(FILE_PR_ZIP);
    }
    private Path resolvePrRoot(PullRequestDownload pullRequestDownload) {
        final var coordinates = pullRequestDownload.coordinates();

        final Path directory = root.resolve(DIR_DOWNLOAD)
            .resolve(coordinates.repositoryName().owner())
            .resolve(coordinates.repositoryName().repository())
            .resolve(DIR_PRS)
            .resolve(String.valueOf(coordinates.number()))
            .resolve(pullRequestDownload.headSha());

        return mkdirs(directory);
    }

    private Path initRootDirectory() {
        final var location = gitHubProperties.getDownload().getLocation();
        try {
            return Files.createDirectories(location.getFile().toPath());
        } catch (IOException e) {
            throw new GitHubStorageException("error initializing download root location", e);
        }
    }

    private static Path mkdirs(Path directory) {
        try {
            return Files.createDirectories(directory);
        } catch (IOException e) {
            throw new GitHubStorageException("error creating parent directory for file", e);
        }
    }

}
