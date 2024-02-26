package io.telicent.jira.sync.cli.commands;

import com.github.rvesse.airline.HelpOption;
import com.github.rvesse.airline.annotations.AirlineModule;

public abstract class SyncCommand {
    @AirlineModule
    protected HelpOption<SyncCommand> help = new HelpOption<>();

    /**
     * Runs the command
     *
     * @return Exit Code to return
     */
    public abstract int run();

    /**
     * Shows help if user requested it
     *
     * @return True if help requested and shown, false otherwise
     */
    public boolean showHelpIfRequested() {
        return this.help.showHelpIfRequested();
    }
}
