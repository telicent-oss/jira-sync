package io.telicent.jira.sync.client.parser;

import com.atlassian.jira.rest.client.internal.json.GenericJsonArrayParser;
import com.atlassian.jira.rest.client.internal.json.JsonArrayParser;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

public class AbstractArrayParser<T> implements JsonArrayParser<Iterable<T>> {
    private final GenericJsonArrayParser<T> itemParser;

    public AbstractArrayParser(JsonObjectParser<T> parser) {
        this.itemParser = new GenericJsonArrayParser<>(parser);
    }

    @Override
    public Iterable<T> parse(JSONArray jsonArray) throws JSONException {
        return this.itemParser.parse(jsonArray);
    }
}
