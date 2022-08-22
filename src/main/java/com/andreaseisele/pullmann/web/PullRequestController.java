package com.andreaseisele.pullmann.web;

import com.andreaseisele.pullmann.domain.PullRequestCoordinates;
import com.andreaseisele.pullmann.domain.RepositoryName;
import com.andreaseisele.pullmann.service.PullRequestService;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        @RequestParam(required = false, defaultValue = "1") @Positive Integer page,
        Model model) {

        if (repoFullName != null && !repoFullName.isBlank()) {
            final var maybeRepositoryName = RepositoryName.parse(repoFullName);
            if (maybeRepositoryName.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid repository full name");
            }
            final var repositoryName = maybeRepositoryName.get();

            final var result = pullRequestService.requestsForRepo(repositoryName, page);
            model.addAttribute("repoFullName", repoFullName);
            model.addAttribute("owner", repositoryName.getOwner());
            model.addAttribute("repo", repositoryName.getRepository());
            model.addAttribute("pulls", result.getPullRequests());
            model.addAttribute("page", result.getPage());
            model.addAttribute("maxPages", result.getMaxPages());
        }

        return "pulls";
    }

    @GetMapping("/details/{owner}/{repo}/{number}")
    public String details(@PathVariable("owner") String owner,
                          @PathVariable("repo") String repo,
                          @PathVariable("number") Long number,
                          Model model) {
        final var coordinates = new PullRequestCoordinates(new RepositoryName(owner, repo), number);
        final var pullRequest = pullRequestService.requestDetails(coordinates);

        model.addAttribute("pr", pullRequest);
        model.addAttribute("canMerge", Boolean.TRUE.equals(pullRequest.mergeable()));
        model.addAttribute("owner", owner);
        model.addAttribute("repo", repo);

        return "prDetails";
    }

}
