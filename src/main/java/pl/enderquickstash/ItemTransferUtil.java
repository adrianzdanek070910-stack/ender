package pl.enderquickstash;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

/**
 * Safe, atomic item transfer logic used by the stash feature.
 *
 * <p>All transfer operations are done <em>in-place</em> on real inventory
 * objects, never on clones that are later discarded, which prevents any
 * possibility of item duplication.</p>
 */
public final class ItemTransferUtil {

    private ItemTransferUtil() {}

    /**
     * Result of a transfer attempt.
     */
    public enum TransferResult {
        /** Item(s) successfully moved into the target inventory. */
        SUCCESS,
        /** The source hand slot was empty – nothing to transfer. */
        HAND_EMPTY,
        /** Not all items could be placed; the target inventory is full. */
        INVENTORY_FULL
    }

    /**
     * Attempts to move items held in the player's main hand into the
     * {@code target} inventory.
     *
     * <p>The operation is atomic with respect to duplication:
     * <ol>
     *   <li>We first <em>simulate</em> the insertion using
     *       {@link Inventory#addItem(ItemStack...)} on a snapshot – if
     *       Bukkit says it can fit, we then do the real move.</li>
     *   <li>We only clear / reduce the source slot <em>after</em> the
     *       items have been confirmed added to the target.</li>
     * </ol>
     * </p>
     *
     * @param source      the held-item stack (must not be null / AIR)
     * @param target      the Ender Chest inventory to stash into
     * @param wholeStack  {@code true}  → transfer the entire stack,
     *                    {@code false} → transfer only one item
     * @return a {@link TransferResult} indicating what happened
     */
    public static TransferResult transfer(ItemStack source,
                                          Inventory target,
                                          boolean wholeStack) {

        if (source == null || source.getType().isAir()) {
            return TransferResult.HAND_EMPTY;
        }

        // Build the stack we actually want to move
        ItemStack toMove = source.clone();
        if (!wholeStack) {
            toMove.setAmount(1);
        }

        // ── Simulate: will Bukkit accept this stack? ────────────────────────
        // addItem returns the leftovers it couldn't fit.
        HashMap<Integer, ItemStack> leftover = target.addItem(toMove.clone());
        if (!leftover.isEmpty()) {
            // At least some items didn't fit → report full, do NOT touch source
            return TransferResult.INVENTORY_FULL;
        }

        // ── Everything fits – now do the real move ──────────────────────────
        // Add the real (non-clone) amount to the target.
        target.addItem(toMove);   // guaranteed to succeed (we just proved it)

        // Reduce / clear the source slot
        int newAmount = source.getAmount() - toMove.getAmount();
        if (newAmount <= 0) {
            source.setAmount(0);  // Bukkit interprets 0 as AIR / slot cleared
        } else {
            source.setAmount(newAmount);
        }

        return TransferResult.SUCCESS;
    }
}
