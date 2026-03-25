package pl.enderquickstash.listeners;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.enderquickstash.ConfigManager;
import pl.enderquickstash.EnderQuickStash;
import pl.enderquickstash.ItemTransferUtil;
import pl.enderquickstash.ItemTransferUtil.TransferResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listens for left-clicks on Ender Chest blocks and triggers the quick-stash
 * logic when appropriate.
 */
public class EnderChestClickListener implements Listener {

    private final EnderQuickStash plugin;

    /**
     * Per-player last-action timestamp for cooldown enforcement.
     * Key: Player UUID, Value: System.currentTimeMillis() of last stash action.
     */
    private final Map<UUID, Long> cooldownMap = new HashMap<>();

    public EnderChestClickListener(EnderQuickStash plugin) {
        this.plugin = plugin;
    }

    /**
     * Intercepts left-click physical interaction with an Ender Chest.
     *
     * <p>Priority is set to HIGH so we run after most protection plugins
     * (e.g. WorldGuard) but before MONITOR-priority listeners that may
     * track inventory changes.</p>
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        // We care only about LEFT_CLICK_BLOCK with the main hand
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.ENDER_CHEST) return;

        Player player = event.getPlayer();

        // Permission check
        ConfigManager cfg = plugin.getConfigManager();
        if (!player.hasPermission("enderquickstash.use")) {
            player.sendMessage(cfg.format(cfg.getMsgNoPermission(), null));
            // Cancel so vanilla "attack" animation still doesn't break blocks
            event.setCancelled(true);
            return;
        }

        // Cancel the event so the Ender Chest doesn't open / take damage
        event.setCancelled(true);

        // ── Cooldown check ────────────────────────────────────────────────
        long now = System.currentTimeMillis();
        long cooldown = cfg.getCooldownMs();
        if (cooldown > 0) {
            Long last = cooldownMap.get(player.getUniqueId());
            if (last != null && (now - last) < cooldown) {
                player.sendMessage(cfg.format(cfg.getMsgCooldown(), null));
                return;
            }
        }

        // ── Retrieve held item ────────────────────────────────────────────
        ItemStack held = player.getInventory().getItemInMainHand();

        if (held.getType().isAir()) {
            player.sendMessage(cfg.format(cfg.getMsgHandEmpty(), null));
            return;
        }

        // ── Get the player's Ender Chest inventory ────────────────────────
        Inventory enderChest = player.getEnderChest();

        // ── Attempt transfer ──────────────────────────────────────────────
        TransferResult result = ItemTransferUtil.transfer(
                held,
                enderChest,
                cfg.isTransferWholeStack()
        );

        switch (result) {
            case SUCCESS -> {
                // Record cooldown timestamp
                cooldownMap.put(player.getUniqueId(), now);

                // Format a nice item name
                String itemName = formatItemName(held);
                player.sendMessage(cfg.format(cfg.getMsgItemStashed(), itemName));

                if (cfg.isPlaySoundOnSuccess()) {
                    player.playSound(player.getLocation(),
                            Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.2f);
                }
            }

            case INVENTORY_FULL -> {
                player.sendMessage(cfg.format(cfg.getMsgEnderChestFull(), null));

                if (cfg.isPlaySoundOnFail()) {
                    player.playSound(player.getLocation(),
                            Sound.BLOCK_CHEST_LOCKED, 0.8f, 1.0f);
                }
            }

            case HAND_EMPTY ->
                    // Shouldn't normally reach here (we checked above), but handle defensively
                    player.sendMessage(cfg.format(cfg.getMsgHandEmpty(), null));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns the display name of an ItemStack (custom name if present,
     * otherwise a human-readable version of the material name).
     */
    private String formatItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        // Convert e.g. DIAMOND_SWORD → "Diamond Sword"
        String raw = item.getType().name().replace('_', ' ').toLowerCase();
        String[] words = raw.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1))
                  .append(' ');
            }
        }
        return sb.toString().trim();
    }
}
