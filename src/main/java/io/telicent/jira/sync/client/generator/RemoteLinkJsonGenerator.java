package io.telicent.jira.sync.client.generator;

import com.atlassian.jira.rest.client.internal.json.gen.JsonGenerator;
import io.telicent.jira.sync.client.model.RemoteLinkInput;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
public class RemoteLinkJsonGenerator implements JsonGenerator<RemoteLinkInput> {
    @Override
    public JSONObject generate(RemoteLinkInput link) throws JSONException {

        // TODO Convert other possible fields

        final JSONObject json = new JSONObject();
        if (StringUtils.isNotBlank(link.getGlobalId())) {
            json.put("globalId", link.getGlobalId());
        }
        JSONObject linkObject = new JSONObject();
        linkObject.put("url", link.getUrl());
        linkObject.put("title", link.getTitle());
        if (StringUtils.isNotBlank(link.getSummary())) {
            linkObject.put("summary", link.getSummary());
        }
        json.put("object", linkObject);
        return json;
    }
}
