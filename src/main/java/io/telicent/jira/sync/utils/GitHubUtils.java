package io.telicent.jira.sync.utils;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GitHubUtils {
    /**
     * Builds a preamble Markdown text that links back to the original GitHub Issue/Comment
     *
     * @param user      User who created the issue/comment
     * @param createdAt When the issue/comment was created
     * @param updatedAt When the issue/comment was last updated
     * @return Preamble
     */
    public static StringBuilder buildPreamble(GHUser user, Date createdAt, Date updatedAt, String action,
                                              String originalUrl) {
        StringBuilder commentPreamble = new StringBuilder();
        if (user != null) {
            commentPreamble.append("GitHub User [")
                           .append(getUsername(user))
                           .append("](")
                           .append(user.getHtmlUrl())
                           .append(") [")
                           .append(action)
                           .append("](")
                           .append(originalUrl)
                           .append(") on ")
                           .append(createdAt.toInstant().toString());
            if (updatedAt != null && updatedAt.after(createdAt)) {
                commentPreamble.append(" and was last updated on ").append(updatedAt.toInstant().toString());
            }
            commentPreamble.append("\n\n");
        }
        return commentPreamble;
    }

    /**
     * Gets the most human-readable username available for a user
     *
     * @param user GitHub User
     * @return Human-readable username
     */
    private static String getUsername(GHUser user)  {
        try {
            if (user.getName() != null) {
                return user.getName();
            }
        } catch (IOException e) {
            // Ignore and fallback to other options
        }

        if (user.getLogin() != null) {
            return user.getLogin();
        } else {
            return Long.toString(user.getId());
        }
    }

    /**
     * Translates GitHub Issue labels into JIRA labels (which are simple strings)
     *
     * @param issue GitHub Issue
     * @return JIRA Labels
     */
    public static Object translateLabels(GHIssue issue) {
        List<String> labels = new ArrayList<>();
        for (GHLabel label : issue.getLabels()) {
            labels.add(label.getName());
        }
        return labels;
    }

    /**
     * Builds a comment message that links to the JIRA issue created for a GitHub issue
     *
     * @param jiraBaseUrl  JIRA Base URL
     * @param jiraIssueKey JIRA Issue Key
     * @return Comment message as Markdown
     */
    public static String buildCloseComment(String jiraBaseUrl, String jiraIssueKey) {
        StringBuilder builder = new StringBuilder();
        builder.append("This issue was synced to JIRA as [")
               .append(jiraIssueKey)
               .append("](")
               .append(jiraBaseUrl)
               .append("/browse/")
               .append(jiraIssueKey)
               .append(") and will be tracked and actioned there in future");
        return builder.toString();
    }
}
