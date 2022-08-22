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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

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
                          @RequestParam(value = "merged", required = false) Boolean merged,
                          Model model) {
        final PullRequestCoordinates coordinates = buildCoordinates(owner, repo, number);
        final var pullRequest = pullRequestService.requestDetails(coordinates);

        model.addAttribute("pr", pullRequest);
        model.addAttribute("canMerge", Boolean.TRUE.equals(pullRequest.mergeable()));
        model.addAttribute("owner", owner);
        model.addAttribute("repo", repo);
        if (merged != null) {
            model.addAttribute("merged", merged);
        }

        return "prDetails";
    }

    @PostMapping("/merge/{owner}/{repo}/{number}")
    public RedirectView merge(@PathVariable("owner") String owner,
                              @PathVariable("repo") String repo,
                              @PathVariable("number") Long number,
                              RedirectAttributes redirectAttributes) {

        final PullRequestCoordinates coordinates = buildCoordinates(owner, repo, number);
        final var result = pullRequestService.merge(coordinates);
        final var merged = result.isSuccessful();

        redirectAttributes.addAttribute("merged", merged);

        return new RedirectView("/pulls/details/{owner}/{repo}/{number}");
    }

    @PostMapping("download/{owner}/{repo}/{number}")
    public String download(@PathVariable("owner") String owner,
                           @PathVariable("repo") String repo,
                           @PathVariable("number") Long number) {

        final PullRequestCoordinates coordinates = buildCoordinates(owner, repo, number);
        pullRequestService.startDownload(coordinates);

        return "redirect:/downloads";
    }

    private static PullRequestCoordinates buildCoordinates(String owner, String repo, Long number) {
        return new PullRequestCoordinates(new RepositoryName(owner, repo), number);
    }

}
