package io.telicent.jira.sync.cli.commands;

import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;
import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.github.rvesse.airline.annotations.Command;
import io.atlassian.util.concurrent.Promise;

import java.io.IOException;

@Command(name = "jira-types", description = "Finds the available JIRA Issue Types for the specified JIRA Project")
public class IssueTypes extends JiraSyncCommand {
    @Override
    public int run() {
        try (JiraRestClient jira = this.jiraOptions.connect()) {
            IssueRestClient issues = jira.getIssueClient();
            Promise<Iterable<CimProject>> promise = issues.getCreateIssueMetadata(
                    new GetCreateIssueMetadataOptionsBuilder().withProjectKeys(this.jiraOptions.getProjectKey())
                                                              .build());
            Iterable<CimProject> projects = promise.claim();
            int typesFound = 0;
            for (CimProject project : projects) {
                for (CimIssueType issueType : project.getIssueTypes()) {
                    typesFound++;
                    System.out.println("Issue Type ID: " + issueType.getId());
                    System.out.println("Name: " + issueType.getName());
                    System.out.println("Description:");
                    System.out.println(issueType.getDescription());
                    System.out.println();
                }
            }
            if (typesFound == 0) {
                System.out.println("No Issue Types found for project " + this.jiraOptions.getProjectKey());
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
