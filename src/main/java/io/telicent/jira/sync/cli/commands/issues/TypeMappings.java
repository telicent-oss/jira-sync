package io.telicent.jira.sync.cli.commands.issues;

import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import io.telicent.jira.sync.cli.commands.SyncCommand;
import io.telicent.jira.sync.cli.options.GitHubOptions;
import io.telicent.jira.sync.cli.options.JiraIssueTypeMappingOptions;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Command(name = "type-mappings")
public class TypeMappings extends SyncCommand {

    @AirlineModule
    protected final GitHubOptions gitHubOptions = new GitHubOptions();

    @AirlineModule
    private JiraIssueTypeMappingOptions jiraIssueTypeMappingOptions = new JiraIssueTypeMappingOptions();

    @Option(name = "--github-issue-id", title = "GitHubIssueID", description = "Specifies the ID of a single GitHub Issue that you wish to test JIRA type mappings for.  If omitted all issues from the GitHub repository are tested.")
    private int ghIssueId = 0;

    @Option(name = "--github-repository", title = "GitHubRepository", description = "Specifies the GitHub Repository whose issue you want to test JIRA type mappings for.")
    @Required
    private String ghRepo;

    @Option(name = "--include-closed", description = "When specified also sync's any GitHub issues that are already closed on the GitHub side.")
    private boolean includeClosed = false;

    @Option(name = "--detailed", description = "When specified show title and labels of each issue.")
    private boolean detailed = false;

    @Override
    public int run() {
        try {
            GitHub gitHub = this.gitHubOptions.connect();
            GHRepository repository = gitHub.getRepository(this.ghRepo);
            if (repository == null) {
                throw new RuntimeException(
                        "GitHub Repository " + this.ghRepo + " does not exist, or your provided GitHub Credentials do not permit access to it");
            }

            if (this.ghIssueId > 0) {
                showTypeMapping(repository.getIssue(this.ghIssueId));
            } else {
                System.out.println("Showing JIRA type mappings for GitHub Issues from repository " + this.ghRepo);
                List<GHIssue> issues = new ArrayList<>(
                        repository.getIssues(
                                this.includeClosed ? org.kohsuke.github.GHIssueState.ALL : GHIssueState.OPEN));
                Collections.sort(issues, Comparator.comparingInt(GHIssue::getNumber));
                for (GHIssue issue : issues) {
                    showTypeMapping(issue);
                }
            }

            return 0;
        } catch (GHFileNotFoundException e) {
            throw new RuntimeException(
                    "GitHub Repository " + this.ghRepo + " does not exist, or your provided GitHub Credentials do not permit access to it");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showTypeMapping(GHIssue issue) {
        if (issue.isPullRequest()) {
            System.out.println("Ignoring GitHub PR #" + issue.getNumber());
        } else {
            long jiraIssueType = this.jiraIssueTypeMappingOptions.getJiraIssueType(issue);
            System.out.println("GitHub Issue #" + issue.getNumber() + " maps to JIRA Issue Type " + jiraIssueType);
            if (this.detailed) {
                System.out.println("Title: " + issue.getTitle());
                System.out.println(
                        "Labels: " + issue.getLabels()
                                          .stream()
                                          .map(l -> l.getName())
                                          .collect(Collectors.joining(", ")));
            }
        }
        if (this.detailed) {
            System.out.println();
        }
    }
}
