package io.telicent.jira.sync.client.parser;

import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class RemoteLinkJsonParser implements JsonObjectParser<RemoteIssueLink> {
    @Override
    public RemoteIssueLink parse(JSONObject json) throws JSONException {
        RemoteIssueLinkBuilder builder = new RemoteIssueLinkBuilder();
        // TODO Parse application, icon and status information
        builder.id(json.getLong("id"))
               .globalId(JsonParseUtil.getOptionalString(json,"globalId"))
               .relationship(JsonParseUtil.getOptionalString(json, "relationship"));
        JSONObject object = json.getJSONObject("object");
        builder.url(object.getString("url"))
               .summary(JsonParseUtil.getOptionalString(object,"summary"))
               .title(object.getString("title"));
        return builder.build();
    }
}
