package io.telicent.jira.sync.client.mapping;

import org.kohsuke.github.GHIssue;

public interface IssueMappingRule {

    /**
     * Gets the type of the mapping rule
     *
     * @return Type
     */
    String type();

    /**
     * Gets the JIRA Issue Type ID to use for issues matched by this rule
     *
     * @return Issue Type
     */
    long jiraIssueType();

    /**
     * Indicates whether this rule matches the given issue
     *
     * @param issue Issue
     * @return True if matched, false otherwise
     */
    boolean matches(GHIssue issue);
}
