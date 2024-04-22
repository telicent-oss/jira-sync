package io.telicent.jira.sync.cli.options;

import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.NotBlank;
import com.github.rvesse.airline.annotations.restrictions.Required;

public class JiraProjectOptions extends JiraOptions{
    @Option(name = "--jira-project-key", title = "JiraProjectKey", description = "Specifies the name of the JIRA Project Key for the JIRA project you want to sync against")
    @Required
    @NotBlank
    private String jiraProjectKey;

    /**
     * Gets the JIRA Project Key for the project the user wants to sync against
     *
     * @return JIRA Project Key
     */
    public String getProjectKey() {
        return this.jiraProjectKey;
    }
}
