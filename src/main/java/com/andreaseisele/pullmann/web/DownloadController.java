package com.andreaseisele.pullmann.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/downloads")
public class DownloadController {

    @GetMapping
    public String list() {
        return "downloads";
    }

}
