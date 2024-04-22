package io.telicent.jira.sync.client.generator;

import com.atlassian.jira.rest.client.internal.json.gen.JsonGenerator;
import io.telicent.jira.sync.client.model.CommentProperty;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.List;
import java.util.Map;

public class CommentPropertyGenerator implements JsonGenerator<CommentProperty> {
    private final MapGenerator mapGenerator = new MapGenerator();

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject generate(CommentProperty property) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("key", property.key());
        if (property.value() instanceof Map<?, ?> map) {
            json.put("value", this.mapGenerator.generate((Map<String, ?>) map));
        } else if (property.value() instanceof List<?> list) {
            json.put("value", this.mapGenerator.generateList((List<Object>) list));
        } else {
            json.put("value", property.value());
        }

        return json;
    }
}
