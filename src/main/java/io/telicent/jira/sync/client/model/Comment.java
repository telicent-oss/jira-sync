package io.telicent.jira.sync.client.model;

import com.atlassian.adf.model.node.Doc;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.Visibility;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

import java.net.URI;
import java.util.List;
import java.util.Objects;

public class Comment extends com.atlassian.jira.rest.client.api.domain.Comment {
    private final Doc body;
    private final List<CommentProperty> properties;

    public Comment(@Nullable URI self, @Nullable Doc body, @Nullable List<CommentProperty> properties,
                   @Nullable BasicUser author, @Nullable BasicUser updateAuthor, @Nullable DateTime creationDate,
                   @Nullable DateTime updateDate, @Nullable Visibility visibility, @Nullable Long id) {
        super(self, null, author, updateAuthor, creationDate, updateDate, visibility, id);

        this.body = body;
        this.properties = properties;
    }


    /**
     * Gets the comment body as an Atlassian Document
     *
     * @return Body document
     */
    public Doc getBodyDocument() {
        return this.body;
    }

    /**
     * Gets the comment properties (if any)
     *
     * @return Properties
     */
    public List<CommentProperty> getProperties() {
        return this.properties;
    }

    /**
     * Gets the comment property with the specific key (if present)
     * @param key Key
     * @return Comment Property, or {@code null} if no such property
     */
    public CommentProperty getProperty(String key) {
        if (this.properties == null) {
            return null;
        }
        return this.properties.stream().filter(p -> Objects.equals(p.key(), key)).findFirst().orElse(null);
    }
}
