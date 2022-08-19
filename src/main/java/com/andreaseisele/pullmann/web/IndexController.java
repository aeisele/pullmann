package com.andreaseisele.pullmann.web;

import com.andreaseisele.pullmann.service.RepositoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
@Controller
public class IndexController {

    private final RepositoryService repositoryService;

    public IndexController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("repositories", repositoryService.listRepositories());
        return "index";
    }

}
