package me.moof.aliases.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.moof.aliases.config.AliasConfig;
import me.moof.aliases.config.AliasDefinition;
import me.moof.aliases.config.ConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AliasCommandRegistry {
    private static final Map<CommandSourceStack, Set<String>> ACTIVE_CHAINS = Collections.synchronizedMap(new WeakHashMap<>());

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

        final String cleanName = name;

        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(cleanName)
                .requires(source -> hasPermission(source, alias.getPermissionLevel()))
                .executes(context -> executeAlias(context.getSource(), cleanName, alias.getTarget(), ""));

        node.then(Commands.argument("args", StringArgumentType.greedyString())
                .executes(context -> executeAlias(
                        context.getSource(),
                        cleanName,
                        alias.getTarget(),
                        StringArgumentType.getString(context, "args")
                )));

        dispatcher.register(node);
    }

    public static boolean hasPermission(CommandSourceStack source, int level) {
        if (level <= 0) {
            return true;
        }
        PermissionLevel permLevel = PermissionLevel.byId(level);
        return source.permissions().hasPermission(new Permission.HasCommandLevel(permLevel));
    }

    private static int executeAlias(CommandSourceStack source, String aliasName, String targetTemplate, String rawArgs) {
        Set<String> active = ACTIVE_CHAINS.computeIfAbsent(source, s -> new HashSet<>());
        if (active.contains(aliasName)) {
            source.sendFailure(Component.literal("Recursive command loop detected for alias: /" + aliasName)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (active.size() >= 10) {
            source.sendFailure(Component.literal("Command alias recursion depth limit reached (max 10).")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        String finalCommand = parseCommand(targetTemplate, rawArgs);
        if (finalCommand.startsWith("/")) {
            finalCommand = finalCommand.substring(1);
        }

        if (finalCommand.isBlank()) {
            return 0;
        }

        active.add(aliasName);
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

        Pattern posFallbackPattern = Pattern.compile("\\$\\{(\\d+)\\|([^}]+)\\}");
        Matcher posMatcher = posFallbackPattern.matcher(result);
        StringBuilder sb = new StringBuilder();
        while (posMatcher.find()) {
            int index = Integer.parseInt(posMatcher.group(1));
            String fallback = posMatcher.group(2);
            String replacement = (index < argsArray.length) ? argsArray[index] : fallback;
            posMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        posMatcher.appendTail(sb);
        result = sb.toString();

        for (int i = 0; i < argsArray.length; i++) {
            result = result.replace("$" + i, argsArray[i]);
            result = result.replace("{" + i + "}", argsArray[i]);
        }

        Pattern argsFallbackPattern = Pattern.compile("(\\$\\{args\\|([^}]+)\\}|\\{args\\|([^}]+)\\})");
        Matcher argsMatcher = argsFallbackPattern.matcher(result);
        sb = new StringBuilder();
        while (argsMatcher.find()) {
            String fallback = argsMatcher.group(2) != null ? argsMatcher.group(2) : argsMatcher.group(3);
            String replacement = trimmedArgs.isEmpty() ? fallback : trimmedArgs;
            argsMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        argsMatcher.appendTail(sb);
        result = sb.toString();

        String defaultArgsValue = trimmedArgs.isEmpty() ? "@s" : trimmedArgs;
        if (result.contains("$args")) {
            result = result.replace("$args", defaultArgsValue);
        }
        if (result.contains("{args}")) {
            result = result.replace("{args}", defaultArgsValue);
        }

        boolean hadPlaceholders = targetTemplate.contains("$") || targetTemplate.contains("{");
        if (!hadPlaceholders && !trimmedArgs.isEmpty()) {
            result = result + " " + trimmedArgs;
        }

        return result.replaceAll("\\s+", " ").trim();
    }
}
