package com.andreaseisele.pullmann.service;

import com.andreaseisele.pullmann.domain.PullRequestCoordinates;
import com.andreaseisele.pullmann.download.DownloadState;
import com.andreaseisele.pullmann.download.FileStore;
import com.andreaseisele.pullmann.download.PullRequestDownload;
import com.andreaseisele.pullmann.github.GitHubClient;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class DownloadService {

    private final Logger logger = LoggerFactory.getLogger(DownloadService.class);

    private final GitHubClient gitHubClient;
    private final AsyncTaskExecutor pullRequestDownloadExecutor;
    private final FileStore fileStore;

    private final Map<PullRequestDownload, DownloadState> downloads = new ConcurrentHashMap<>();

    private final Set<SseEmitter> eventEmitters = ConcurrentHashMap.newKeySet();

    public DownloadService(GitHubClient gitHubClient,
                           @Qualifier("pullRequestDownloadExecutor") AsyncTaskExecutor pullRequestDownloadExecutor,
                           FileStore fileStore) {
        this.gitHubClient = gitHubClient;
        this.pullRequestDownloadExecutor = pullRequestDownloadExecutor;
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
        } else if (state == DownloadState.RUNNING) {
            emitEvent();
        }
    }

    public Map<PullRequestDownload, DownloadState> getDownloads() {
        return Map.copyOf(downloads);
    }

    public Optional<Path> findZip(PullRequestDownload download) {
        return fileStore.findZip(download);
    }

    public void deleteZip(PullRequestDownload download) {
        downloads.computeIfPresent(download, (k, state) -> {
            if (state != DownloadState.RUNNING) {
                fileStore.deleteZip(download);
                emitEvent();
                return null;
            }
            return state;
        });
    }

    public void registerEmitter(SseEmitter emitter) {
        emitter.onCompletion(() -> eventEmitters.remove(emitter));
        emitter.onTimeout(emitter::complete);
        eventEmitters.add(emitter);
    }

    private void executeDownload(PullRequestDownload pullRequestDownload) {
        logger.info("starting new pull request download [{}]", pullRequestDownload);

        final var target = fileStore.getForPullRequest(pullRequestDownload);
        try {
            gitHubClient.downloadRepoContent(pullRequestDownload.coordinates().repositoryName(),
                pullRequestDownload.headSha(),
                target);

            logger.debug("pull request download done for [{}]", pullRequestDownload);
            downloads.put(pullRequestDownload, DownloadState.FINISHED);

        } catch (RuntimeException rt) {
            logger.error("unexpected error while downloading", rt);
            downloads.put(pullRequestDownload, DownloadState.ERROR);
        }

        emitEvent();
    }

    private void emitEvent() {
        final var event = SseEmitter.event()
            .id(UUID.randomUUID().toString())
            .data("downloads updated")
            .name("download update event")
            .build();

        for (final var emitter : eventEmitters) {
            try {
                emitter.send(event);
            } catch (IOException e) {
                logger.warn("unable to send SSE download update event {}", e.getMessage());
            }
        }
    }
}
