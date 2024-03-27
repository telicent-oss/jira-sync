package io.telicent.jira.sync.cli.commands;

import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import io.atlassian.util.concurrent.Promise;
import io.telicent.jira.sync.cli.options.CrossLinkOptions;
import io.telicent.jira.sync.client.AsynchronousRemoteLinksClient;
import io.telicent.jira.sync.client.EnhancedJiraRestClient;
import io.telicent.jira.sync.client.model.CrossLinkedProject;
import io.telicent.jira.sync.client.model.CrossLinks;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

@Command(name = "cross-links", description = "Command that calculates the cross-links between GitHub and JIRA")
public class ComputeCrossLinks extends JiraSyncCommand {

    public static final String GITHUB_LINK_ID_PREFIX = "github:";
    @AirlineModule
    private CrossLinkOptions crossLinkOptions = new CrossLinkOptions();

    @Override
    public int run() {
        try (EnhancedJiraRestClient jiraRestClient = this.jiraOptions.connect()) {
            CrossLinks crossLinks = this.crossLinkOptions.loadCrossLinks();

            SearchRestClient searchClient = jiraRestClient.getSearchClient();
            StringBuilder jql = new StringBuilder();
            jql.append("project = ").append(this.jiraOptions.getProjectKey());
            CrossLinkedProject jiraToGitHub = crossLinks.getJiraToGitHub();
            CrossLinkedProject githubToJira = crossLinks.getGitHubToJira();
            String lastSyncId = jiraToGitHub.getLastSyncedIds().get(this.jiraOptions.getProjectKey());
            if (StringUtils.isNotBlank(lastSyncId)) {
                jql.append(" and id > ").append(lastSyncId);
            }
            jql.append(" order by id");
            System.out.println("Using JQL Query:");
            System.out.print("  ");
            System.out.println(jql);
            Promise<SearchResult> searchPromise = searchClient.searchJql(jql.toString());
            int processed = 0;
            try {
                SearchResult searchResults = searchPromise.claim();

                AsynchronousRemoteLinksClient remoteLinksClient = jiraRestClient.getRemoteLinksClient();
                for (Issue issue : searchResults.getIssues()) {
                    Promise<Iterable<RemoteIssueLink>> remoteLinksPromise =
                            remoteLinksClient.getRemoteLinks(issue.getKey());
                    Iterable<RemoteIssueLink> remoteLinks = remoteLinksPromise.claim();
                    for (RemoteIssueLink link : remoteLinks) {
                        if (StringUtils.startsWith(link.getGlobalId(), GITHUB_LINK_ID_PREFIX)) {
                            String gitHubLinkId = link.getGlobalId().substring(GITHUB_LINK_ID_PREFIX.length());
                            jiraToGitHub.setLinks(issue.getKey(), gitHubLinkId);
                            githubToJira.setLinks(gitHubLinkId, issue.getKey());
                            System.out.println("Discovered cross link " + issue.getKey() + " -> " + gitHubLinkId);
                        }
                    }
                    jiraToGitHub.getLastSyncedIds().put(this.jiraOptions.getProjectKey(), issue.getKey());

                    processed++;
                }

                if (processed > 0) {
                    System.out.println(
                            "Processed " + processed + " issues from JIRA Project " + this.jiraOptions.getProjectKey());

                    // Save the computed cross-links
                    this.crossLinkOptions.saveCrossLinks();
                } else {
                    System.out.println("No new issues found in JIRA Project " + this.jiraOptions.getProjectKey());
                }
            } catch (RestClientException e) {
                reportJiraRestError(e);
                return 1;
            }

            return 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
