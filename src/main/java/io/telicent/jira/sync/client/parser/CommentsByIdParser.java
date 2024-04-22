package io.telicent.jira.sync.client.parser;

import io.telicent.jira.sync.client.model.Comment;

public class CommentsByIdParser extends AbstractWrappedArrayParser<Comment> {

    public CommentsByIdParser() {
        super(new CommentsParser(), "values");
    }
}
