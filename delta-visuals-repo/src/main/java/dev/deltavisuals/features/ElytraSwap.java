package dev.deltavisuals.features;

import dev.deltavisuals.core.Category;
import dev.deltavisuals.core.Module;
import dev.deltavisuals.core.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Swaps chest slot between elytra and a chestplate using ordinary inventory clicks
 * (pick up, place, put back) - exactly what you would do manually. Requires the inventory to be open.
 */
public final class ElytraSwap extends Module {
    private static final int CHEST_SLOT = 6;
    private final Setting.Num cooldown;
    private final Setting.Bool message;
    private long last;

    public ElytraSwap() {
        super("Elytra Swap", "Key (inventory open): swap between elytra and chestplate", Category.PLAYER, true);
        action = true;
        allowInInventory = true;
        cooldown = add(new Setting.Num("Anti-Spam Delay", 250, 2000, 50, 500, " ms"));
        message = add(new Setting.Bool("Show Messages", true));
    }

    @Override
    public void onBind(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.interactionManager == null) return;
        if (!(mc.currentScreen instanceof InventoryScreen)) {
            if (message.get()) Inv.msg(mc, "Open your inventory first, then press the key");
            return;
        }
        long now = System.currentTimeMillis();
        if (now - last < cooldown.get()) return;
        last = now;

        PlayerScreenHandler h = p.playerScreenHandler;
        boolean wearingElytra = h.getSlot(CHEST_SLOT).getStack().isOf(Items.ELYTRA);
        int from;
        if (wearingElytra) {
            from = Inv.chestplateSlot(h);
            if (from < 0) { if (message.get()) Inv.msg(mc, "No chestplate in your inventory to swap to"); return; }
        } else {
            from = Inv.handlerSlot(h, Items.ELYTRA);
            if (from < 0) { if (message.get()) Inv.msg(mc, "No elytra in your inventory"); return; }
        }
        var im = mc.interactionManager;
        im.clickSlot(h.syncId, from, 0, SlotActionType.PICKUP, p);
        im.clickSlot(h.syncId, CHEST_SLOT, 0, SlotActionType.PICKUP, p);
        if (!h.getCursorStack().isEmpty()) im.clickSlot(h.syncId, from, 0, SlotActionType.PICKUP, p);
        if (message.get()) Inv.msg(mc, wearingElytra ? "Chestplate equipped" : "Elytra equipped");
    }
}
