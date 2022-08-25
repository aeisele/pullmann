package com.andreaseisele.pullmann.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


import com.andreaseisele.pullmann.domain.PullRequestCoordinates;
import com.andreaseisele.pullmann.domain.RepositoryName;
import com.andreaseisele.pullmann.download.DownloadState;
import com.andreaseisele.pullmann.download.FileStore;
import com.andreaseisele.pullmann.download.PullRequestDownload;
import com.andreaseisele.pullmann.github.GitHubClient;
import com.andreaseisele.pullmann.github.dto.BranchInfo;
import com.andreaseisele.pullmann.github.dto.PullRequest;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.support.TaskExecutorAdapter;

@ExtendWith(MockitoExtension.class)
class DownloadServiceTest {

    @Mock
    private FileStore fileStore;

    @Mock
    private GitHubClient gitHubClient;

    private DownloadService service;

    @BeforeEach
    void setUp() {
        final TaskExecutorAdapter pullRequestDownloadExecutor = new TaskExecutorAdapter(Executors.newSingleThreadExecutor());
        this.service = new DownloadService(gitHubClient, pullRequestDownloadExecutor, fileStore);
    }

    @Test
    void getDownloads_empty() {
        final Map<PullRequestDownload, DownloadState> downloads = service.getDownloads();

        assertThat(downloads).isEmpty();
    }

    @Test
    void startDownload() {
        final RepositoryName repositoryName = new RepositoryName("octocat", "Hello-World");
        final PullRequestCoordinates coordinates = new PullRequestCoordinates(repositoryName, 1);
        final String ref = "5bed3c62446116728f65e3809210bb605f11e687";
        final PullRequestDownload download = new PullRequestDownload(coordinates, ref);
        final BranchInfo head = new BranchInfo("main", "main", ref);
        final PullRequest pullRequest = new PullRequest(1L, 1L, null, null, null, null, null, head, null, 0L, false, false);

        when(fileStore.getForPullRequest(download)).thenReturn(Path.of(""));

        when(gitHubClient.pullRequestDetails(coordinates)).thenReturn(pullRequest);

        when(gitHubClient.downloadRepoContent(eq(repositoryName), eq(ref), any(Path.class)))
            .thenAnswer(AdditionalAnswers.answersWithDelay(2000, invocation -> true));

        service.startDownload(coordinates);

        await()
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() ->
                assertThat(service.getDownloads()).containsEntry(download, DownloadState.FINISHED));
    }

}