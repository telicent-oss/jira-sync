package io.telicent.jira.sync.client.mapping.rules;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHLabel;

import java.util.Collection;
import java.util.Objects;

@Getter
public class LabelMatch extends AbstractMappingRule {
    private final String[] labels;

    public LabelMatch(long jiraIssueType, Collection<String> labelsToMatch) {
        super(jiraIssueType);
        Objects.requireNonNull(labelsToMatch);
        this.labels = labelsToMatch.toArray(new String[0]);
    }

    @JsonCreator
    public static LabelMatch of(@JsonProperty("jiraIssueType") long jiraIssueType,
                         @JsonProperty("labels") Collection<String> labelsToMatch, @JsonProperty("type") String type) {
        return new LabelMatch(jiraIssueType, labelsToMatch);
    }

    @Override
    public String type() {
        return "label";
    }

    @Override
    public boolean matches(GHIssue issue) {
        for (GHLabel label : issue.getLabels()) {
            if (StringUtils.equalsAnyIgnoreCase(label.getName(), this.labels)) {
                return true;
            }
        }
        return false;
    }
}
