package io.telicent.jira.sync.client.parser;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import io.telicent.jira.sync.client.model.BasicRemoteLink;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;

public class BasicRemoteLinkParser implements JsonObjectParser<BasicRemoteLink> {
    @Override
    public BasicRemoteLink parse(JSONObject json) throws JSONException {
        final URI selfUri = JsonParseUtil.getSelfUri(json);
        final Long id = json.getLong("id");
        return new BasicRemoteLink(selfUri,  id);
    }
}
