package io.telicent.jira.sync.client.parser;

import com.atlassian.adf.model.node.Doc;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class DocParser implements JsonObjectParser<Doc> {

    private final MapParser mapParser = new MapParser();

    @Override
    public Doc parse(JSONObject json) throws JSONException {
        return Doc.parse(this.mapParser.parse(json));
    }
}
