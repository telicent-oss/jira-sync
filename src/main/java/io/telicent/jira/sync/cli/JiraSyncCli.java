package io.telicent.jira.sync.cli;

import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.annotations.Group;
import com.github.rvesse.airline.annotations.Parser;
import com.github.rvesse.airline.annotations.help.ExitCodes;
import com.github.rvesse.airline.parser.ParseResult;
import com.github.rvesse.airline.parser.errors.ParseException;
import com.github.rvesse.airline.parser.errors.handlers.CollectAll;
import io.telicent.jira.sync.cli.commands.Help;
import io.telicent.jira.sync.cli.commands.IssueTypes;
import io.telicent.jira.sync.cli.commands.OneGitHubToJira;
import io.telicent.jira.sync.cli.commands.SyncCommand;

//@formatter:off
@Cli(name = "jira-sync",
     description = "Provides a CLI for synchronising between JIRA and GitHub",
     commands = {
        Help.class,
     },
     defaultCommand = Help.class,
     groups = {
        @Group(
            name = "issues",
            commands = {
                IssueTypes.class,
                OneGitHubToJira.class
            }
        )
     },
     parserConfiguration = @Parser(errorHandler = CollectAll.class))
@ExitCodes(
    codes = { 0, 1, 2, 3 },
    descriptions = {
        "Success",
        "Failure",
        "Help Shown",
        "Command Options Invalid"
    }
)
//@formatter:on
public class JiraSyncCli {

    public static void main(String[] args) {
        com.github.rvesse.airline.Cli<SyncCommand> cli = new com.github.rvesse.airline.Cli<>(JiraSyncCli.class);
        ParseResult<SyncCommand> result = cli.parseWithResult(args);
        if (result.wasSuccessful()) {
            try {
                System.exit(result.getCommand().run());
            } catch (Throwable e) {
                System.err.println("Unexpected Error: ");
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
                System.exit(1);
            }
        } else {
            if (result.getCommand() != null) {
                if (result.getCommand().showHelpIfRequested()) {
                    System.exit(2);
                }

                System.err.println("Command and/or options were invalid:");
                System.err.println();
                int i = 0;
                for (ParseException e : result.getErrors()) {
                    System.err.println((++i) + ": " + e.getMessage());
                }
                System.exit(3);
            }
        }
    }
}
