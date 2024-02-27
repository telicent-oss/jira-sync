package io.telicent.jira.sync.client;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class EnhancedJiraRestClient extends AsynchronousJiraRestClient {

    private final AsynchronousRemoteLinksClient remoteLinksClient;
    private final EnhancedIssuesRestClient enhancedIssuesClient;

    public EnhancedJiraRestClient(URI serverUri,
                                  DisposableHttpClient httpClient) {
        super(serverUri, httpClient);

        URI baseUri = UriBuilder.fromUri(serverUri).path("/rest/api/latest").build();
        this.remoteLinksClient = new AsynchronousRemoteLinksClient(baseUri, httpClient);

        URI v3Uri = UriBuilder.fromUri(serverUri).path("/rest/api/3").build();
        this.enhancedIssuesClient = new EnhancedIssuesRestClient(v3Uri, httpClient, this.getSessionClient(), this.getMetadataClient());
    }

    public AsynchronousRemoteLinksClient getRemoteLinksClient() {
        return this.remoteLinksClient;
    }

    @Override
    public IssueRestClient getIssueClient() {
        return this.enhancedIssuesClient;
    }
}
