package io.telicent.jira.sync.cli.commands;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import io.atlassian.util.concurrent.Promise;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;

@Command(name = "to-jira", description = "Command for synchronising a single GitHub Issue to JIRA")
public class OneGitHubToJira extends JiraGitHubSyncCommand {

    @Option(name = "--github-issue-id", title = "GitHubIssueID", description = "Specifies the ID of the GitHub Issue that you wish to sync to JIRA")
    @Required
    private int ghIssueId;

    @Option(name = "--github-repository", title = "GitHubRepository", description = "Specifies the GitHub Repository whose issue you want to sync to JIRA")
    @Required
    private String ghRepo;

    @Option(name = "--jira-issue-type", title = "JiraIssueTypeId", description = "Specifies the ID of the JIRA Issue Type the GitHub Issues should be synchronised to JIRA as")
    @Required
    private int jiraIssueType;

    @Override
    public int run() {
        try {
            GitHub gitHub = this.gitHubOptions.connect();
            GHRepository repository = gitHub.getRepository(this.ghRepo);
            if (repository == null) {
                throw new RuntimeException(
                        "GitHub Repository " + this.ghRepo + " does not exist, or your provided GitHub Token does not permit access to it");
            }
            GHIssue issue = repository.getIssue(this.ghIssueId);
            if (issue == null) {
                throw new RuntimeException(
                        "GitHub Repository " + this.ghRepo + " does not contain Issue ID " + this.ghIssueId);
            }

            try (JiraRestClient jiraRestClient = this.jiraOptions.connect()) {
                // TODO Figure out if the issue has already been synced, turns out this is harder than it should be cos
                //  Atlassian's JIRA REST Client intentionally omits many parts of the REST API

                IssueRestClient issues = jiraRestClient.getIssueClient();
                IssueInput input = new IssueInputBuilder().setIssueTypeId((long) this.jiraIssueType)
                                                          .setDescription(issue.getBody())
                                                          .setSummary(issue.getTitle())
                                                          .build();
                Promise<BasicIssue> creation = issues.createIssue(input);
                BasicIssue created = creation.claim();
                System.out.println("Created JIRA Issue " + created.getKey());

                // TODO Insert a Web Link on the JIRA Issue so we can easily get back to the original GitHub Issue

                return 0;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
