package com.andreaseisele.pullmann.service;

import static org.assertj.core.api.Assertions.assertThat;


import com.andreaseisele.pullmann.download.FileStore;
import com.andreaseisele.pullmann.github.GitHubClient;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class DownloadServiceTest {

    @Mock
    private FileStore fileStore;

    @Mock
    private GitHubClient gitHubClient;

    private DownloadService service;

    @BeforeEach
    void setUp() {
        final var pullRequestDownloadExecutor = new TaskExecutorAdapter(Executors.newSingleThreadExecutor());
        this.service = new DownloadService(gitHubClient, pullRequestDownloadExecutor, fileStore);
    }

    @Test
    void getDownloads_empty() {
        final var downloads = service.getDownloads();

        assertThat(downloads).isEmpty();
    }



}