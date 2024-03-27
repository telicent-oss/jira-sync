package io.telicent.jira.sync.cli.commands;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.github.rvesse.airline.annotations.AirlineModule;
import io.telicent.jira.sync.cli.options.JiraOptions;
import org.apache.commons.lang3.StringUtils;


public abstract class JiraSyncCommand extends SyncCommand {

    @AirlineModule
    protected JiraOptions jiraOptions = new JiraOptions();

    protected void reportJiraRestError(RestClientException e) {
        if (e.getErrorCollections()
             .stream()
             .flatMap(c -> c.getErrorMessages().stream())
             .anyMatch(m -> StringUtils.containsIgnoreCase(m,
                                                           "'" + this.jiraOptions.getProjectKey() + "' does not exist"))) {
            System.out.println(
                    this.jiraOptions.getProjectKey() + " does not appear to be a valid JIRA Project Key");
            System.out.println(
                    "NB: This error may occur if you are computing links for a JIRA Project that the provided JIRA Credentials do not have access to");
        } else {
            System.out.println(
                    "Failed to search for issues in JIRA Project " + this.jiraOptions.getProjectKey());
            e.getErrorCollections()
             .stream()
             .flatMap(c -> c.getErrorMessages().stream())
             .forEach(m -> System.out.println("  " + m));
        }
    }
}
