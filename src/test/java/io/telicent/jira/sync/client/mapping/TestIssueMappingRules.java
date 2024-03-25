package io.telicent.jira.sync.client.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.telicent.jira.sync.client.mapping.rules.LabelMatch;
import io.telicent.jira.sync.client.mapping.rules.MatchAll;
import io.telicent.jira.sync.client.mapping.rules.TitleMatch;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHLabel;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestIssueMappingRules {

    public static final List<String> TITLE_SEARCH_TERMS = List.of("Feature", "Epic");
    public static final List<String> LABELS_TO_MATCH = List.of("bug");

    private static final ObjectMapper YAML = new YAMLMapper();

    private GHIssue createMockIssue(String title, String... labels) {
        GHIssue issue = mock(GHIssue.class);
        when(issue.getTitle()).thenReturn(title);
        List<GHLabel> ghLabels = new ArrayList<>();
        for (String label : labels) {
            GHLabel ghLabel = mock(GHLabel.class);
            when(ghLabel.getName()).thenReturn(label);
            ghLabels.add(ghLabel);
        }
        when(issue.getLabels()).thenReturn(ghLabels);
        when(issue.toString()).thenReturn(title + " (Labels: " + StringUtils.join(labels, ", "));
        return issue;
    }

    @Test
    public void givenAnIssue_whenUsingMatchAllRule_thenIssueIsMatched() {
        // Given
        GHIssue issue = createMockIssue("This is an issue", "bug");
        MatchAll rule = new MatchAll(0);

        // When and Then
        Assert.assertTrue(rule.matches(issue));
    }

    @DataProvider(name = "titleIssues")
    public Object[][] titleIssues() {
        return new Object[][] {
                { "A generic title", false },
                { "[EPIC] Some awesome new functionality", true },
                { "[Feature] Cool new feature", true },
                { "[Epic] Add a new feature", true },
                { "[Bug] Something is broken", false }
        };
    }

    @Test(dataProvider = "titleIssues")
    public void givenATitledIssue_whenUsingTitleMatchRule_thenIssueIsMatched(String title, boolean shouldMatch) {
        // Given
        GHIssue issue = createMockIssue(title);
        TitleMatch titleMatch = new TitleMatch(0, TITLE_SEARCH_TERMS);

        // When
        boolean matched = titleMatch.matches(issue);

        // Then
        Assert.assertEquals(matched, shouldMatch);
    }

    @DataProvider(name = "labelIssues")
    public Object[][] labelIssues() {
        return new Object[][] {
                { new String[] { "bug" }, true },
                { new String[] { "various", "other", "labels" }, false },
                { new String[] { "feature", "epic" }, false },
                { new String[] { "BUG" }, true },
                { new String[] { "enhancement" }, false }
        };
    }

    @Test(dataProvider = "labelIssues")
    public void givenALabelledIssue_whenUsingLabelMatchRule_thenIssueIsMatched(String[] labels, boolean shouldMatch) {
        // Given
        GHIssue issue = createMockIssue("Generic title", labels);
        LabelMatch labelMatch = new LabelMatch(0, LABELS_TO_MATCH);

        // When
        boolean matched = labelMatch.matches(issue);

        // Then
        Assert.assertEquals(matched, shouldMatch);
    }

    @DataProvider(name = "simpleIssues")
    public Object[][] simpleIssues() {
        return new Object[][] {
                { createMockIssue("This is a bug", "bug", "error", "security"), 1 },
                { createMockIssue("This is an enhancement", "enhancement", "new-feature"), 2 },
                { createMockIssue("[Epic] Add super awesome stuff"), 3 },
                { createMockIssue("[Feature] Add cool feature", "new-feature"), 3 },
                { createMockIssue("Some other issue", "security"), 4 }
        };
    }

    @Test(dataProvider = "simpleIssues")
    public void givenLoadedMappingRules_whenSelectingJiraIssueType_thenCorrectTypeIsSelected(GHIssue issue,
                                                                                             long expectedIssueType) throws
            IOException {
        try (InputStream input = TestIssueMappingRules.class.getResourceAsStream("/simple-rules.yaml")) {
            // Given
            MappingRules rules = YAML.readValue(input, MappingRules.class);

            // When
            long issueType = rules.selectJiraIssueType(issue);

            // Then
            Assert.assertEquals(issueType, expectedIssueType);
        }
    }
}
