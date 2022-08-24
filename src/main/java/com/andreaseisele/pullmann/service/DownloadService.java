package com.andreaseisele.pullmann.service;

import com.andreaseisele.pullmann.domain.PullRequestCoordinates;
import com.andreaseisele.pullmann.download.DownloadState;
import com.andreaseisele.pullmann.download.DownloadedFile;
import com.andreaseisele.pullmann.download.FileDownload;
import com.andreaseisele.pullmann.download.FileStore;
import com.andreaseisele.pullmann.download.PullRequestDownload;
import com.andreaseisele.pullmann.github.GitHubClient;
import com.andreaseisele.pullmann.github.dto.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

    private final Logger logger = LoggerFactory.getLogger(DownloadService.class);

    private final GitHubClient gitHubClient;
    private final AsyncTaskExecutor pullRequestDownloadExecutor;
    private final AsyncTaskExecutor fileDownloadExecutor;
    private final FileStore fileStore;

    private final Map<PullRequestDownload, DownloadState> downloads = new ConcurrentHashMap<>();

    public DownloadService(GitHubClient gitHubClient,
                           @Qualifier("pullRequestDownloadExecutor") AsyncTaskExecutor pullRequestDownloadExecutor,
                           @Qualifier("fileDownloadExecutor") AsyncTaskExecutor fileDownloadExecutor,
                           FileStore fileStore) {
        this.gitHubClient = gitHubClient;
        this.pullRequestDownloadExecutor = pullRequestDownloadExecutor;
        this.fileDownloadExecutor = fileDownloadExecutor;
        this.fileStore = fileStore;
    }

    @PostConstruct
    public void init() {
        for (PullRequestDownload download : fileStore.findFinished()) {
            downloads.put(download, DownloadState.FINISHED);
        }
    }

    public void startDownload(PullRequestCoordinates coordinates) {
        final var pullRequest = gitHubClient.pullRequestDetails(coordinates);
        final var headSha = pullRequest.head().sha();

        final var download = new PullRequestDownload(coordinates, headSha);

        var state = downloads.computeIfAbsent(download, k -> {
            pullRequestDownloadExecutor.submit(() -> executeDownload(download));
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
        logger.info("starting new pull request download [{}]", pullRequestDownload);

        final var completionService = new ExecutorCompletionService<DownloadedFile>(fileDownloadExecutor);
        final var downloaded = new ArrayList<DownloadedFile>();
        final var downloadTasks = new ArrayList<Future<DownloadedFile>>();

        var pageOfFiles = gitHubClient.files(pullRequestDownload.coordinates(), 1);
        while (pageOfFiles != null) {
            final var page = pageOfFiles.getPage();
            final var maxPages = pageOfFiles.getMaxPages();

            for (final var file : pageOfFiles.getFiles()) {
                var task = completionService.submit(() -> downloadFile(pullRequestDownload, file));
                downloadTasks.add(task);
            }

            if (page < maxPages) {
                pageOfFiles = gitHubClient.files(pullRequestDownload.coordinates(), page + 1);
            } else {
                pageOfFiles = null;
            }
        }

        try {
            for (int i = 0; i < downloadTasks.size(); i++) {
                final var completedTask = completionService.take();
                downloaded.add(completedTask.get());
            }

            fileStore.zipUp(pullRequestDownload, downloaded);

            logger.debug("pull request download done for [{}]", pullRequestDownload);
            downloads.put(pullRequestDownload, DownloadState.FINISHED);
        } catch (InterruptedException e) {
            logger.warn("interrupted during download -> cancelling");
            Thread.currentThread().interrupt();
            cancelAndTransitionToError(pullRequestDownload, downloadTasks);
        } catch (ExecutionException e) {
            logger.error("download error -> cancelling", e);
            cancelAndTransitionToError(pullRequestDownload, downloadTasks);
            throw launderThrowable(e);
        }
    }

    private DownloadedFile downloadFile(PullRequestDownload pullRequestDownload, File file) {
        logger.debug("starting single file download [{}]", pullRequestDownload);

        final var fileDownload = new FileDownload(pullRequestDownload, file.filename(), file.sha());
        final var target = fileStore.getForFile(fileDownload);
        gitHubClient.downloadFile(file.rawUrl(), target);

        logger.debug("done with single file downloat at [{}]", target);
        return new DownloadedFile(fileDownload, target);
    }

    private void cancelAndTransitionToError(PullRequestDownload pullRequestDownload, List<Future<DownloadedFile>> downloadTasks) {
        downloads.put(pullRequestDownload, DownloadState.ERROR);
        downloadTasks.forEach(task -> task.cancel(true));
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
