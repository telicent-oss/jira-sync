package io.telicent.jira.sync.cli.options;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.RequireOnlyOne;
import io.telicent.jira.sync.client.mapping.MappingRules;
import org.kohsuke.github.GHIssue;

import java.io.File;
import java.io.IOException;

public class JiraIssueTypeMappingOptions {

    @Option(name = "--jira-issue-type", title = "JiraIssueTypeId", description = "Specifies the ID of the JIRA Issue Type the GitHub Issues should be synchronised to JIRA as.")
    @RequireOnlyOne(tag = "issue-type-mappings")
    private int jiraIssueType;

    @Option(name = "--jira-issue-mappings", title = "JiraIssueMappingRulesFile", description = "Specifies a file containing a set of mapping rules for determining what JIRA Issue Type should be used when synchronising a GitHub Issue with a JIRA issue.")
    @RequireOnlyOne(tag = "issue-type-mappings")
    @com.github.rvesse.airline.annotations.restrictions.File(mustExist = true)
    private File jiraIssueMappingRules;

    private MappingRules rules;

    /**
     * Gets the JIRA Issue Type ID to use for the given GitHub Issue
     *
     * @param issue GitHub Issue
     * @return JIRA Issue Type ID
     */
    public long getJiraIssueType(GHIssue issue) {
        if (this.jiraIssueMappingRules != null) {
            if (this.rules == null) {
                try {
                    this.rules = new YAMLMapper().readValue(this.jiraIssueMappingRules, MappingRules.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return this.rules.selectJiraIssueType(issue);
        } else {
            return this.jiraIssueType;
        }
    }
}
