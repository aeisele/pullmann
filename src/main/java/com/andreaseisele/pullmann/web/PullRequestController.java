package com.andreaseisele.pullmann.web;

import com.andreaseisele.pullmann.domain.PullRequestCoordinates;
import com.andreaseisele.pullmann.domain.RepositoryName;
import com.andreaseisele.pullmann.github.dto.PullRequest;
import com.andreaseisele.pullmann.github.result.MergeResult;
import com.andreaseisele.pullmann.github.result.PullRequestResult;
import com.andreaseisele.pullmann.service.PullRequestService;
import java.util.Optional;
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
            final Optional<RepositoryName> maybeRepositoryName = RepositoryName.parse(repoFullName);
            if (maybeRepositoryName.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid repository full name");
            }
            final RepositoryName repositoryName = maybeRepositoryName.get();

            final PullRequestResult result = pullRequestService.requestsForRepo(repositoryName, page);
            model.addAttribute("repoFullName", repoFullName);
            model.addAttribute("owner", repositoryName.owner());
            model.addAttribute("repo", repositoryName.repository());
            model.addAttribute("pulls", result.getList());
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
                          @RequestParam(value = "closed", required = false) Boolean closed,
                          Model model) {
        final PullRequestCoordinates coordinates = buildCoordinates(owner, repo, number);
        final PullRequest pullRequest = pullRequestService.requestDetails(coordinates);

        model.addAttribute("pr", pullRequest);
        model.addAttribute("canMerge", Boolean.TRUE.equals(pullRequest.mergeable())
            && pullRequest.state() != PullRequest.State.CLOSED);
        model.addAttribute("owner", owner);
        model.addAttribute("repo", repo);
        if (merged != null) {
            model.addAttribute("merged", merged);
        }
        if (closed != null) {
            model.addAttribute("closed", closed);
        }

        return "prDetails";
    }

    @PostMapping("/merge/{owner}/{repo}/{number}")
    public RedirectView merge(@PathVariable("owner") String owner,
                              @PathVariable("repo") String repo,
                              @PathVariable("number") Long number,
                              RedirectAttributes redirectAttributes) {

        final PullRequestCoordinates coordinates = buildCoordinates(owner, repo, number);
        final MergeResult result = pullRequestService.merge(coordinates);
        final boolean merged = result.isSuccessful();

        redirectAttributes.addAttribute("merged", merged);

        return new RedirectView("/pulls/details/{owner}/{repo}/{number}");
    }

    @PostMapping("/close/{owner}/{repo}/{number}")
    public RedirectView close(@PathVariable("owner") String owner,
                              @PathVariable("repo") String repo,
                              @PathVariable("number") Long number,
                              RedirectAttributes redirectAttributes) {

        final PullRequestCoordinates coordinates = buildCoordinates(owner, repo, number);
        final boolean closed = pullRequestService.close(coordinates);

        redirectAttributes.addAttribute("closed", closed);

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
