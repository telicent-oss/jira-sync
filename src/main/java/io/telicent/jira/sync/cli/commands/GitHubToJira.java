package io.telicent.jira.sync.cli.commands;

import com.atlassian.adf.jackson2.AdfJackson2;
import com.atlassian.adf.markdown.MarkdownParser;
import com.atlassian.adf.model.node.Doc;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.IssueFieldId;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import io.atlassian.util.concurrent.Promise;
import io.telicent.jira.sync.cli.options.CrossLinkOptions;
import io.telicent.jira.sync.client.AsynchronousRemoteLinksClient;
import io.telicent.jira.sync.client.EnhancedIssuesRestClient;
import io.telicent.jira.sync.client.EnhancedJiraRestClient;
import io.telicent.jira.sync.client.model.BasicRemoteLink;
import io.telicent.jira.sync.client.model.CrossLinks;
import io.telicent.jira.sync.client.model.RemoteLinkInput;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Command(name = "to-jira", description = "Command for synchronising GitHub Issues to JIRA")
public class GitHubToJira extends JiraGitHubSyncCommand {

    private static final TypeReference<Map<String, Object>> GENERIC_MAP_TYPE = new TypeReference<>() {
    };

    @Option(name = "--github-issue-id", title = "GitHubIssueID", description = "Specifies the ID of a single GitHub Issue that you wish to sync to JIRA.  If omitted all issues from the GitHub repository are sync'd to JIRA.")
    private int ghIssueId = 0;

    @Option(name = "--github-repository", title = "GitHubRepository", description = "Specifies the GitHub Repository whose issue you want to sync to JIRA")
    @Required
    private String ghRepo;

    @Option(name = "--jira-issue-type", title = "JiraIssueTypeId", description = "Specifies the ID of the JIRA Issue Type the GitHub Issues should be synchronised to JIRA as")
    @Required
    private int jiraIssueType;

    @AirlineModule
    private CrossLinkOptions crossLinkOptions = new CrossLinkOptions();

    @Override
    public int run() {
        try {
            GitHub gitHub = this.gitHubOptions.connect();
            GHRepository repository = gitHub.getRepository(this.ghRepo);
            if (repository == null) {
                throw new RuntimeException(
                        "GitHub Repository " + this.ghRepo + " does not exist, or your provided GitHub Credentials do not permit access to it");
            }
            CrossLinks crossLinks = this.crossLinkOptions.loadCrossLinks();

            try (EnhancedJiraRestClient jiraRestClient = this.jiraOptions.connect()) {
                if (this.ghIssueId > 0) {
                    GHIssue issue = repository.getIssue(this.ghIssueId);
                    if (issue == null) {
                        throw new RuntimeException(
                                "GitHub Repository " + this.ghRepo + " does not contain Issue ID " + this.ghIssueId);
                    } else if (issue.isPullRequest()) {
                        System.out.println("GitHub Repository " + this.ghRepo + " Issue ID " + this.ghIssueId + " is a PR for which sync is not supported");
                    } else {
                        this.syncOneIssue(crossLinks, jiraRestClient, issue);
                    }
                } else {
                    System.out.println("Syncing GitHub Issues from repository " + this.ghRepo);
                    for (GHIssue issue : repository.getIssues(GHIssueState.ALL)) {
                        if (issue.isPullRequest()) {
                            System.out.println("Skipping PR #" + issue.getNumber());
                            continue;
                        }
                        System.out.println("Syncing Issue #" + issue.getNumber());
                        this.syncOneIssue(crossLinks, jiraRestClient, issue);
                    }
                }
                return 0;
            }
        } catch (GHFileNotFoundException e) {
            throw new RuntimeException(
                    "GitHub Repository " + this.ghRepo + " does not exist, or your provided GitHub Credentials do not permit access to it");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void syncOneIssue(CrossLinks crossLinks, EnhancedJiraRestClient jiraRestClient, GHIssue issue) throws
            IOException {
        String gitHubIssueId = this.ghRepo + "/" + issue.getNumber();
        String jiraKey = crossLinks.getGitHubToJira().getLinks().get(gitHubIssueId);

        // Prepare the JIRA Issue content
        IssueRestClient issues = jiraRestClient.getIssueClient();
        IssueInput input = new IssueInputBuilder().setIssueTypeId((long) this.jiraIssueType)
                                                  .setProjectKey(this.jiraOptions.getProjectKey())
                                                  .setFieldInput(new FieldInput(IssueFieldId.DESCRIPTION_FIELD, translateMarkdownToAdf(issue)))
                                                  //.setDescription(translateMarkdownToAdf(issue))
                                                  .setSummary(issue.getTitle())
                                                  // TODO Copy assignee where relevant
                                                  // TODO Figure out if/how to copy labels across
                                                  .build();

        if (StringUtils.isNotBlank(jiraKey)) {
            System.out.println("GitHub Issue " + gitHubIssueId + " syncs to existing JIRA Issue " + jiraKey);
            Promise<Void> updated = issues.updateIssue(jiraKey, input);
            updated.claim();
            System.out.println("Updated JIRA Issue " + jiraKey);
        } else {
            // Need to create a new JIRA Issue
            Promise<BasicIssue> creation = issues.createIssue(input);
            BasicIssue created = creation.claim();
            System.out.println("Created new JIRA Issue " + created.getKey());

            // Update the cross-links with the new links
            // NB - We don't update the last sync'd ID as ComputeCrossLinks does because there might be issue creation
            //      happening on the JIRA side which we haven't sync'd the other way yet
            crossLinks.getGitHubToJira().setLinks(gitHubIssueId, created.getKey());
            crossLinks.getJiraToGitHub().setLinks(created.getKey(), gitHubIssueId);
            this.crossLinkOptions.saveCrossLinks();

            jiraKey = created.getKey();

            // TODO If there's a JIRA Key mentioned in the issue automatically add issue links?
        }

        // Always create/update the remote link in case someone has modified it incorrectly
        AsynchronousRemoteLinksClient remoteLinksClient = jiraRestClient.getRemoteLinksClient();
        RemoteLinkInput remoteLink = RemoteLinkInput.builder()
                                                    .url(issue.getHtmlUrl().toString())
                                                    .title("GitHub Issue " + gitHubIssueId)
                                                    .globalId("github:" + gitHubIssueId)
                                                    .build();
        Promise<BasicRemoteLink> createdLinkPromise =
                remoteLinksClient.createOrUpdateRemoteLink(jiraKey, remoteLink);
        BasicRemoteLink createdLink = createdLinkPromise.claim();
        System.out.println(
                "Associated GitHub Issue with JIRA Issue via Remote Link ID " + createdLink.getId());

        // TODO If the GH Issue is closed apply a suitable transition to the JIRA Issue if it isn't also closed?

        // TODO Sync Issue Comments if desired?
    }

    private static Object translateMarkdownToAdf(GHIssue issue) throws JsonProcessingException {
        MarkdownParser parser = new MarkdownParser();
        Doc doc = parser.unmarshall(issue.getBody());
        AdfJackson2 jackson = new AdfJackson2();
        String json = jackson.marshall(doc);
        Map<String, Object> map = new ObjectMapper().readValue(json, GENERIC_MAP_TYPE);
        return new ComplexIssueInputFieldValue(map);
    }
}
