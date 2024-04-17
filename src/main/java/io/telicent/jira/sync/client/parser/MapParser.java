package io.telicent.jira.sync.client.parser;

import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONString;

import java.util.*;

public class MapParser implements JsonObjectParser<Map<String, ?>> {
    @Override
    public Map<String, ?> parse(JSONObject json) throws JSONException {
        Map<String, Object> map = new LinkedHashMap<>();

        for (Iterator it = json.keys(); it.hasNext(); ) {
            String key = (String) it.next();
            Object value = json.get(key);
            map.put(key, parseValue(value));
        }

        return map;
    }

    public List<Object> parseList(JSONArray json) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            Object value = json.get(i);
            list.add(parseValue(value));
        }
        return list;
    }

    public Object parseValue(Object value) throws JSONException {
        if (value instanceof JSONObject obj) {
            return parse(obj);
        } else if (value instanceof JSONArray arr) {
            return parseList(arr);
        } else if (value instanceof JSONString str) {
            return str.toString();
        } else if (value instanceof String str) {
            return str;
        } else if (value instanceof Long l) {
            return l;
        } else if (value instanceof Integer i) {
            return i;
        } else if (value instanceof Boolean b) {
            return b;
        } else if (value instanceof Double d) {
            return d;
        } else {
            throw new JSONException("Unable to convert value of type " + value.getClass());
        }
    }
}
