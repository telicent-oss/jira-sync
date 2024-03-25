package io.telicent.jira.sync.client.mapping.rules;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHIssue;

import java.util.Collection;
import java.util.Objects;

@Getter
public class TitleMatch extends AbstractMappingRule {

    private final String[] searchTerms;

    public TitleMatch(long jiraIssueType, Collection<String> searchTerms) {
        super(jiraIssueType);
        Objects.requireNonNull(searchTerms);
        this.searchTerms = searchTerms.toArray(new String[0]);
    }

    @JsonCreator
    public static TitleMatch of(@JsonProperty("jiraIssueType") long jiraIssueType,
                                @JsonProperty("searchTerms") Collection<String> searchTerms,
                                @JsonProperty("type") String type) {
        return new TitleMatch(jiraIssueType, searchTerms);
    }

    @Override
    public String type() {
        return "title";
    }

    @Override
    public boolean matches(GHIssue issue) {
        for (String searchTerm : this.searchTerms) {
            if (StringUtils.containsIgnoreCase(issue.getTitle(), searchTerm)) {
                return true;
            }
        }
        return false;
    }
}
