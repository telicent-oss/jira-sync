package io.telicent.jira.sync.client.mapping;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.telicent.jira.sync.client.mapping.rules.LabelMatch;
import io.telicent.jira.sync.client.mapping.rules.MatchAll;
import io.telicent.jira.sync.client.mapping.rules.TitleMatch;
import lombok.*;
import org.kohsuke.github.GHIssue;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MappingRules {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
    @JsonSubTypes(value = {
            @JsonSubTypes.Type(name = "match-all", value = MatchAll.class),
            @JsonSubTypes.Type(name = "label", value = LabelMatch.class),
            @JsonSubTypes.Type(name = "title", value = TitleMatch.class)
    })
    private List<IssueMappingRule> rules;

    private int defaultJiraIssueType;

    /**
     * Select the JIRA Issue Type ID to use for the given GitHub Issue
     *
     * @param issue GitHub Issue
     * @return JIRA Issue Type ID
     */
    public long selectJiraIssueType(GHIssue issue) {
        for (IssueMappingRule rule : this.rules) {
            if (rule.matches(issue)) {
                return rule.jiraIssueType();
            }
        }
        return this.defaultJiraIssueType;
    }
}
