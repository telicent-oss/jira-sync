package io.telicent.jira.sync.cli.commands;

import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import io.atlassian.util.concurrent.Promise;
import io.telicent.jira.sync.client.AsynchronousRemoteLinksClient;
import io.telicent.jira.sync.client.EnhancedJiraRestClient;

import java.io.IOException;

@Command(name = "remote-links", description = "Retrieves the remote links associated with a JIRA Issue")
public class RemoteLinks extends JiraSyncCommand{

    @Option(name = "--jira-issue", title = "JiraIssue", description = "Specifies the key of a JIRA Issue")
    @Required
    private String issueKey;

    @Override
    public int run() {
        try (EnhancedJiraRestClient jiraRestClient = this.jiraOptions.connect()) {
            AsynchronousRemoteLinksClient remoteLinksClient = jiraRestClient.getRemoteLinksClient();
            Promise<Iterable<RemoteIssueLink>> linksPromise = remoteLinksClient.getRemoteLinks(this.issueKey);
            Iterable<RemoteIssueLink> links = linksPromise.claim();

            int linkCount = 0;
            for (RemoteIssueLink link : links) {
                System.out.println("Remote Link Title: " + link.getTitle());
                System.out.println("Remote Link URL: " + link.getUrl());
                System.out.println();
                linkCount++;
            }
            if (linkCount == 0) {
                System.out.println("JIRA Issue " + this.issueKey + " has no remote links associated with it");
            }

            return 0;
        } catch (RestClientException e) {
            this.reportJiraRestError(e);
            return 1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
