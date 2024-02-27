package io.telicent.jira.sync.client;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.rest.client.internal.async.AbstractAsynchronousRestClient;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import io.atlassian.util.concurrent.Promise;
import io.telicent.jira.sync.client.generator.RemoteLinkJsonGenerator;
import io.telicent.jira.sync.client.model.BasicRemoteLink;
import io.telicent.jira.sync.client.model.RemoteLinkInput;
import io.telicent.jira.sync.client.parser.BasicRemoteLinkParser;
import io.telicent.jira.sync.client.parser.RemoteLinkJsonParser;
import io.telicent.jira.sync.client.parser.RemoteLinksJsonParser;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class AsynchronousRemoteLinksClient extends AbstractAsynchronousRestClient {

    private final RemoteLinkJsonParser remoteLinkParser = new RemoteLinkJsonParser();
    private final RemoteLinksJsonParser remoteLinksParser = new RemoteLinksJsonParser();
    private final RemoteLinkJsonGenerator remoteLinkGenerator = new RemoteLinkJsonGenerator();
    private final BasicRemoteLinkParser basicRemoteLinkParser = new BasicRemoteLinkParser();

    private final URI baseUri;

    public AsynchronousRemoteLinksClient(URI baseUri, HttpClient client) {
        super(client);
        this.baseUri = baseUri;
    }

    public Promise<Iterable<RemoteIssueLink>> getRemoteLinks(String issueKey) {
        UriBuilder uriBuilder = UriBuilder.fromUri(this.baseUri);
        uriBuilder.path("issue").path(issueKey).path("remotelink");
        return this.getAndParse(uriBuilder.build(), this.remoteLinksParser);
    }

    public Promise<RemoteIssueLink> getRemoteLink(String issueKey, Long id) {
        UriBuilder uriBuilder = UriBuilder.fromUri(this.baseUri);
        uriBuilder.path("issue").path(issueKey).path("remotelink").path(Long.toString(id));
        return this.getAndParse(uriBuilder.build(), this.remoteLinkParser);
    }

    public Promise<BasicRemoteLink> createOrUpdateRemoteLink(String issueKey, RemoteLinkInput input) {
        UriBuilder uriBuilder = UriBuilder.fromUri(this.baseUri);
        uriBuilder.path("issue").path(issueKey).path("remotelink");
        return this.postAndParse(uriBuilder.build(), input, this.remoteLinkGenerator, this.basicRemoteLinkParser);
    }

}
