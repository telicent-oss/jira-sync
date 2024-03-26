package io.telicent.jira.sync.cli.commands;

import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.help.CommandGroupUsageGenerator;
import com.github.rvesse.airline.help.CommandUsageGenerator;
import com.github.rvesse.airline.help.GlobalUsageGenerator;
import com.github.rvesse.airline.help.UsageHelper;
import com.github.rvesse.airline.help.cli.CliCommandGroupUsageGenerator;
import com.github.rvesse.airline.help.cli.CliCommandUsageGenerator;
import com.github.rvesse.airline.help.cli.CliGlobalUsageGenerator;
import com.github.rvesse.airline.help.cli.CliGlobalUsageSummaryGenerator;
import com.github.rvesse.airline.model.CommandGroupMetadata;
import com.github.rvesse.airline.model.CommandMetadata;
import com.github.rvesse.airline.model.GlobalMetadata;
import com.github.rvesse.airline.utils.predicates.parser.CommandFinder;
import com.github.rvesse.airline.utils.predicates.parser.GroupFinder;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Command(name = "help", description = "Shows help about this CLI")
public class Help extends SyncCommand {

    @AirlineModule
    private GlobalMetadata<SyncCommand> globalMetadata;

    @Arguments
    private List<String> arguments = new ArrayList<>();

    @Override
    public int run() {
        try {
            // Determine what group and/or command the user supplied as arguments (if any)
            // This is used to show more specific help where possible
            CommandMetadata commandMetadata = null;
            CommandGroupMetadata groupMetadata = null;
            if (!this.arguments.isEmpty()) {
                GroupFinder groupFinder = new GroupFinder(this.arguments.get(0));
                groupMetadata =
                        globalMetadata != null ? IterableUtils.find(globalMetadata.getCommandGroups(), groupFinder) :
                        null;
                if (groupMetadata != null) {
                    if (this.arguments.size() > 1) {
                        CommandFinder commandFinder = new CommandFinder(this.arguments.get(1));
                        commandMetadata = IterableUtils.find(groupMetadata.getCommands(), commandFinder);
                    }
                } else {
                    CommandFinder commandFinder = new CommandFinder(this.arguments.get(0));
                    commandMetadata = globalMetadata != null ?
                                      IterableUtils.find(globalMetadata.getDefaultGroupCommands(), commandFinder) :
                                      null;
                }
            }

            // Show the most specific help possible
            if (commandMetadata != null) {
                CommandUsageGenerator generator = newCommandHelpGenerator();
                generator.usage(globalMetadata != null ? globalMetadata.getName() : null,
                                groupMetadata != null ? toGroupNames(groupMetadata) : null, commandMetadata.getName(),
                                commandMetadata,
                                globalMetadata != null ? globalMetadata.getParserConfiguration() : null);
            } else if (groupMetadata != null) {
                CommandGroupUsageGenerator<SyncCommand> generator = newGroupHelpGenerator();
                generator.usage(this.globalMetadata, toGroups(groupMetadata).toArray(new CommandGroupMetadata[0]));
            } else {
                showGlobalHelp();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 2;
    }

    @NotNull
    private static CliCommandUsageGenerator newCommandHelpGenerator() {
        return new CliCommandUsageGenerator();
    }

    @NotNull
    private static CliCommandGroupUsageGenerator<SyncCommand> newGroupHelpGenerator() {
        return new CliCommandGroupUsageGenerator<>();
    }

    private void showGlobalHelp() throws IOException {
        GlobalUsageGenerator<SyncCommand> generator = newGlobalHelpGenerator();
        generator.usage(this.globalMetadata);
    }

    @NotNull
    private static CliGlobalUsageGenerator<SyncCommand> newGlobalHelpGenerator() {
        return new CliGlobalUsageGenerator<>();
    }

    private static List<CommandGroupMetadata> toGroups(CommandGroupMetadata group) {
        List<CommandGroupMetadata> groupPath = new ArrayList<CommandGroupMetadata>();
        groupPath.add(group);
        while (group.getParent() != null) {
            group = group.getParent();
            groupPath.add(0, group);
        }
        return groupPath;
    }

    private static String[] toGroupNames(CommandGroupMetadata group) {
        return UsageHelper.toGroupNames(toGroups(group));
    }
}
