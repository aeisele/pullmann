package com.andreaseisele.pullmann.web;

import com.andreaseisele.pullmann.domain.RepositoryName;
import com.andreaseisele.pullmann.service.PullRequestService;
import javax.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Validated
@Controller
@RequestMapping("/pulls")
public class PullRequestController {

    private final PullRequestService pullRequestService;

    public PullRequestController(PullRequestService pullRequestService) {
        this.pullRequestService = pullRequestService;
    }

    @GetMapping
    public String pullsForRepo(
        @RequestParam(required = false) @Pattern(regexp = RepositoryName.REGEX_REPO_FULL_NAME) String repoFullName,
        @RequestParam(required = false, defaultValue = "1") Integer page,
        Model model) {

        if (repoFullName != null && !repoFullName.isBlank()) {
            final var repositoryName = RepositoryName.parse(repoFullName);
            if (repositoryName.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid repository full name");
            }

            final var result = pullRequestService.requestsForRepo(repositoryName.get(), page);
            model.addAttribute("repoFullName", repoFullName);
            model.addAttribute("pulls", result.getPullRequests());
            model.addAttribute("page", result.getPage());
            model.addAttribute("maxPages", result.getMaxPages());
        }

        return "pulls";
    }

}
