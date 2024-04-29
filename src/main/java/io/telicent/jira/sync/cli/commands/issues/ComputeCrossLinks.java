package io.telicent.jira.sync.cli.commands.issues;

import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import io.atlassian.util.concurrent.Promise;
import io.telicent.jira.sync.cli.commands.JiraProjectSyncCommand;
import io.telicent.jira.sync.cli.options.CrossLinkOptions;
import io.telicent.jira.sync.client.AsynchronousIssueCommentsClient;
import io.telicent.jira.sync.client.AsynchronousRemoteLinksClient;
import io.telicent.jira.sync.client.EnhancedJiraRestClient;
import io.telicent.jira.sync.client.model.Comment;
import io.telicent.jira.sync.client.model.CommentProperty;
import io.telicent.jira.sync.client.model.CrossLinkedProject;
import io.telicent.jira.sync.client.model.CrossLinks;
import io.telicent.jira.sync.utils.JiraUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;

@Command(name = "cross-links", description = "Command that calculates the cross-links between GitHub and JIRA")
public class ComputeCrossLinks extends JiraProjectSyncCommand {

    public static final String GITHUB_LINK_ID_PREFIX = "github:";
    @AirlineModule
    private CrossLinkOptions crossLinkOptions = new CrossLinkOptions();

    @Override
    public int run() {
        try (EnhancedJiraRestClient jiraRestClient = this.jiraOptions.connect()) {
            CrossLinks crossLinks = this.crossLinkOptions.loadCrossLinks();

            SearchRestClient searchClient = jiraRestClient.getSearchClient();
            CrossLinkedProject jiraToGitHub = crossLinks.getJiraToGitHub();
            CrossLinkedProject githubToJira = crossLinks.getGitHubToJira();

            int processed;
            do {
                processed = 0;

                // Get the last sync'd ID and use it to form our JQL query
                String lastSyncId = jiraToGitHub.getLastSyncedIds().get(this.jiraOptions.getProjectKey());
                StringBuilder jql = new StringBuilder();
                jql.append("project = ").append(this.jiraOptions.getProjectKey());
                if (StringUtils.isNotBlank(lastSyncId)) {
                    jql.append(" and id > ").append(lastSyncId);
                }
                jql.append(" order by id");
                System.out.println("Using JQL Query:");
                System.out.print("  ");
                System.out.println(jql);

                // Search for issues, this returns at most 50 issues by default
                Promise<SearchResult> searchPromise = searchClient.searchJql(jql.toString());
                try {
                    SearchResult searchResults = searchPromise.claim();

                    AsynchronousRemoteLinksClient remoteLinksClient = jiraRestClient.getRemoteLinksClient();
                    AsynchronousIssueCommentsClient commentsClient = jiraRestClient.getCommentsClient();
                    for (Issue issue : searchResults.getIssues()) {
                        // Get the remote links for that issue and look for the remote link this tool creates
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

                        // Process the comments for the issue
                        // Again look for the comment property this tool creates
                        Promise<Iterable<Comment>> commentsPromise = commentsClient.getComments(issue.getKey());
                        Iterable<Comment> comments = commentsPromise.claim();
                        Promise<Iterable<Comment>> commentsByIdPromise = commentsClient.getCommentsByIds(
                                IterableUtils.toList(comments).stream().map(c -> c.getId().toString()).toList());
                        comments = commentsByIdPromise.claim();
                        for (Comment comment : comments) {
                            CommentProperty jiraSyncProperty = comment.getProperty(JiraUtils.COMMENT_PROPERTY_KEY);
                            if (jiraSyncProperty != null) {
                                @SuppressWarnings("unchecked")
                                String gitHubCommentId = ((Map<String, String>) jiraSyncProperty.value()).get(
                                        JiraUtils.GITHUB_COMMENT_ID_PROPERTY);
                                String jiraCommentId = JiraUtils.getJiraCommentId(issue.getKey(), comment);
                                jiraToGitHub.setLinks(jiraCommentId, gitHubCommentId);
                                githubToJira.setLinks(gitHubCommentId, jiraCommentId);
                                System.out.println("Discovered cross link " + jiraCommentId + " -> " + gitHubCommentId);
                            }
                        }

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

                // If we found at least one issue this time then we'll go round the loop and query again
            } while (processed > 0);

            return 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
