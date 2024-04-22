package io.telicent.jira.sync.cli.commands.issues;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import io.atlassian.util.concurrent.Promise;
import io.telicent.jira.sync.cli.commands.JiraSyncCommand;

import java.io.IOException;

@Command(name = "jira-fields", description = "Finds the available JIRA Fields")
public class Fields extends JiraSyncCommand {

    @Option(name = "--ignore-jira-builtins", description = "When set ignores JIRA built-in fields causing only custom fields to be listed")
    private boolean ignoreJiraBuiltIns = false;

    @Override
    public int run() {
        System.out.println("Retrieving JIRA Fields...");
        System.out.println();

        try (JiraRestClient jira = this.jiraOptions.connect()) {
            Promise<Iterable<Field>> promise = jira.getMetadataClient().getFields();
            Iterable<Field> fields = promise.claim();
            int fieldsFound = 0;
            for (Field field : fields) {
                if (this.ignoreJiraBuiltIns && field.getFieldType() == FieldType.JIRA) {
                    continue;
                }
                fieldsFound++;
                System.out.println("Field ID: " + field.getId());
                System.out.println("Name: " + field.getName());
                System.out.println("Type: " + field.getFieldType());
                System.out.println();
            }
            if (fieldsFound == 0) {
                System.out.println("No Fields Found!");
            }

            return 0;
        } catch (RestClientException e) {
            this.reportJiraRestError(e);
            return 1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
