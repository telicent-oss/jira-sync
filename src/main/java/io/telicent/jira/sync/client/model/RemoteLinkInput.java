package io.telicent.jira.sync.client.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RemoteLinkInput {
    private final String url, title, summary, globalId;

    public RemoteLinkInput(String url, String title, String summary, String globalId) {
        this.url = url;
        this.title = title;
        this.summary = summary;
        this.globalId = globalId;
    }
}
