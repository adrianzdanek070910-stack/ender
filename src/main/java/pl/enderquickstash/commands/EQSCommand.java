package pl.enderquickstash.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import pl.enderquickstash.ConfigManager;
import pl.enderquickstash.EnderQuickStash;

import java.util.Collections;
import java.util.List;

/**
 * Handles the /enderquickstash (alias /eqs) command.
 *
 * <p>Currently supported sub-commands:
 * <ul>
 *   <li>{@code reload} – reloads config.yml without restarting the server</li>
 * </ul>
 * </p>
 */
public class EQSCommand implements CommandExecutor, TabCompleter {

    private final EnderQuickStash plugin;

    public EQSCommand(EnderQuickStash plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {

        ConfigManager cfg = plugin.getConfigManager();

        if (!sender.hasPermission("enderquickstash.admin")) {
            sender.sendMessage(cfg.format(cfg.getMsgNoPermission(), null));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            sender.sendMessage(cfg.format(cfg.getMsgPluginReloaded(), null));
            return true;
        }

        // Show usage
        sender.sendMessage("§5EnderQuickStash §8» §r/eqs reload §7— przeładuj konfigurację");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,
                                      Command command,
                                      String alias,
                                      String[] args) {
        if (args.length == 1 && sender.hasPermission("enderquickstash.admin")) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}
