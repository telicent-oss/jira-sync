package io.telicent.jira.sync.client.parser;

import io.telicent.jira.sync.client.model.Comment;

public class CommentsParser extends AbstractArrayParser<Comment> {
    public CommentsParser() {
        super(new CommentParser());
    }
}
