package io.telicent.jira.sync.client;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.SessionRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousIssueRestClient;
import com.atlassian.jira.rest.client.internal.json.BasicIssueJsonParser;
import io.atlassian.util.concurrent.Promise;
import io.telicent.jira.sync.client.generator.EnhancedIssueInputJsonGenerator;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class EnhancedIssuesRestClient extends AsynchronousIssueRestClient {
    private final URI baseUri;
    private BasicIssueJsonParser basicIssueParser = new BasicIssueJsonParser();

    public EnhancedIssuesRestClient(URI baseUri, HttpClient client,
                                    SessionRestClient sessionRestClient,
                                    MetadataRestClient metadataRestClient) {
        super(baseUri, client, sessionRestClient, metadataRestClient);
        this.baseUri = baseUri;
    }

    @Override
    public Promise<BasicIssue> createIssue(IssueInput issue) {
        final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri).path("issue");
        return postAndParse(uriBuilder.build(), issue, new EnhancedIssueInputJsonGenerator(), basicIssueParser);
    }

    @Override
    public Promise<Void> updateIssue(String issueKey, IssueInput issue) {
        final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri).path("issue").path(issueKey);
        return put(uriBuilder.build(), issue, new EnhancedIssueInputJsonGenerator());
    }
}
