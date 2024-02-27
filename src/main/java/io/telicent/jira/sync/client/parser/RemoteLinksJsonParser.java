package io.telicent.jira.sync.client.parser;

import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.rest.client.internal.json.GenericJsonArrayParser;
import com.atlassian.jira.rest.client.internal.json.JsonArrayParser;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

public class RemoteLinksJsonParser implements JsonArrayParser<Iterable<RemoteIssueLink>> {
    private final GenericJsonArrayParser<RemoteIssueLink>
            linksParser = new GenericJsonArrayParser(new RemoteLinkJsonParser());

    @Override
    public Iterable<RemoteIssueLink> parse(JSONArray jsonArray) throws JSONException {
        return this.linksParser.parse(jsonArray);
    }
}
