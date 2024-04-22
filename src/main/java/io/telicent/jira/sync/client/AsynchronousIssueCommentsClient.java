package io.telicent.jira.sync.client;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.internal.async.AbstractAsynchronousRestClient;
import io.atlassian.util.concurrent.Promise;
import io.telicent.jira.sync.client.generator.CommentGenerator;
import io.telicent.jira.sync.client.generator.MapGenerator;
import io.telicent.jira.sync.client.model.Comment;
import io.telicent.jira.sync.client.model.CommentInput;
import io.telicent.jira.sync.client.parser.CommentParser;
import io.telicent.jira.sync.client.parser.CommentsByIdParser;
import io.telicent.jira.sync.client.parser.PagedCommentsParser;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class AsynchronousIssueCommentsClient extends AbstractAsynchronousRestClient {

    private final CommentsByIdParser commentsByIdParser = new CommentsByIdParser();
    private final PagedCommentsParser commentsParser = new PagedCommentsParser();
    private final CommentParser commentParser = new CommentParser();
    private final CommentGenerator commentGenerator = new CommentGenerator();
    private final MapGenerator mapGenerator = new MapGenerator();
    private final URI baseUri;

    public AsynchronousIssueCommentsClient(URI baseUri, HttpClient client) {
        super(client);
        this.baseUri = baseUri;
    }

    private @NotNull UriBuilder buildCommentApiUri(String issueKey) {
        //@formatter:off
        return UriBuilder.fromUri(this.baseUri)
                         .path("issue")
                         .path(issueKey)
                         .path("comment");
        //@formatter:on
    }

    private @NotNull UriBuilder buildUriForComment(String issueKey, String commentId) {
        return buildCommentApiUri(issueKey).path(commentId);
    }

    public Promise<Iterable<Comment>> getComments(String issueKey) {
        UriBuilder uriBuilder = buildCommentApiUri(issueKey);
        return this.getAndParse(uriBuilder.build(), this.commentsParser);
    }

    public Promise<Iterable<Comment>> getCommentsByIds(List<String> commentIds) {
        UriBuilder uriBuilder =
                UriBuilder.fromUri(this.baseUri).path("comment").path("list").queryParam("expand", "properties");
        Map<String, Object> request = Map.of("ids", commentIds);
        return this.postAndParse(uriBuilder.build(), request, this.mapGenerator, this.commentsByIdParser);

    }

    public Promise<Comment> getComment(String issueKey, String commentId) {
        UriBuilder uriBuilder = buildUriForComment(issueKey, commentId);
        return this.getAndParse(uriBuilder.build(), this.commentParser);
    }


    public Promise<Comment> addComment(String issueKey, CommentInput commentInput) {
        UriBuilder uriBuilder = buildCommentApiUri(issueKey);
        return this.postAndParse(uriBuilder.build(), commentInput, this.commentGenerator, this.commentParser);
    }

    public Promise<Comment> updateComment(String issueKey, String commentId, CommentInput commentInput) {
        UriBuilder uriBuilder = buildUriForComment(issueKey, commentId);
        return this.putAndParse(uriBuilder.build(), commentInput, this.commentGenerator, this.commentParser);
    }

    public Promise<Void> deleteComment(String issueKey, String commentId) {
        UriBuilder uriBuilder = buildUriForComment(issueKey, commentId);
        return this.delete(uriBuilder.build());
    }
}
