package io.telicent.jira.sync.client;

import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;

import java.net.URI;

public class EnhancedJiraRestClientFactory extends AsynchronousJiraRestClientFactory {
    @Override
    public JiraRestClient create(URI serverUri, AuthenticationHandler authenticationHandler) {
        DisposableHttpClient
                httpClient = (new AsynchronousHttpClientFactory()).createClient(serverUri, authenticationHandler);
        return new EnhancedJiraRestClient(serverUri, httpClient);
    }
}
