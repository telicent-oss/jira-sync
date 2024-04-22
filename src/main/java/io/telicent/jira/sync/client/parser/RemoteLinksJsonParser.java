package io.telicent.jira.sync.client.parser;

import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.rest.client.internal.json.GenericJsonArrayParser;
import com.atlassian.jira.rest.client.internal.json.JsonArrayParser;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

public class RemoteLinksJsonParser extends AbstractArrayParser<RemoteIssueLink> {

    public RemoteLinksJsonParser() {
        super(new RemoteLinkJsonParser());
    }
}
