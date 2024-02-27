package io.telicent.jira.sync.cli.commands;

import com.atlassian.adf.jackson2.AdfJackson2;
import com.atlassian.adf.markdown.MarkdownParser;
import com.atlassian.adf.model.node.Doc;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.IssueFieldId;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import io.atlassian.util.concurrent.Promise;
import io.telicent.jira.sync.cli.options.CrossLinkOptions;
import io.telicent.jira.sync.client.AsynchronousRemoteLinksClient;
import io.telicent.jira.sync.client.EnhancedJiraRestClient;
import io.telicent.jira.sync.client.model.BasicRemoteLink;
import io.telicent.jira.sync.client.model.CrossLinks;
import io.telicent.jira.sync.client.model.RemoteLinkInput;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Command(name = "to-jira", description = "Command for synchronising a single GitHub Issue to JIRA")
public class OneGitHubToJira extends JiraGitHubSyncCommand {

    private static final TypeReference<Map<String, Object>> GENERIC_MAP_TYPE = new TypeReference<>() {
    };

    @Option(name = "--github-issue-id", title = "GitHubIssueID", description = "Specifies the ID of the GitHub Issue that you wish to sync to JIRA")
    @Required
    private int ghIssueId;

    @Option(name = "--github-repository", title = "GitHubRepository", description = "Specifies the GitHub Repository whose issue you want to sync to JIRA")
    @Required
    private String ghRepo;

    @Option(name = "--jira-issue-type", title = "JiraIssueTypeId", description = "Specifies the ID of the JIRA Issue Type the GitHub Issues should be synchronised to JIRA as")
    @Required
    private int jiraIssueType;

    @AirlineModule
    private CrossLinkOptions crossLinkOptions = new CrossLinkOptions();

    @Override
    public int run() {
        try {
            GitHub gitHub = this.gitHubOptions.connect();
            GHRepository repository = gitHub.getRepository(this.ghRepo);
            if (repository == null) {
                throw new RuntimeException(
                        "GitHub Repository " + this.ghRepo + " does not exist, or your provided GitHub Credentials do not permit access to it");
            }
            GHIssue issue = repository.getIssue(this.ghIssueId);
            if (issue == null) {
                throw new RuntimeException(
                        "GitHub Repository " + this.ghRepo + " does not contain Issue ID " + this.ghIssueId);
            }

            try (EnhancedJiraRestClient jiraRestClient = this.jiraOptions.connect()) {
                String gitHubIssueId = this.ghRepo + "/" + this.ghIssueId;
                CrossLinks crossLinks = this.crossLinkOptions.loadCrossLinks();
                String jiraKey = crossLinks.getGitHubToJira().getLinks().get(gitHubIssueId);

                // Prepare the JIRA Issue content
                IssueRestClient issues = jiraRestClient.getIssueClient();
                IssueInput input = new IssueInputBuilder().setIssueTypeId((long) this.jiraIssueType)
                                                          .setProjectKey(this.jiraOptions.getProjectKey())
                                                          .setFieldInput(new FieldInput(IssueFieldId.DESCRIPTION_FIELD, translateMarkdownToAdf(issue)))
                                                          //.setDescription(translateMarkdownToAdf(issue))
                                                          .setSummary(issue.getTitle())
                                                          // TODO Copy assignee where relevant
                                                          // TODO Figure out how to copy labels across
                                                          .build();

                if (StringUtils.isNotBlank(jiraKey)) {
                    System.out.println("GitHub Issue " + gitHubIssueId + " syncs to existing JIRA Issue " + jiraKey);
                    Promise<Void> updated = issues.updateIssue(jiraKey, input);
                    updated.claim();
                    System.out.println("Updated JIRA Issue " + jiraKey);
                } else {
                    // Need to create a new JIRA Issue
                    Promise<BasicIssue> creation = issues.createIssue(input);
                    BasicIssue created = creation.claim();
                    System.out.println("Created new JIRA Issue " + created.getKey());
                    crossLinks.getGitHubToJira().setLinks(gitHubIssueId, created.getKey());
                    crossLinks.getJiraToGitHub().setLinks(created.getKey(), gitHubIssueId);

                    // TODO If there's a JIRA Key mentioned in the issue automatically add issue links?

                    AsynchronousRemoteLinksClient remoteLinksClient = jiraRestClient.getRemoteLinksClient();
                    RemoteLinkInput remoteLink = RemoteLinkInput.builder()
                                                                .url(issue.getHtmlUrl().toString())
                                                                .title("GitHub Issue " + gitHubIssueId)
                                                                .globalId("github:" + gitHubIssueId)
                                                                .build();
                    Promise<BasicRemoteLink> createdLinkPromise =
                            remoteLinksClient.createOrUpdateRemoteLink(created.getKey(), remoteLink);
                    BasicRemoteLink createdLink = createdLinkPromise.claim();
                    System.out.println(
                            "Associated GitHub Issue with JIRA Issue via Remote Link ID " + createdLink.getId());
                }

                // TODO Sync Issue Comments

                return 0;
            }
        } catch (GHFileNotFoundException e) {
            throw new RuntimeException(
                    "GitHub Repository " + this.ghRepo + " does not exist, or your provided GitHub Credentials do not permit access to it");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object translateMarkdownToAdf(GHIssue issue) throws JsonProcessingException {
        MarkdownParser parser = new MarkdownParser();
        Doc doc = parser.unmarshall(issue.getBody());
        AdfJackson2 jackson = new AdfJackson2();
        String json = jackson.marshall(doc);
        Map<String, Object> map = new ObjectMapper().readValue(json, GENERIC_MAP_TYPE);
        return new ComplexIssueInputFieldValue(map);
    }

    private static ComplexIssueInputFieldValue transformMap(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map<?, ?>) {
                Map<String, Object> child = (Map<String, Object>) entry.getValue();
                entry.setValue(transformMap(child));
            } else if (entry.getValue() instanceof List<?>) {
                List<Object> child = (List<Object>) entry.getValue();
                entry.setValue(transformList(child));
            }
        }
        return new ComplexIssueInputFieldValue(map);
    }

    private static List<Object> transformList(List<Object> list) {
        List<Object> transformed = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?>) {
                Map<String, Object> child = (Map<String, Object>) item;
                transformed.add(transformMap(child));
            } else if (item instanceof List<?>) {
                List<Object> child = (List<Object>) item;
                transformed.add(transformList(child));
            } else {
                transformed.add(item);
            }
        }
        return transformed;
    }
}
