package io.telicent.jira.sync.cli.commands;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.github.rvesse.airline.annotations.AirlineModule;
import io.telicent.jira.sync.cli.options.JiraOptions;
import io.telicent.jira.sync.cli.options.JiraProjectOptions;
import org.apache.commons.lang3.StringUtils;


public abstract class JiraSyncCommand extends SyncCommand {

    @AirlineModule
    protected JiraOptions jiraOptions = new JiraOptions();

    protected void reportJiraRestError(RestClientException e) {
        System.out.println("Failed to complete a JIRA REST API call:");
        e.getErrorCollections()
         .stream()
         .flatMap(c -> c.getErrorMessages().stream())
         .forEach(m -> System.out.println("  " + m));
    }
}
