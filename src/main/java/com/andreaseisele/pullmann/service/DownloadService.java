package com.andreaseisele.pullmann.service;

import com.andreaseisele.pullmann.domain.PullRequestCoordinates;
import com.andreaseisele.pullmann.download.DownloadState;
import com.andreaseisele.pullmann.download.FileDownload;
import com.andreaseisele.pullmann.download.FileStore;
import com.andreaseisele.pullmann.download.PullRequestDownload;
import com.andreaseisele.pullmann.github.GitHubClient;
import com.andreaseisele.pullmann.github.dto.File;
import com.andreaseisele.pullmann.github.error.GitHubStorageException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

    private final Logger logger = LoggerFactory.getLogger(DownloadService.class);

    private final GitHubClient gitHubClient;
    private final AsyncTaskExecutor executor;
    private final FileStore fileStore;

    private final Map<PullRequestDownload, DownloadState> downloads = new ConcurrentHashMap<>();

    public DownloadService(GitHubClient gitHubClient,
                           @Qualifier("downloadExecutor") AsyncTaskExecutor executor,
                           FileStore fileStore) {
        this.gitHubClient = gitHubClient;
        this.executor = executor;
        this.fileStore = fileStore;
    }

    public void startDownload(PullRequestCoordinates coordinates) {
        final var pullRequest = gitHubClient.pullRequestDetails(coordinates);
        final var headSha = pullRequest.head().sha();

        final var download = new PullRequestDownload(coordinates, headSha);

        var state = downloads.computeIfAbsent(download, k -> {
            logger.info("starting new pull request download [{}]", download);
            executor.submit(() -> executeDownload(download));
            return DownloadState.RUNNING;
        });

        if (state == DownloadState.FINISHED) {
            logger.info("pull request download already done [{}]", download);
        } else if (state == DownloadState.ERROR) {
            logger.info("pull request download is in error state [{}]", download);
        }
    }

    public Map<PullRequestDownload, DownloadState> getDownloads() {
        return Map.copyOf(downloads);
    }

    private void executeDownload(PullRequestDownload pullRequestDownload) {
        final var completionService = new ExecutorCompletionService<Path>(executor);
        final var downloaded = new ArrayList<Path>();
        int files = 0;

        var pageOfFiles = gitHubClient.files(pullRequestDownload.coordinates(), 1);
        while (pageOfFiles != null) {
            final var page = pageOfFiles.getPage();
            final var maxPages = pageOfFiles.getMaxPages();

            for (final var file : pageOfFiles.getFiles()) {
                files++;
                completionService.submit(() -> downloadFile(pullRequestDownload, file));
            }

            if (page < maxPages) {
                pageOfFiles = gitHubClient.files(pullRequestDownload.coordinates(), page + 1);
            } else {
                pageOfFiles = null;
            }
        }

        try {
            for (int i = 0; i < files; i++) {
                final var future = completionService.take();
                downloaded.add(future.get());
            }
        } catch (InterruptedException e) {
            logger.warn("interrupted during download");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw launderThrowable(e);
        }

        zipUp(pullRequestDownload, downloaded);

        logger.debug("pull request download done for [{}]", pullRequestDownload);
        downloads.put(pullRequestDownload, DownloadState.FINISHED);
    }

    private Path downloadFile(PullRequestDownload pullRequestDownload, File file) {
        logger.debug("starting single file download [{}]", pullRequestDownload);

        final var fileDownload = new FileDownload(pullRequestDownload, file.filename(), file.sha());
        final var target = fileStore.getForFile(fileDownload);
        gitHubClient.downloadFile(file.rawUrl(), target);

        logger.debug("done with single file downloat at [{}]", target);
        return target;
    }

    private void zipUp(PullRequestDownload pullRequestDownload, List<Path> downloaded) {
        logger.debug("starting zip file construction [{}]", pullRequestDownload);

        final var target = fileStore.getForPullRequest(pullRequestDownload);
        try (var fos = Files.newOutputStream(target);
             var zos = new ZipOutputStream(fos)) {
            for (var filePath : downloaded) {
                addFile(zos, filePath);
            }
        } catch (IOException e) {
            throw new GitHubStorageException("error zipping up files", e);
        }

        logger.debug("done with zip file construction at [{}]", target);
    }

    private void addFile(ZipOutputStream zos, Path filePath) throws IOException {
        logger.debug("adding file to zip [{}]", filePath);

        final var parentName = filePath.getParent().getFileName().toString();
        zos.putNextEntry(new ZipEntry(parentName + "/"));
        zos.closeEntry();

        var fileEntry = new ZipEntry(parentName + "/" + filePath.getFileName().toString());
        zos.putNextEntry(fileEntry);
        Files.copy(filePath, zos);
        zos.closeEntry();
    }

    private static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException rt)
            return rt;
        else if (t instanceof Error err)
            throw err;
        else
            throw new IllegalStateException("Not unchecked", t);
    }

}
