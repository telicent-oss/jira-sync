package io.telicent.jira.sync.client.generator;

import com.atlassian.adf.model.node.Doc;
import com.atlassian.jira.rest.client.internal.json.gen.JsonGenerator;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class DocGenerator implements JsonGenerator<Doc> {

    private final MapGenerator mapGenerator = new MapGenerator();

    @Override
    public JSONObject generate(Doc doc) throws JSONException {
        return this.mapGenerator.generate(doc.toMap());
    }
}
