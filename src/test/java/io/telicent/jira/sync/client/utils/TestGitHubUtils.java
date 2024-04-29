package io.telicent.jira.sync.client.utils;

import io.telicent.jira.sync.utils.GitHubUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestGitHubUtils {

    @DataProvider(name = "labels")
    private Object[][] sampleLabels() {
        return new Object[][] {
                { "test", "test"},
                { "foo/bar", "foo/bar"},
                { "good first issue", "good-first-issue" },
                { "good-first-issue", "good-first-issue"},
                { "abc123", "abc123", }
        };
    }

    @Test(dataProvider = "labels")
    public void givenLabel_whenSanitisingForJira_thenSafeForJira(String rawLabel, String expected) {
        // When
        String sanitised = GitHubUtils.sanitiseForJira(rawLabel);

        // Then
        Assert.assertEquals(sanitised, expected);
    }
}
