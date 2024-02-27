package io.telicent.jira.sync.cli.options;

import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.NotBlank;
import com.github.rvesse.airline.annotations.restrictions.Required;
import io.telicent.jira.sync.client.EnhancedJiraRestClient;
import io.telicent.jira.sync.client.EnhancedJiraRestClientFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

/**
 * Options for connecting to JIRA
 */
public class JiraOptions {

    @Option(name = "--jira-url", title = "JiraUrl", description = "Specifies the URL for your JIRA Instance you want to sync against")
    @Required
    @NotBlank
    private String jiraUrl;

    @Option(name = "--jira-user", title = "JiraUsername", description = "Specifies the username to autenticate to JIRA with")
    @Required
    @NotBlank
    private String jiraUsername;

    @Option(name = "--jira-token-file", title = "JiraTokenFile", description = "Supplies a file from which an API token can be read and used to authenticate to the JIRA API")
    @Required
    @com.github.rvesse.airline.annotations.restrictions.File(mustExist = true)
    private File jiraTokenFile;

    @Option(name = "--jira-project-key", title = "JiraProjectKey", description = "Specifies the name of the JIRA Project Key for the JIRA project you want to sync against")
    @Required
    @NotBlank
    private String jiraProjectKey;

    private EnhancedJiraRestClient instance = null;

    public EnhancedJiraRestClient connect() {
        if (this.instance != null) {
            return this.instance;
        }

        JiraRestClientFactory factory = new EnhancedJiraRestClientFactory();
        try (BufferedReader reader = new BufferedReader(new FileReader(this.jiraTokenFile))) {
            this.instance = (EnhancedJiraRestClient) factory.create(URI.create(this.jiraUrl),
                                                                    new BasicHttpAuthenticationHandler(
                                                                            this.jiraUsername, reader.readLine()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this.instance;
    }

    /**
     * Gets the JIRA Project Key for the project the user wants to sync against
     *
     * @return JIRA Project Key
     */
    public String getProjectKey() {
        return this.jiraProjectKey;
    }
}
