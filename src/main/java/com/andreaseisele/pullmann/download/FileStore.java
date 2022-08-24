package com.andreaseisele.pullmann.download;

import com.andreaseisele.pullmann.domain.PullRequestCoordinates;
import com.andreaseisele.pullmann.domain.RepositoryName;
import com.andreaseisele.pullmann.github.GitHubProperties;
import com.andreaseisele.pullmann.github.error.GitHubStorageException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileStore {

    private static final Logger logger = LoggerFactory.getLogger(FileStore.class);

    private static final String DIR_DOWNLOAD = "downloads";
    private static final String DIR_PRS = "pulls";
    private static final String DIR_OBJECTS = "objects";
    private static final String FILE_PR_ZIP = "pr.zip";
    private static final String FILE_OBJECT = "blob.bin";

    private static final int OFFSET_PR_HEAD_SHA = -2;
    private static final int OFFSET_PR_NUMBER = -3;
    private static final int OFFSET_PR_REPO = -5;
    private static final int OFFSET_PR_OWNER = -6;

    private final GitHubProperties gitHubProperties;

    private final Path root;

    public FileStore(GitHubProperties gitHubProperties) {
        this.gitHubProperties = gitHubProperties;
        this.root = initRootDirectory();
    }

    // <root>/downloads/<owner>/<repo>/pulls/<number>/<head sha>/objects/<file sha>/blob.bin
    public Path getForFile(FileDownload fileDownload) {
        final var pullRequestDownload = fileDownload.parentDownload();

        final Path directory = resolvePrRoot(pullRequestDownload)
            .resolve(DIR_OBJECTS)
            .resolve(fileDownload.sha());

        return mkdirs(directory).resolve(FILE_OBJECT);
    }

    // <root>/downloads/<owner>/<repo>/pulls/<number>/<head sha>/pr.zip
    public Path getForPullRequest(PullRequestDownload pullRequestDownload) {
        return resolvePrRoot(pullRequestDownload).resolve(FILE_PR_ZIP);
    }

    public void zipUp(PullRequestDownload pullRequestDownload, List<DownloadedFile> downloaded) {
        logger.debug("starting zip file construction [{}]", pullRequestDownload);

        final var target = getForPullRequest(pullRequestDownload);
        try (var fos = Files.newOutputStream(target);
             var zos = new ZipOutputStream(fos)) {
            for (var file : downloaded) {
                addFile(zos, file);
            }
        } catch (IOException e) {
            throw new GitHubStorageException("error zipping up files", e);
        }

        logger.debug("done with zip file construction at [{}]", target);
    }

    public void removeZip(PullRequestDownload pullRequestDownload) {
        logger.info("attempting to remove pull request zip [{}]", pullRequestDownload);

        final var target = getForPullRequest(pullRequestDownload);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            logger.warn("unable to delete pull request zip", e);
        }
    }

    public List<PullRequestDownload> findFinished() {
        final var downloadDirectory = root.resolve(DIR_DOWNLOAD);
        logger.info("looking for finished downloads in [{}]", downloadDirectory);

        if (!Files.exists(downloadDirectory)) {
            logger.warn("download directory [{}] does not exist (yet)", downloadDirectory);
            return Collections.emptyList();
        }

        try (final var pathStream = Files.find(downloadDirectory, 6, (path, attrs) -> path.endsWith(FILE_PR_ZIP))) {
            return pathStream
                .map(FileStore::tryReconstructDownload)
                .flatMap(Optional::stream)
                .toList();
        } catch (IOException e) {
            throw new GitHubStorageException("error reconstructing downloads from disk", e);
        }
    }

    private void addFile(ZipOutputStream zos, DownloadedFile file) throws IOException {
        logger.debug("adding file to zip [{}]", file);

        var fileEntry = new ZipEntry(file.download().filename());
        zos.putNextEntry(fileEntry);
        Files.copy(file.path(), zos);
        zos.closeEntry();
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

    static Optional<PullRequestDownload> tryReconstructDownload(Path zipPath) {
        try {
            final var nameCount = zipPath.getNameCount();
            final var headSha = zipPath.getName(nameCount + OFFSET_PR_HEAD_SHA).toString();
            final var number = zipPath.getName(nameCount + OFFSET_PR_NUMBER).toString();
            final var repository = zipPath.getName(nameCount + OFFSET_PR_REPO).toString();
            final var owner = zipPath.getName(nameCount + OFFSET_PR_OWNER).toString();

            final var repositoryName = new RepositoryName(owner, repository);
            final var coordinates = new PullRequestCoordinates(repositoryName, Long.parseLong(number));
            return Optional.of(new PullRequestDownload(coordinates, headSha));
        } catch (RuntimeException rt) {
            logger.error("unable to reconstruct download from zip path [{}]", zipPath);
            return Optional.empty();
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
