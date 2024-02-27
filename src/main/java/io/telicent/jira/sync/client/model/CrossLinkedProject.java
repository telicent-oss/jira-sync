package io.telicent.jira.sync.client.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class CrossLinkedProject {
    private final LinkedHashMap<String, String> links = new LinkedHashMap<>();
    private final Map<String, String> lastSyncedIds = new LinkedHashMap<>();

    @JsonAnyGetter
    public Map<String, String> getLinks() {
        return this.links;
    }

    @JsonAnySetter
    public void setLinks(String source, String target) {
        this.links.put(source, target);
    }
}
