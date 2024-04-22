package io.telicent.jira.sync.utils;

import com.atlassian.adf.jackson2.AdfJackson2;
import com.atlassian.adf.markdown.MarkdownParser;
import com.atlassian.adf.model.node.Doc;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.telicent.jira.sync.client.model.Comment;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.kohsuke.github.GHIssue;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JiraUtils {

    public static final String COMMENT_PROPERTY_KEY = "io.telicent.jira-sync";
    private static final TypeReference<Map<String, Object>> GENERIC_MAP_TYPE = new TypeReference<>() {
    };
    public static final String GITHUB_COMMENT_ID_PROPERTY = "github.comment.id";

    public static String getJiraCommentId(String jiraKey, Comment created) {
        return String.format("%s/comments/%d", jiraKey, created.getId());
    }

    public static Object translateMarkdownToAdf(StringBuilder preamble, GHIssue issue) throws JsonProcessingException {
        Doc doc = translateMarkdownToAdfDocument(preamble + issue.getBody());
        AdfJackson2 jackson = new AdfJackson2();
        String json = jackson.marshall(doc);
        Map<String, Object> map = new ObjectMapper().readValue(json, GENERIC_MAP_TYPE);
        return new ComplexIssueInputFieldValue(map);
    }

    public static Doc translateMarkdownToAdfDocument(String markdown) {
        MarkdownParser parser = new MarkdownParser();
        return parser.unmarshall(markdown);
    }
}
