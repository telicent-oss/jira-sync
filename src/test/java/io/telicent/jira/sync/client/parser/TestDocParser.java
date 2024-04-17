package io.telicent.jira.sync.client.parser;

import com.atlassian.adf.jackson2.AdfJackson2;
import com.atlassian.adf.markdown.MarkdownParser;
import com.atlassian.adf.model.node.Doc;
import io.telicent.jira.sync.client.generator.DocGenerator;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDocParser {

    private static final MarkdownParser PARSER = new MarkdownParser();
    private static final AdfJackson2 JACKSON = new AdfJackson2();
    private static final DocGenerator DOC_GENERATOR = new DocGenerator();
    private static final DocParser DOC_PARSER = new DocParser();

    public static Doc fromMarkdown(String markdown) {
        return PARSER.unmarshall(markdown);
    }

    public static String toJSON(Doc doc) {
        return JACKSON.marshall(doc);
    }

    private void verifyRoundTrip(Doc doc, JSONObject generated) throws JSONException {
        Doc parsed = DOC_PARSER.parse(generated);
        Assert.assertEquals(parsed, doc);
    }

    @Test
    public void givenSimpleDocument_whenGeneratingJSON_thenRoundTrips() throws JSONException {
        // Given
        String markdown = """
                # A Title
                
                Here is some content with an **important** [link](https://example.org)
                """;
        Doc doc = fromMarkdown(markdown);

        // When
        JSONObject json = DOC_GENERATOR.generate(doc);

        // Then
        verifyRoundTrip(doc, json);
    }

    @Test
    public void givenComplexDocument_whenGeneratingJSON_thenRoundTrips() throws JSONException {
        // Given
        String markdown = """
                # A Title
                
                Here is some content with an **important** [link](https://example.org)
                
                ## Subtitle
                
                Here is a list:  
                
                - A
                - B
                    - C
                    - D
                         - E
                    - F
                - G
                
                ---
                
                ### A further subtitle
                
                Here's an *ordered* list:
                
                1. One
                2. Two
              
                """;
        Doc doc = fromMarkdown(markdown);

        // When
        JSONObject json = DOC_GENERATOR.generate(doc);

        // Then
        verifyRoundTrip(doc, json);
    }
}
