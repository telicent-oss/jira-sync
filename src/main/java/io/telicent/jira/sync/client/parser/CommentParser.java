package io.telicent.jira.sync.client.parser;

import com.atlassian.adf.model.node.Doc;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.Visibility;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import com.atlassian.jira.rest.client.internal.json.VisibilityJsonParser;
import com.google.common.base.Optional;
import io.telicent.jira.sync.client.model.Comment;
import io.telicent.jira.sync.client.model.CommentProperty;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;
import java.util.List;

public class CommentParser implements JsonObjectParser<Comment> {
    private final VisibilityJsonParser visParser = new VisibilityJsonParser();
    private final DocParser docParser = new DocParser();
    private final CommentPropertyParser commentPropertyParser = new CommentPropertyParser();

    @Override
    public Comment parse(JSONObject json) throws JSONException {
        final URI selfUri = JsonParseUtil.getSelfUri(json);
        final Long id = JsonParseUtil.getOptionalLong(json, "id");
        final Doc body = this.docParser.parse(json.getJSONObject("body"));
        final BasicUser author = JsonParseUtil.parseBasicUser(json.optJSONObject("author"));
        final BasicUser updateAuthor = JsonParseUtil.parseBasicUser(json.optJSONObject("updateAuthor"));
        final Visibility visibility = visParser.parseVisibility(json);
        List<CommentProperty> properties = null;
        Optional<JSONArray> propArray = JsonParseUtil.getOptionalArray(json, "properties");
        if (propArray.isPresent()) {
            properties = this.commentPropertyParser.parseList(propArray.get());
        }

        return new Comment(selfUri, body, properties, author, updateAuthor,
                           JsonParseUtil.parseDateTime(json.getString("created")),
                           JsonParseUtil.parseDateTime(json.getString("updated")), visibility, id);
    }
}
