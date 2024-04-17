package io.telicent.jira.sync.client.generator;

import com.atlassian.adf.model.node.Doc;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.internal.json.gen.ComplexIssueInputFieldValueJsonGenerator;
import org.codehaus.jettison.json.JSONException;

import java.util.Map;

public class EnhancedComplexIssueInputFieldValueJsonGenerator extends ComplexIssueInputFieldValueJsonGenerator {
    private DocGenerator docGenerator = new DocGenerator();

    @Override
    public Object generateFieldValueForJson(Object rawValue) throws JSONException {
        if (rawValue instanceof Map<?, ?>) {
            // HACK: JIRA's code doesn't handle raw Map, unless we wrap in a ComplexIssueInputFieldValue, so we just
            //       do that as a workaround
            return super.generateFieldValueForJson(new ComplexIssueInputFieldValue((Map<String, Object>) rawValue));
        } else if (rawValue instanceof Doc doc) {
            return docGenerator.generate(doc);
        }
        return super.generateFieldValueForJson(rawValue);
    }
}
