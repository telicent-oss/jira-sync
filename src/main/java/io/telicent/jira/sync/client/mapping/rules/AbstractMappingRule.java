package io.telicent.jira.sync.client.mapping.rules;

import io.telicent.jira.sync.client.mapping.IssueMappingRule;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
public abstract class AbstractMappingRule implements IssueMappingRule {

    private final long jiraIssueType;

    public AbstractMappingRule(long jiraIssueType) {
        if (jiraIssueType < 0) throw new IllegalArgumentException("Jira Issue Type ID MUST be >= 0");
        this.jiraIssueType = jiraIssueType;
    }

    @Override
    public final long jiraIssueType() {
        return this.jiraIssueType;
    }
}
