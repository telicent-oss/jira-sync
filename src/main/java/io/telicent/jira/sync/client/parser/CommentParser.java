package io.telicent.jira.sync.client.parser;

import com.atlassian.adf.model.node.Doc;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.Visibility;
import com.atlassian.jira.rest.client.internal.json.CommentJsonParser;
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
    private final CommentJsonParser baseCommentParser = new CommentJsonParser();

    @Override
    public Comment parse(JSONObject json) throws JSONException {
        // Parse the base comment
        com.atlassian.jira.rest.client.api.domain.Comment baseComment = this.baseCommentParser.parse(json);

        // Then parse the extra fields we care about/parse and represent differently versus the base parser
        final Doc body = this.docParser.parse(json.getJSONObject("body"));
        List<CommentProperty> properties = null;
        Optional<JSONArray> propArray = JsonParseUtil.getOptionalArray(json, "properties");
        if (propArray.isPresent()) {
            properties = this.commentPropertyParser.parseList(propArray.get());
        }

        return new Comment(baseComment.getSelf(), body, properties, baseComment.getAuthor(),
                           baseComment.getUpdateAuthor(), baseComment.getCreationDate(), baseComment.getUpdateDate(),
                           baseComment.getVisibility(), baseComment.getId());
    }
}
