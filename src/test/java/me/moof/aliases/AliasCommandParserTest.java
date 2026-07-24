package me.moof.aliases;

import me.moof.aliases.command.AliasCommandRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AliasCommandParserTest {

    @Test
    public void testTrailingArgsAppending() {
        String target = "gamemode creative";
        String rawArgs = "Seth";
        String parsed = AliasCommandRegistry.parseCommand(target, rawArgs);
        Assertions.assertEquals("gamemode creative Seth", parsed);
    }

    @Test
    public void testUniversalArgsPlaceholder() {
        String target = "effect give $args minecraft:saturation 5 255 true";
        String rawArgs = "Seth";
        String parsed = AliasCommandRegistry.parseCommand(target, rawArgs);
        Assertions.assertEquals("effect give Seth minecraft:saturation 5 255 true", parsed);
    }

    @Test
    public void testPositionalPlaceholders() {
        String target = "tp {0} {1}";
        String rawArgs = "PlayerA PlayerB";
        String parsed = AliasCommandRegistry.parseCommand(target, rawArgs);
        Assertions.assertEquals("tp PlayerA PlayerB", parsed);
    }

    @Test
    public void testNoArgsProvided() {
        String target = "time set day";
        String rawArgs = "";
        String parsed = AliasCommandRegistry.parseCommand(target, rawArgs);
        Assertions.assertEquals("time set day", parsed);
    }
}
