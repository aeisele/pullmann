package com.andreaseisele.pullmann.web;

import com.andreaseisele.pullmann.github.result.RepositoryResult;
import com.andreaseisele.pullmann.service.RepositoryService;
import javax.validation.constraints.Positive;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Validated
@RequestMapping("/")
@Controller
public class IndexController {

    private final RepositoryService repositoryService;

    public IndexController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @GetMapping
    public String index(@Positive @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                        Model model) {

        final RepositoryResult result = repositoryService.listRepositories(page);

        model.addAttribute("repositories", result.getList());
        model.addAttribute("page", result.getPage());
        model.addAttribute("maxPages", result.getMaxPages());

        return "index";
    }

}
