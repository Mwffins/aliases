package me.moof.aliases.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.moof.aliases.Aliases;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("aliases.json");
    private static AliasConfig config = new AliasConfig();

    public static AliasConfig getConfig() {
        return config;
    }

    public static void loadConfig() {
        File configFile = CONFIG_PATH.toFile();
        if (!configFile.exists()) {
            config = AliasConfig.createDefaultConfig();
            saveConfig();
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            config = GSON.fromJson(reader, AliasConfig.class);
            if (config == null) {
                config = AliasConfig.createDefaultConfig();
            }
        } catch (IOException e) {
            Aliases.LOGGER.error("Failed to load config file aliases.json", e);
            config = AliasConfig.createDefaultConfig();
        }
    }

    public static void saveConfig() {
        File configFile = CONFIG_PATH.toFile();
        try {
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            Aliases.LOGGER.error("Failed to save config file aliases.json", e);
        }
    }

    public static boolean isCircular(String name, String target) {
        String cleanName = name.toLowerCase().startsWith("/") ? name.substring(1) : name.toLowerCase();
        String currentTarget = target.trim().toLowerCase();
        if (currentTarget.startsWith("/")) {
            currentTarget = currentTarget.substring(1);
        }

        String firstWord = currentTarget.split("\\s+")[0];
        if (firstWord.equalsIgnoreCase(cleanName)) {
            return true;
        }

        Set<String> visited = new HashSet<>();
        visited.add(cleanName);

        while (firstWord != null && !firstWord.isBlank()) {
            if (visited.contains(firstWord)) {
                return true;
            }
            visited.add(firstWord);

            String nextAlias = firstWord;
            Optional<AliasDefinition> def = config.getAliases().stream()
                    .filter(a -> a.getName().equalsIgnoreCase(nextAlias))
                    .findFirst();

            if (def.isPresent()) {
                String nextTarget = def.get().getTarget().trim().toLowerCase();
                if (nextTarget.startsWith("/")) {
                    nextTarget = nextTarget.substring(1);
                }
                firstWord = nextTarget.split("\\s+")[0];
            } else {
                break;
            }
        }
        return false;
    }

    public static boolean addAlias(String name, String target, int permissionLevel, String description) {
        String cleanName = name.toLowerCase().startsWith("/") ? name.substring(1) : name;
        Optional<AliasDefinition> existing = config.getAliases().stream()
                .filter(a -> a.getName().equalsIgnoreCase(cleanName))
                .findFirst();

        if (existing.isPresent()) {
            AliasDefinition alias = existing.get();
            alias.setTarget(target);
            alias.setPermissionLevel(permissionLevel);
            if (description != null && !description.isBlank()) {
                alias.setDescription(description);
            }
        } else {
            config.getAliases().add(new AliasDefinition(cleanName, target, permissionLevel, description));
        }
        saveConfig();
        return true;
    }

    public static boolean removeAlias(String name) {
        String cleanName = name.toLowerCase().startsWith("/") ? name.substring(1) : name;
        boolean removed = config.getAliases().removeIf(a -> a.getName().equalsIgnoreCase(cleanName));
        if (removed) {
            saveConfig();
        }
        return removed;
    }
}
