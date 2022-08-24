package com.andreaseisele.pullmann.download;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


import com.andreaseisele.pullmann.domain.PullRequestCoordinates;
import com.andreaseisele.pullmann.domain.RepositoryName;
import com.andreaseisele.pullmann.github.GitHubProperties;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;
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

        assertThat(path).hasFileName("blob.bin");
        assertThat(path.getParent()).exists();
    }

    @Test
    void reconstructDownload() {
        final var zipPath = tempDir.resolve("downloads")
            .resolve("octocat")
            .resolve("Hello-World")
            .resolve("pulls")
            .resolve("1")
            .resolve("6dcb09b5b57875f334f61aebed695e2e4193db5e")
            .resolve("pr.zip");

        final var reconstructed = FileStore.tryReconstructDownload(zipPath);

        assertThat(reconstructed).hasValueSatisfying(download -> {
            assertThat(download.headSha()).isEqualTo("6dcb09b5b57875f334f61aebed695e2e4193db5e");
            assertThat(download.coordinates().number()).isEqualTo(1);
            assertThat(download.coordinates().repositoryName().repository()).isEqualTo("Hello-World");
            assertThat(download.coordinates().repositoryName().owner()).isEqualTo("octocat");
        });
    }

    @Test
    void reconstructDownload_invalid() {
        final var zipPath = tempDir.resolve("downloads")
            .resolve("some")
            .resolve("wrong")
            .resolve("path")
            .resolve("pr.zip");

        final var reconstructed = FileStore.tryReconstructDownload(zipPath);

        assertThat(reconstructed).isEmpty();
    }

    @Test
    void reconstructDownload_invalidNumber() {
        final var zipPath = tempDir.resolve("downloads")
            .resolve("octocat")
            .resolve("Hello-World")
            .resolve("pulls")
            .resolve("one")
            .resolve("6dcb09b5b57875f334f61aebed695e2e4193db5e")
            .resolve("pr.zip");

        final var reconstructed = FileStore.tryReconstructDownload(zipPath);

        assertThat(reconstructed).isEmpty();
    }

    @Test
    void removeZip() throws IOException {
        final var repositoryName = new RepositoryName("octocat", "Hello-World");
        final var coordinates = new PullRequestCoordinates(repositoryName, 1);
        final var download = new PullRequestDownload(coordinates, "6dcb09b5b57875f334f61aebed695e2e4193db5e");
        final var zipPath = tempDir.resolve("downloads")
            .resolve("octocat")
            .resolve("Hello-World")
            .resolve("pulls")
            .resolve("1")
            .resolve("6dcb09b5b57875f334f61aebed695e2e4193db5e")
            .resolve("pr.zip");
        Files.createDirectories(zipPath.getParent());
        Files.createFile(zipPath);

        final var fileStore = new FileStore(properties);

        fileStore.removeZip(download);

        assertThat(zipPath).doesNotExist();
    }

    @Test
    void removeZip_doesnt_exist() throws IOException {
        final var repositoryName = new RepositoryName("octocat", "Hello-World");
        final var coordinates = new PullRequestCoordinates(repositoryName, 1);
        final var download = new PullRequestDownload(coordinates, "6dcb09b5b57875f334f61aebed695e2e4193db5e");
        final var zipPath = tempDir.resolve("downloads")
            .resolve("octocat")
            .resolve("Hello-World")
            .resolve("pulls")
            .resolve("1")
            .resolve("6dcb09b5b57875f334f61aebed695e2e4193db5e")
            .resolve("pr.zip");
        Files.createDirectories(zipPath.getParent());

        final var fileStore = new FileStore(properties);

        fileStore.removeZip(download);

        assertThat(zipPath).doesNotExist();
    }

    @Test
    void zipUp() throws URISyntaxException, IOException {
        final var repositoryName = new RepositoryName("octocat", "Hello-World");
        final var coordinates = new PullRequestCoordinates(repositoryName, 1);
        final var download = new PullRequestDownload(coordinates, "6dcb09b5b57875f334f61aebed695e2e4193db5e");
        final var fileStore = new FileStore(properties);

        final var testFile1 = testFile(download,
            "/zip/objects/48f73d778703cd9bccbac854b1d4d47be68b2bfb/blob.bin",
            "/dir/file1/file1.txt",
            "48f73d778703cd9bccbac854b1d4d47be68b2bfb");
        final var testFile2 = testFile(download,
            "/zip/objects/e097702c2f7b7a18f0b7a1c5332140821ca59aa1/blob.bin",
            "/dir/file2/file2.txt",
            "e097702c2f7b7a18f0b7a1c5332140821ca59aa1");

        fileStore.zipUp(download, List.of(testFile1, testFile2));

        final var zipPath = fileStore.getForPullRequest(download);
        assertThat(zipPath)
            .exists()
            .hasFileName("pr.zip");

        try (final var zipFile = new ZipFile(zipPath.toFile())) {
            final var entry1 = zipFile.getEntry("/dir/file1/file1.txt");
            assertThat(entry1).isNotNull();
            assertThat(entry1.isDirectory()).isFalse();
            assertThat(zipFile.getInputStream(entry1)).hasBinaryContent(Files.readAllBytes(testFile1.path()));

            final var entry2 = zipFile.getEntry("/dir/file2/file2.txt");
            assertThat(entry2).isNotNull();
            assertThat(entry2.isDirectory()).isFalse();
            assertThat(zipFile.getInputStream(entry2)).hasBinaryContent(Files.readAllBytes(testFile2.path()));
        }
    }

    private DownloadedFile testFile(PullRequestDownload pullRequestDownload, String resource, String filename, String sha) throws URISyntaxException {
        final var path = Path.of(getClass().getResource(resource).toURI());
        final var download = new FileDownload(pullRequestDownload, filename, sha);
        return new DownloadedFile(download, path);
    }

}