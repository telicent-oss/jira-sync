package io.telicent.jira.sync.client.generator;

import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.PropertyInput;
import com.atlassian.jira.rest.client.internal.json.gen.IssueInputJsonGenerator;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class EnhancedIssueInputJsonGenerator extends IssueInputJsonGenerator {

    private EnhancedComplexIssueInputFieldValueJsonGenerator generator =
            new EnhancedComplexIssueInputFieldValueJsonGenerator();

    @Override
    public JSONObject generate(final IssueInput issue) throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        final JSONObject fields = new JSONObject();

        if (issue != null && issue.getFields() != null) {
            for (final FieldInput field : issue.getFields().values()) {
                if (field.getValue() != null) {
                    fields.put(field.getId(), generator.generateFieldValueForJson(field.getValue()));
                }
            }
        }

        jsonObject.put("fields", fields);

        // Add entity properties
        final JSONArray entityProperties = new JSONArray();
        if (issue != null && issue.getProperties() != null) {
            for (final PropertyInput p : issue.getProperties()) {
                final JSONObject property = new JSONObject();
                property.put("key", p.getKey());
                property.put("value", new JSONObject(p.getValue()));
                entityProperties.put(property);
            }
        }
        jsonObject.put("properties", entityProperties);

        return jsonObject;
    }
}
