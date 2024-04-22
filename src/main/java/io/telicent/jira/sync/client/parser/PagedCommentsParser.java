package io.telicent.jira.sync.client.parser;

import io.telicent.jira.sync.client.model.Comment;

public class PagedCommentsParser extends AbstractWrappedArrayParser<Comment> {

    public PagedCommentsParser() {
        super(new CommentsParser(), "comments");
    }
}
