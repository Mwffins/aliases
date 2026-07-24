package me.moof.aliases.config;

import java.util.ArrayList;
import java.util.List;

public class AliasConfig {
    private boolean enabled = true;
    private List<AliasDefinition> aliases = new ArrayList<>();

    public AliasConfig() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<AliasDefinition> getAliases() {
        return aliases;
    }

    public void setAliases(List<AliasDefinition> aliases) {
        this.aliases = aliases;
    }

    public static AliasConfig createDefaultConfig() {
        AliasConfig config = new AliasConfig();
        List<AliasDefinition> defaultAliases = new ArrayList<>();

        defaultAliases.add(new AliasDefinition("gmc", "gamemode creative", 2, "Switch gamemode to Creative"));
        defaultAliases.add(new AliasDefinition("gms", "gamemode survival", 2, "Switch gamemode to Survival"));
        defaultAliases.add(new AliasDefinition("gma", "gamemode adventure", 2, "Switch gamemode to Adventure"));
        defaultAliases.add(new AliasDefinition("gmsp", "gamemode spectator", 2, "Switch gamemode to Spectator"));
        defaultAliases.add(new AliasDefinition("day", "time set day", 2, "Set time to day"));
        defaultAliases.add(new AliasDefinition("night", "time set night", 2, "Set time to night"));
        defaultAliases.add(new AliasDefinition("sun", "weather clear", 2, "Set weather to clear"));
        defaultAliases.add(new AliasDefinition("rain", "weather rain", 2, "Set weather to rain"));
        defaultAliases.add(new AliasDefinition("ci", "clear", 2, "Clear inventory"));
        defaultAliases.add(new AliasDefinition("feed", "effect give ${args|@s} minecraft:saturation 5 255 true", 2, "Feed player"));
        defaultAliases.add(new AliasDefinition("heal", "effect give ${args|@s} minecraft:instant_health 1 255 true", 2, "Heal player"));

        config.setAliases(defaultAliases);
        return config;
    }
}
