package io.telicent.jira.sync.client.generator;

import com.atlassian.jira.rest.client.internal.json.gen.JsonGenerator;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * A JSON Generator that deals with complex maps
 */
public class MapGenerator implements JsonGenerator<Map<String, ?>> {

    @Override
    @SuppressWarnings("unchecked")
    public JSONObject generate(Map<String, ?> map) throws JSONException {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map<?, ?> childMap) {
                json.put(entry.getKey(), generate((Map<String, ?>) childMap));
            } else if (entry.getValue() instanceof List<?> list) {
                json.put(entry.getKey(), generateList((List<Object>) list));
            } else {
                json.put(entry.getKey(), entry.getValue());
            }
        }

        return json;
    }

    @SuppressWarnings("unchecked")
    public JSONArray generateList(List<Object> list) throws JSONException {
        JSONArray json = new JSONArray();
        for (Object obj : list) {
            if (obj instanceof Map<?, ?> childMap) {
                json.put(generate((Map<String, ?>) childMap));
            } else if (obj instanceof List<?>) {
                json.put(generateList(list));
            } else {
                json.put(obj);
            }
        }

        return json;
    }
}
