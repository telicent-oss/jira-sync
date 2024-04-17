package io.telicent.jira.sync.client.generator;

import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.rest.client.internal.json.gen.JsonGenerator;
import com.atlassian.jira.rest.client.internal.json.gen.VisibilityJsonGenerator;
import io.telicent.jira.sync.client.model.Comment;
import io.telicent.jira.sync.client.model.CommentProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class CommentGenerator implements JsonGenerator<Comment> {

    private final DocGenerator docGenerator = new DocGenerator();
    private final VisibilityJsonGenerator visGenerator = new VisibilityJsonGenerator();
    private final CommentPropertyGenerator commentPropertyGenerator = new CommentPropertyGenerator();

    @Override
    public JSONObject generate(Comment comment) throws JSONException {
        JSONObject json = new JSONObject();

        json.put("body", docGenerator.generate(comment.getBodyDocument()));
        if (comment.getVisibility() != null) {
            json.put("visibility", visGenerator.generate(comment.getVisibility()));
        }
        if (CollectionUtils.isNotEmpty(comment.getProperties())) {
            JSONArray properties = new JSONArray();
            for (CommentProperty p : comment.getProperties()) {
                properties.put(this.commentPropertyGenerator.generate(p));
            }
        }

        return json;
    }
}
