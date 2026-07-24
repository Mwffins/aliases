package me.moof.aliases;

import me.moof.aliases.command.AliasCommandRegistry;
import me.moof.aliases.command.AliasesAdminCommand;
import me.moof.aliases.config.ConfigManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Aliases implements ModInitializer {
	public static final String MOD_ID = "aliases";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Aliases mod.");

		ConfigManager.loadConfig();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			AliasesAdminCommand.register(dispatcher);
			AliasCommandRegistry.registerAll(dispatcher);
		});
	}
}
