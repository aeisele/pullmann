package com.andreaseisele.pullmann.web;

import com.andreaseisele.pullmann.domain.PullRequestCoordinates;
import com.andreaseisele.pullmann.domain.RepositoryName;
import com.andreaseisele.pullmann.download.PullRequestDownload;
import com.andreaseisele.pullmann.service.DownloadService;
import java.io.IOException;
import java.nio.file.Files;
import org.springframework.core.io.PathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
@RequestMapping("/downloads")
public class DownloadController {

    private final DownloadService downloadService;

    public DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @GetMapping
    public String list(Model model) {
        final var downloads = downloadService.getDownloads();
        model.addAttribute("downloads", downloads);

        return "downloads";
    }

    @GetMapping("/zip/{owner}/{repo}/{number}/{headSha}")
    public ResponseEntity<PathResource> downloadZip(@PathVariable("owner") String owner,
                                                    @PathVariable("repo") String repo,
                                                    @PathVariable("number") Long number,
                                                    @PathVariable("headSha") String headSha) throws IOException {

        final var repositoryName = new RepositoryName(owner, repo);
        final var coordinates = new PullRequestCoordinates(repositoryName, number);
        final var download = new PullRequestDownload(coordinates, headSha);

        final var zip = downloadService.findZip(download);
        if (zip.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "unable to find pull request zip");
        }

        final var contentDisposition = ContentDisposition.attachment()
            .filename(zip.get().getFileName().toString())
            .build();

        final var headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "zip"));
        headers.setContentDisposition(contentDisposition);
        headers.setContentLength(Files.size(zip.get()));

        return new ResponseEntity<>(new PathResource(zip.get()), headers, HttpStatus.OK);
    }

    @PostMapping("/delete/{owner}/{repo}/{number}/{headSha}")
    public String deleteZip(@PathVariable("owner") String owner,
                            @PathVariable("repo") String repo,
                            @PathVariable("number") Long number,
                            @PathVariable("headSha") String headSha) {

        final var repositoryName = new RepositoryName(owner, repo);
        final var coordinates = new PullRequestCoordinates(repositoryName, number);
        final var download = new PullRequestDownload(coordinates, headSha);

        downloadService.deleteZip(download);

        return "redirect:/downloads";
    }

    @GetMapping("/events")
    public SseEmitter downloadEvents() {
        final var emitter = new SseEmitter();
        downloadService.registerEmitter(emitter);
        return emitter;
    }

}
