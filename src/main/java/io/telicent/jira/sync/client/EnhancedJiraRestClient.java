package io.telicent.jira.sync.client;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class EnhancedJiraRestClient extends AsynchronousJiraRestClient {

    private final AsynchronousRemoteLinksClient remoteLinksClient;
    private final EnhancedIssuesRestClient enhancedIssuesClient;
    private final AsynchronousIssueCommentsClient commentsClient;

    public EnhancedJiraRestClient(URI serverUri,
                                  DisposableHttpClient httpClient) {
        super(serverUri, httpClient);

        URI baseUri = UriBuilder.fromUri(serverUri).path("/rest/api/latest").build();
        this.remoteLinksClient = new AsynchronousRemoteLinksClient(baseUri, httpClient);

        // For these API Clients want to explicitly use the v3 API
        URI v3Uri = UriBuilder.fromUri(serverUri).path("/rest/api/3").build();
        this.enhancedIssuesClient =
                new EnhancedIssuesRestClient(v3Uri, httpClient, this.getSessionClient(), this.getMetadataClient());
        this.commentsClient = new AsynchronousIssueCommentsClient(v3Uri, httpClient);
    }

    /**
     * Gets an API Client for manipulating issue remote links
     *
     * @return Issue Remote Links API Client
     */
    public AsynchronousRemoteLinksClient getRemoteLinksClient() {
        return this.remoteLinksClient;
    }

    /**
     * Gets an API Client for manipulating issue comments
     *
     * @return Issue Comments API Client
     */
    public AsynchronousIssueCommentsClient getCommentsClient() {
        return this.commentsClient;
    }

    @Override
    public IssueRestClient getIssueClient() {
        return this.enhancedIssuesClient;
    }
}
