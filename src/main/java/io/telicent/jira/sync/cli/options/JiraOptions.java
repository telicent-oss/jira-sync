package io.telicent.jira.sync.cli.options;

import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.NotBlank;
import com.github.rvesse.airline.annotations.restrictions.RequireOnlyOne;
import com.github.rvesse.airline.annotations.restrictions.Required;
import io.telicent.jira.sync.client.EnhancedJiraRestClient;
import io.telicent.jira.sync.client.EnhancedJiraRestClientFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
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
    @RequireOnlyOne(tag = "jira-token")
    @com.github.rvesse.airline.annotations.restrictions.File(mustExist = true)
    private File jiraTokenFile;

    @Option(name = "--jira-token-env", title = "JiraTokenEnvVar", description = "Specifies an environment variable from which the API token can be read and used to authenticate to the JIRA API")
    @RequireOnlyOne(tag = "jira-token")
    @NotBlank
    private String jiraTokenEnv;

    private EnhancedJiraRestClient instance = null;

    public EnhancedJiraRestClient connect() {
        if (this.instance != null) {
            return this.instance;
        }

        JiraRestClientFactory factory = new EnhancedJiraRestClientFactory();
        String jiraToken = readJiraToken();
        this.instance = (EnhancedJiraRestClient) factory.create(URI.create(this.jiraUrl),
                                                                new BasicHttpAuthenticationHandler(this.jiraUsername,
                                                                                                   jiraToken));

        return this.instance;
    }

    /**
     * Reads in the JIRA API token to use
     *
     * @return JIRA API Token
     */
    private String readJiraToken() {
        if (this.jiraTokenFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(this.jiraTokenFile))) {
                return reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            String token = System.getenv(this.jiraTokenEnv);
            if (StringUtils.isBlank(token)) {
                throw new RuntimeException(
                        "Specified environment variable " + this.jiraTokenEnv + " is empty, no JIRA API Token available");
            }
            return token;
        }
    }

    /**
     * Gets the Base URL of the JIRA instance
     *
     * @return Base URL
     */
    public String getBaseUrl() {
        return this.jiraUrl;
    }
}
