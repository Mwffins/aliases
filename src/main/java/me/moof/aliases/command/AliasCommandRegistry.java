package me.moof.aliases.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.moof.aliases.Aliases;
import me.moof.aliases.config.AliasConfig;
import me.moof.aliases.config.AliasDefinition;
import me.moof.aliases.config.ConfigManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class AliasCommandRegistry {

    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {
        AliasConfig config = ConfigManager.getConfig();
        if (!config.isEnabled()) {
            return;
        }

        for (AliasDefinition alias : config.getAliases()) {
            registerAlias(dispatcher, alias);
        }
    }

    private static void registerAlias(CommandDispatcher<CommandSourceStack> dispatcher, AliasDefinition alias) {
        String name = alias.getName().toLowerCase();
        if (name.startsWith("/")) {
            name = name.substring(1);
        }

        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(name)
                .requires(source -> hasPermission(source, alias.getPermissionLevel()))
                .executes(context -> executeAlias(context.getSource(), alias.getTarget(), ""));

        node.then(Commands.argument("args", StringArgumentType.greedyString())
                .executes(context -> executeAlias(
                        context.getSource(),
                        alias.getTarget(),
                        StringArgumentType.getString(context, "args")
                )));

        dispatcher.register(node);
        Aliases.LOGGER.info("Registered alias command: /{}", name);
    }

    public static boolean hasPermission(CommandSourceStack source, int level) {
        if (level <= 0) {
            return true;
        }
        PermissionLevel permLevel = PermissionLevel.byId(level);
        return source.permissions().hasPermission(new Permission.HasCommandLevel(permLevel));
    }

    private static int executeAlias(CommandSourceStack source, String targetTemplate, String rawArgs) {
        String finalCommand = parseCommand(targetTemplate, rawArgs);
        if (finalCommand.startsWith("/")) {
            finalCommand = finalCommand.substring(1);
        }

        if (finalCommand.isBlank()) {
            return 0;
        }

        source.getServer().getCommands().performPrefixedCommand(source, finalCommand);
        return 1;
    }

    public static String parseCommand(String targetTemplate, String rawArgs) {
        if (targetTemplate == null || targetTemplate.isBlank()) {
            return "";
        }

        String result = targetTemplate;
        String trimmedArgs = rawArgs == null ? "" : rawArgs.trim();
        String[] argsArray = trimmedArgs.isEmpty() ? new String[0] : trimmedArgs.split("\\s+");

        for (int i = 0; i < argsArray.length; i++) {
            result = result.replace("$" + i, argsArray[i]);
            result = result.replace("{" + i + "}", argsArray[i]);
        }

        if (result.contains("$args")) {
            result = result.replace("$args", trimmedArgs);
        }
        if (result.contains("{args}")) {
            result = result.replace("{args}", trimmedArgs);
        }

        boolean hadPlaceholders = targetTemplate.contains("$") || targetTemplate.contains("{");
        if (!hadPlaceholders && !trimmedArgs.isEmpty()) {
            result = result + " " + trimmedArgs;
        }

        return result.replaceAll("\\s+", " ").trim();
    }
}
