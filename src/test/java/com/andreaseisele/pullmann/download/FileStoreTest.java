package com.andreaseisele.pullmann.download;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


import com.andreaseisele.pullmann.domain.PullRequestCoordinates;
import com.andreaseisele.pullmann.domain.RepositoryName;
import com.andreaseisele.pullmann.github.GitHubProperties;
import java.nio.file.Path;
import java.util.Optional;
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
        final RepositoryName repositoryName = new RepositoryName("octocat", "Hello-World");
        final PullRequestCoordinates coordinates = new PullRequestCoordinates(repositoryName, 1);
        final PullRequestDownload
            download = new PullRequestDownload(coordinates, "6dcb09b5b57875f334f61aebed695e2e4193db5e");
        final FileStore fileStore = new FileStore(properties);

        final Path path = fileStore.getForPullRequest(download);

        assertThat(path)
            .exists()
            .isDirectory();
    }

    @Test
    void reconstructDownload() {
        final Path zipPath = tempDir.resolve("downloads")
            .resolve("octocat")
            .resolve("Hello-World")
            .resolve("pulls")
            .resolve("1")
            .resolve("6dcb09b5b57875f334f61aebed695e2e4193db5e")
            .resolve("aeisele-pullman-playgournd-5bed3c6.zip");

        final Optional<PullRequestDownload> reconstructed = FileStore.tryReconstructDownload(zipPath);

        assertThat(reconstructed).hasValueSatisfying(download -> {
            assertThat(download.headSha()).isEqualTo("6dcb09b5b57875f334f61aebed695e2e4193db5e");
            assertThat(download.coordinates().number()).isEqualTo(1);
            assertThat(download.coordinates().repositoryName().repository()).isEqualTo("Hello-World");
            assertThat(download.coordinates().repositoryName().owner()).isEqualTo("octocat");
        });
    }

    @Test
    void reconstructDownload_invalid() {
        final Path zipPath = tempDir.resolve("downloads")
            .resolve("some")
            .resolve("wrong")
            .resolve("path")
            .resolve("pr.zip");

        final Optional<PullRequestDownload> reconstructed = FileStore.tryReconstructDownload(zipPath);

        assertThat(reconstructed).isEmpty();
    }

    @Test
    void reconstructDownload_invalidNumber() {
        final Path zipPath = tempDir.resolve("downloads")
            .resolve("octocat")
            .resolve("Hello-World")
            .resolve("pulls")
            .resolve("one")
            .resolve("6dcb09b5b57875f334f61aebed695e2e4193db5e")
            .resolve("pr.zip");

        final Optional<PullRequestDownload> reconstructed = FileStore.tryReconstructDownload(zipPath);

        assertThat(reconstructed).isEmpty();
    }

}