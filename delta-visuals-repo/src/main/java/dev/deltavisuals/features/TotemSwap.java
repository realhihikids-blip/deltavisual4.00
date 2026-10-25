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
 * One key press = one vanilla inventory action (the same "swap with off-hand" click you do by hand).
 * Manual only: no danger detection, no automatic triggering, no packet tricks.
 */
public final class TotemSwap extends Module {
    private final Setting.Mode outside;
    private final Setting.Num cooldown;
    private final Setting.Bool message;
    private long last;

    public TotemSwap() {
        super("Totem Swap", "Key: move a totem into your off-hand (inventory open) or select it in the hotbar", Category.PLAYER, true);
        action = true;
        allowInInventory = true;
        outside = add(new Setting.Mode("Inventory Closed", new String[]{"Select Hotbar Totem", "Do Nothing"}, 0));
        cooldown = add(new Setting.Num("Anti-Spam Delay", 150, 1500, 50, 300, " ms"));
        message = add(new Setting.Bool("Show Messages", true));
    }

    @Override
    public void onBind(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.interactionManager == null) return;
        long now = System.currentTimeMillis();
        if (now - last < cooldown.get()) return;
        last = now;

        if (p.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
            if (message.get()) Inv.msg(mc, "Totem is already in your off-hand");
            return;
        }
        if (mc.currentScreen instanceof InventoryScreen) {
            PlayerScreenHandler h = p.playerScreenHandler;
            int slot = Inv.handlerSlot(h, Items.TOTEM_OF_UNDYING);
            if (slot < 0) { if (message.get()) Inv.msg(mc, "No totem in your inventory"); return; }
            // button 40 + SWAP = the vanilla "swap hovered slot with off-hand" action
            mc.interactionManager.clickSlot(h.syncId, slot, 40, SlotActionType.SWAP, p);
            if (message.get()) Inv.msg(mc, "Totem moved to off-hand");
            return;
        }
        if (outside.is("Do Nothing")) {
            if (message.get()) Inv.msg(mc, "Open your inventory to swap a totem");
            return;
        }
        int hb = Inv.hotbarSlot(p, Items.TOTEM_OF_UNDYING);
        if (hb < 0) { if (message.get()) Inv.msg(mc, "No totem in your hotbar"); return; }
        p.getInventory().setSelectedSlot(hb);
        if (message.get()) Inv.msg(mc, "Totem selected - press your swap-hands key to equip it");
    }
}
