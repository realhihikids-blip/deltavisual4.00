package dev.deltavisuals.features;

import dev.deltavisuals.core.Category;
import dev.deltavisuals.core.Modules;
import dev.deltavisuals.core.Setting;
import dev.deltavisuals.hud.TextHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;

import java.util.List;

/** Readiness indicator for the item shortcut modules. */
public final class ItemStatusHud extends TextHud {
    private static final int OK = 0xFF4ADE80, WARN = 0xFFFBBF24, BAD = 0xFFF87171, OFF = 0xFF9CA3AF;
    private final Setting.Bool pearl, totem, elytra;

    public ItemStatusHud() {
        super("Item Status", "Shows if Pearl / Totem / Elytra shortcuts are ready", Category.HUD, true, 0.005, 0.3);
        pearl = add(new Setting.Bool("Pearl", true));
        totem = add(new Setting.Bool("Totem", true));
        elytra = add(new Setting.Bool("Elytra", true));
    }

    @Override
    protected void collect(MinecraftClient mc, List<Line> out, boolean editing) {
        ClientPlayerEntity p = mc.player;
        if (p == null) return;
        if (pearl.get()) {
            if (!Modules.PEARL.isEnabled()) out.add(new Line("Pearl: off", OFF));
            else {
                int n = Inv.hotbarSlot(p, Items.ENDER_PEARL);
                out.add(n >= 0 ? new Line("Pearl: ready (slot " + (n + 1) + ")", OK) : new Line("Pearl: not in hotbar", BAD));
            }
        }
        if (totem.get()) {
            if (!Modules.TOTEM.isEnabled()) out.add(new Line("Totem: off", OFF));
            else {
                boolean off = p.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
                int n = Inv.count(p, Items.TOTEM_OF_UNDYING, false);
                if (off) out.add(new Line("Totem: off-hand (+" + n + ")", OK));
                else if (n > 0) out.add(new Line("Totem: " + n + " spare, off-hand empty", WARN));
                else out.add(new Line("Totem: none", BAD));
            }
        }
        if (elytra.get()) {
            if (!Modules.ELYTRA.isEnabled()) out.add(new Line("Elytra: off", OFF));
            else {
                PlayerScreenHandler h = p.playerScreenHandler;
                if (h.getSlot(6).getStack().isOf(Items.ELYTRA)) out.add(new Line("Elytra: worn", OK));
                else if (Inv.handlerSlot(h, Items.ELYTRA) >= 0) out.add(new Line("Elytra: ready in inventory", OK));
                else out.add(new Line("Elytra: none", BAD));
            }
        }
    }
}
