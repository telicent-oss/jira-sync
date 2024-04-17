package io.telicent.jira.sync.client.model;

import com.atlassian.adf.model.node.Doc;
import com.atlassian.jira.rest.client.api.domain.Visibility;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommentInput extends Comment {
    /**
     * Creates a new comment input
     *
     * @param body       Comment body
     * @param visibility Comment visibility
     */
    public CommentInput(Doc body, List<CommentProperty> properties, @Nullable Visibility visibility) {
        super(null, body, properties, null, null, null, null, visibility, null);
    }
}
