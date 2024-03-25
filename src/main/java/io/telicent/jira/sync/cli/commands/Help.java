package io.telicent.jira.sync.cli.commands;

import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.help.UsageHelper;
import com.github.rvesse.airline.help.cli.CliCommandGroupUsageGenerator;
import com.github.rvesse.airline.help.cli.CliCommandUsageGenerator;
import com.github.rvesse.airline.help.cli.CliGlobalUsageSummaryGenerator;
import com.github.rvesse.airline.model.CommandGroupMetadata;
import com.github.rvesse.airline.model.CommandMetadata;
import com.github.rvesse.airline.model.GlobalMetadata;
import com.github.rvesse.airline.utils.predicates.parser.CommandFinder;
import com.github.rvesse.airline.utils.predicates.parser.GroupFinder;
import org.apache.commons.collections4.IterableUtils;

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

            if (commandMetadata != null) {
                CliCommandUsageGenerator generator = new CliCommandUsageGenerator();
                generator.usage(globalMetadata != null ? globalMetadata.getName() : null,
                                groupMetadata != null ? toGroupNames(groupMetadata) : null, commandMetadata.getName(),
                                commandMetadata,
                                globalMetadata != null ? globalMetadata.getParserConfiguration() : null);
            } else if (groupMetadata != null) {
                CliCommandGroupUsageGenerator generator = new CliCommandGroupUsageGenerator();
                generator.usage(this.globalMetadata, toGroups(groupMetadata).toArray(new CommandGroupMetadata[0]));
            } else {
                showGlobalHelp();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 2;
    }

    private void showGlobalHelp() throws IOException {
        CliGlobalUsageSummaryGenerator generator = new CliGlobalUsageSummaryGenerator();
        generator.usage(this.globalMetadata);
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
