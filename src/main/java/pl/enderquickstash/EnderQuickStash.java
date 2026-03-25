package pl.enderquickstash;

import org.bukkit.plugin.java.JavaPlugin;
import pl.enderquickstash.commands.EQSCommand;
import pl.enderquickstash.listeners.EnderChestClickListener;

/**
 * EnderQuickStash – main plugin class.
 *
 * Allows players to quickly stash held items into their Ender Chest
 * by left-clicking on a placed Ender Chest block.
 */
public final class EnderQuickStash extends JavaPlugin {

    private static EnderQuickStash instance;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Initialize config manager (wraps config access + messages)
        configManager = new ConfigManager(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(
                new EnderChestClickListener(this), this
        );

        // Register commands
        EQSCommand eqsCommand = new EQSCommand(this);
        getCommand("enderquickstash").setExecutor(eqsCommand);
        getCommand("enderquickstash").setTabCompleter(eqsCommand);

        getLogger().info("EnderQuickStash v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("EnderQuickStash disabled.");
    }

    /**
     * Reloads the plugin configuration.
     */
    public void reloadPlugin() {
        reloadConfig();
        configManager.reload();
    }

    public static EnderQuickStash getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
