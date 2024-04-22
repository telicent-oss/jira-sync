package io.telicent.jira.sync.cli.commands.issues;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import io.atlassian.util.concurrent.Promise;
import io.telicent.jira.sync.cli.commands.JiraSyncCommand;
import io.telicent.jira.sync.client.AsynchronousIssueCommentsClient;
import io.telicent.jira.sync.client.EnhancedJiraRestClient;
import io.telicent.jira.sync.client.model.Comment;
import org.apache.commons.collections4.IterableUtils;

import java.io.IOException;
import java.util.stream.Collectors;

@Command(name = "jira-comments")
public class Comments extends JiraSyncCommand {

    @Option(name = {
            "--jira-issue", "--jira-issue-id", "--jira-issue-key"
    }, title = "JiraIssueKey", description = "Specifies the key for a specific JIRA Issue that this command should operate over e.g. EXAMPLE-1234")
    @Required
    private String issueKey;

    @Override
    public int run() {
        try (EnhancedJiraRestClient jira = this.jiraOptions.connect()) {
            AsynchronousIssueCommentsClient commentsClient = jira.getCommentsClient();

            Promise<Iterable<Comment>> commentsPromise = commentsClient.getComments(this.issueKey);
            Iterable<Comment> comments = commentsPromise.claim();

            Promise<Iterable<Comment>> commentsByIdPromise = commentsClient.getCommentsByIds(
                    IterableUtils.toList(comments).stream().map(c -> c.getId().toString()).toList());
            comments = commentsByIdPromise.claim();

            for (Comment comment : comments) {
                System.out.println("Author: " + comment.getAuthor().getDisplayName());
                System.out.println("Created: " + comment.getCreationDate());
                System.out.println("Body: ");
                System.out.println(comment.getBodyDocument().toPlainText());
                if (comment.getProperties() != null) {
                    System.out.println("Has Properties: ");
                    comment.getProperties().stream().forEach(p -> System.out.println("  " + p.key()));
                }
                System.out.println();
            }

            return 0;
        } catch (RestClientException e) {
            this.reportJiraRestError(e);
            return 1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
