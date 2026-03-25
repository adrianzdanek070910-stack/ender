package pl.enderquickstash;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Centralizes access to config.yml values and formatted messages.
 */
public class ConfigManager {

    private final EnderQuickStash plugin;

    // Settings cache
    private boolean transferWholeStack;
    private boolean playSoundOnSuccess;
    private boolean playSoundOnFail;
    private long cooldownMs;

    // Message cache
    private String prefix;
    private String msgItemStashed;
    private String msgEnderChestFull;
    private String msgHandEmpty;
    private String msgNoPermission;
    private String msgPluginReloaded;
    private String msgCooldown;

    public ConfigManager(EnderQuickStash plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Reads all values from the current FileConfiguration into local cache.
     */
    public void reload() {
        FileConfiguration cfg = plugin.getConfig();

        transferWholeStack  = cfg.getBoolean("settings.transfer-whole-stack", true);
        playSoundOnSuccess  = cfg.getBoolean("settings.play-sound-on-success", true);
        playSoundOnFail     = cfg.getBoolean("settings.play-sound-on-fail", true);
        cooldownMs          = cfg.getLong("settings.cooldown-ms", 250L);

        prefix              = color(cfg.getString("messages.prefix",          "&8[&5EnderQuick&dStash&8] &r"));
        msgItemStashed      = color(cfg.getString("messages.item-stashed",    "&aUmieszczono &e{item} &aw Ender Chescie!"));
        msgEnderChestFull   = color(cfg.getString("messages.ender-chest-full","&cBrak miejsca w Ender Chescie!"));
        msgHandEmpty        = color(cfg.getString("messages.hand-empty",      "&eNie trzymasz żadnego przedmiotu."));
        msgNoPermission     = color(cfg.getString("messages.no-permission",   "&cNie masz uprawnień do używania tej funkcji."));
        msgPluginReloaded   = color(cfg.getString("messages.plugin-reloaded", "&aKonfiguracja pluginu została przeładowana."));
        msgCooldown         = color(cfg.getString("messages.cooldown",        "&eZaczekaj chwilę przed kolejnym odłożeniem."));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Returns a prefixed, color-translated message with optional placeholder substitution.
     *
     * @param raw    raw message (already translated from cache)
     * @param item   item name to substitute into {item} placeholder (may be null)
     */
    public String format(String raw, String item) {
        String msg = prefix + raw;
        if (item != null) {
            msg = msg.replace("{item}", item);
        }
        return msg;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public boolean isTransferWholeStack() { return transferWholeStack; }
    public boolean isPlaySoundOnSuccess()  { return playSoundOnSuccess; }
    public boolean isPlaySoundOnFail()     { return playSoundOnFail; }
    public long    getCooldownMs()         { return cooldownMs; }

    public String getMsgItemStashed()    { return msgItemStashed; }
    public String getMsgEnderChestFull() { return msgEnderChestFull; }
    public String getMsgHandEmpty()      { return msgHandEmpty; }
    public String getMsgNoPermission()   { return msgNoPermission; }
    public String getMsgPluginReloaded() { return msgPluginReloaded; }
    public String getMsgCooldown()       { return msgCooldown; }
}
