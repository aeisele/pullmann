package com.andreaseisele.pullmann.web;

import com.andreaseisele.pullmann.service.PullRequestService;
import javax.validation.constraints.Pattern;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Validated
@Controller
@RequestMapping("/pulls")
public class PullRequestController {

    private final PullRequestService pullRequestService;

    public PullRequestController(PullRequestService pullRequestService) {
        this.pullRequestService = pullRequestService;
    }

    @GetMapping
    public String pullsForRepo(@RequestParam(required = false) @Pattern(regexp = "([\\w,\\d,\\-,_,\\.]+)\\/([\\w,\\d,\\-,_,\\.]+)") String repoFullName,
                               @RequestParam(required = false, defaultValue = "1") Integer page,
                               Model model) {
        if (repoFullName != null && !repoFullName.isBlank()) {
            final var pullRequests = pullRequestService.requestsForRepo(repoFullName, page);
            model.addAttribute("repoFullName", repoFullName);
            model.addAttribute("pulls", pullRequests);
            model.addAttribute("page", page);
            model.addAttribute("maxPages", 10);
        }
        return "pulls";
    }

}
