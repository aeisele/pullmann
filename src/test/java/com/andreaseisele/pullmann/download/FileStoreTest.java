package com.andreaseisele.pullmann.download;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


import com.andreaseisele.pullmann.domain.PullRequestCoordinates;
import com.andreaseisele.pullmann.domain.RepositoryName;
import com.andreaseisele.pullmann.github.GitHubProperties;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.PathResource;

class FileStoreTest {

    @TempDir
    private Path tempDir;

    private GitHubProperties properties;

    @BeforeEach
    void setUp() {
        this.properties = new GitHubProperties();
        properties.getDownload().setLocation(new PathResource(tempDir));
    }

    @Test
    void init() {
        assertDoesNotThrow(() -> new FileStore(properties));
    }

    @Test
    void getForPullRequest() {
        final var repositoryName = new RepositoryName("octocat", "Hello-World");
        final var coordinates = new PullRequestCoordinates(repositoryName, 1);
        final var download = new PullRequestDownload(coordinates, "6dcb09b5b57875f334f61aebed695e2e4193db5e");
        final var fileStore = new FileStore(properties);

        final var path = fileStore.getForPullRequest(download);

        assertThat(path).hasFileName("pr.zip");
        assertThat(path.getParent()).exists();
    }

    @Test
    void getForFile() {
        final var repositoryName = new RepositoryName("octocat", "Hello-World");
        final var coordinates = new PullRequestCoordinates(repositoryName, 1);
        final var parentDownload = new PullRequestDownload(coordinates, "6dcb09b5b57875f334f61aebed695e2e4193db5e");
        final var download = new FileDownload(parentDownload, "file1.txt", "bbcd538c8e72b8c175046e27cc8f907076331401");
        final var fileStore = new FileStore(properties);

        final var path = fileStore.getForFile(download);

        assertThat(path).hasFileName("file1.txt");
        assertThat(path.getParent()).exists();
    }
}