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
     * @throws IOException
     */
    public static StringBuilder buildPreamble(GHUser user, Date createdAt, Date updatedAt, String action,
                                              String originalUrl) throws IOException {
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

    private static String getUsername(GHUser user) throws IOException {
        if (user.getName() != null) {
            return user.getName();
        } else if (user.getLogin() != null) {
            return user.getLogin();
        } else {
            return Long.toString(user.getId());
        }
    }

    public static Object translateLabels(GHIssue issue) {
        List<String> labels = new ArrayList<>();
        for (GHLabel label : issue.getLabels()) {
            labels.add(label.getName());
        }
        return labels;
    }
}
