package io.telicent.jira.sync.client.model;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class CrossLinks {
    private final CrossLinkedProject jiraToGitHub = new CrossLinkedProject();
    private final CrossLinkedProject gitHubToJira = new CrossLinkedProject();

    /**
     * Adds a cross-link for the given project
     *
     * @param jiraId  Source
     * @param githubId  Target
     */
    public void addCrossLink(String jiraId, String githubId) {
        this.addCrossLink(this.jiraToGitHub, jiraId, githubId);
        this.addCrossLink(this.gitHubToJira, githubId, jiraId);
    }

    private void addCrossLink(CrossLinkedProject project, String source, String target) {
        project.setLinks(source, target);
    }
}
