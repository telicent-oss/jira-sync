package io.telicent.jira.sync.cli.commands;

import com.github.rvesse.airline.annotations.AirlineModule;
import io.telicent.jira.sync.cli.options.GitHubOptions;

public abstract class JiraGitHubSyncCommand extends JiraProjectSyncCommand {

    @AirlineModule
    protected final GitHubOptions gitHubOptions = new GitHubOptions();
}
