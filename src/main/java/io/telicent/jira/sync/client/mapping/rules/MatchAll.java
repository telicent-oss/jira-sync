package io.telicent.jira.sync.client.mapping.rules;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.kohsuke.github.GHIssue;

/**
 * A rule which matches all issues
 */
public class MatchAll extends AbstractMappingRule {
    public MatchAll(long jiraIssueType) {
        super(jiraIssueType);
    }

    @JsonCreator
    public static MatchAll of(@JsonProperty("jiraIssueType") long jiraIssueType, @JsonProperty("type") String type) {
        return new MatchAll(jiraIssueType);
    }

    @Override
    public String type() {
        return "match-all";
    }

    @Override
    public boolean matches(GHIssue issue) {
        return true;
    }
}
