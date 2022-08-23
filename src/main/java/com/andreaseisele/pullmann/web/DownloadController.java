package com.andreaseisele.pullmann.web;

import com.andreaseisele.pullmann.service.DownloadService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

}
