package io.telicent.jira.sync.cli.commands;

import com.github.rvesse.airline.annotations.AirlineModule;
import io.telicent.jira.sync.cli.options.JiraOptions;


public abstract class JiraSyncCommand extends SyncCommand {

    @AirlineModule
    protected JiraOptions jiraOptions = new JiraOptions();

}
