package io.telicent.jira.sync.client.parser;

import com.atlassian.jira.rest.client.internal.json.JsonArrayParser;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class AbstractWrappedArrayParser<T> implements JsonObjectParser<Iterable<T>> {
    private final JsonArrayParser<Iterable<T>> arrayParser;
    private final String fieldToUnwrap;

    public AbstractWrappedArrayParser(JsonArrayParser<Iterable<T>> arrayParser, String fieldToUnwrap) {
        this.arrayParser = arrayParser;
        this.fieldToUnwrap = fieldToUnwrap;
    }

    @Override
    public Iterable<T> parse(JSONObject json) throws JSONException {
        return this.arrayParser.parse(json.getJSONArray(this.fieldToUnwrap));
    }
}
