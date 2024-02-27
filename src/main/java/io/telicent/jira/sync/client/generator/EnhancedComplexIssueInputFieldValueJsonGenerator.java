package io.telicent.jira.sync.client.generator;

import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.internal.json.gen.ComplexIssueInputFieldValueJsonGenerator;
import org.codehaus.jettison.json.JSONException;

import java.util.Map;

public class EnhancedComplexIssueInputFieldValueJsonGenerator extends ComplexIssueInputFieldValueJsonGenerator {
    @Override
    public Object generateFieldValueForJson(Object rawValue) throws JSONException {
        if (rawValue instanceof Map<?, ?>) {
            return super.generateFieldValueForJson(new ComplexIssueInputFieldValue((Map<String, Object>) rawValue));
        }
        return super.generateFieldValueForJson(rawValue);
    }
}
