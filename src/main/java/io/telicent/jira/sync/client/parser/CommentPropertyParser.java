package io.telicent.jira.sync.client.parser;

import com.atlassian.jira.rest.client.internal.json.GenericJsonArrayParser;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import io.telicent.jira.sync.client.model.CommentProperty;
import org.apache.commons.collections4.IterableUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.List;

public class CommentPropertyParser implements JsonObjectParser<CommentProperty> {
    private final GenericJsonArrayParser<CommentProperty> listParser = new GenericJsonArrayParser<>(this);
    private final MapParser mapParser = new MapParser();

    @Override
    public CommentProperty parse(JSONObject json) throws JSONException {
        String key = json.getString("key");
        Object value = this.mapParser.parseValue(json.get("value"));
        return new CommentProperty(key, value);
    }

    public List<CommentProperty> parseList(JSONArray json) throws JSONException {
        Iterable<CommentProperty> iterable = this.listParser.parse(json);
        return IterableUtils.toList(iterable);
    }
}
