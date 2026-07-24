package me.moof.aliases.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.moof.aliases.config.AliasDefinition;
import me.moof.aliases.config.ConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class AliasesAdminCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("aliases")
                .requires(source -> AliasCommandRegistry.hasPermission(source, 4))
                .executes(context -> showHelp(context.getSource()))
                .then(Commands.literal("reload")
                        .executes(context -> reloadConfig(context.getSource())))
                .then(Commands.literal("list")
                        .executes(context -> listAliases(context.getSource())))
                .then(Commands.literal("remove")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> removeAlias(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "name")
                                ))))
                .then(Commands.literal("add")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("target", StringArgumentType.greedyString())
                                        .executes(context -> addAlias(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "name"),
                                                StringArgumentType.getString(context, "target")
                                        ))))));
    }

    private static int showHelp(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("=== Command Aliases Help ===")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("/aliases add <name> <target>")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" - Add or update an alias").withStyle(ChatFormatting.WHITE)), false);
        source.sendSuccess(() -> Component.literal("/aliases remove <name>")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" - Remove an existing alias").withStyle(ChatFormatting.WHITE)), false);
        source.sendSuccess(() -> Component.literal("/aliases list")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" - List configured aliases").withStyle(ChatFormatting.WHITE)), false);
        source.sendSuccess(() -> Component.literal("/aliases reload")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" - Reload config from disk").withStyle(ChatFormatting.WHITE)), false);
        return 1;
    }

    private static int reloadConfig(CommandSourceStack source) {
        ConfigManager.loadConfig();
        syncCommandTrees(source.getServer());
        source.sendSuccess(() -> Component.literal("Aliases config reloaded and command trees updated!")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int listAliases(CommandSourceStack source) {
        List<AliasDefinition> list = ConfigManager.getConfig().getAliases();
        if (list.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No aliases currently registered.")
                    .withStyle(ChatFormatting.GRAY), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("=== Registered Aliases (" + list.size() + ") ===")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

        for (AliasDefinition alias : list) {
            source.sendSuccess(() -> Component.literal("/" + alias.getName())
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(" -> /" + alias.getTarget()).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" [Perm: " + alias.getPermissionLevel() + "]").withStyle(ChatFormatting.DARK_GRAY)), false);
        }
        return list.size();
    }

    private static int addAlias(CommandSourceStack source, String name, String target) {
        if (ConfigManager.isCircular(name, target)) {
            source.sendFailure(Component.literal("Cannot add alias /" + name + ": it creates a circular command loop!"));
            return 0;
        }

        boolean success = ConfigManager.addAlias(name, target, 2, "Custom alias");
        if (success) {
            syncCommandTrees(source.getServer());
            source.sendSuccess(() -> Component.literal("Successfully added alias: ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("/" + name).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" -> /" + target).withStyle(ChatFormatting.AQUA)), true);
            return 1;
        }
        source.sendFailure(Component.literal("Failed to add alias /" + name));
        return 0;
    }

    private static int removeAlias(CommandSourceStack source, String name) {
        boolean removed = ConfigManager.removeAlias(name);
        if (removed) {
            syncCommandTrees(source.getServer());
            source.sendSuccess(() -> Component.literal("Successfully removed alias /" + name)
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        source.sendFailure(Component.literal("Alias /" + name + " not found."));
        return 0;
    }

    private static void syncCommandTrees(MinecraftServer server) {
        if (server != null) {
            CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
            AliasCommandRegistry.registerAll(dispatcher);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                server.getCommands().sendCommands(player);
            }
        }
    }
}
