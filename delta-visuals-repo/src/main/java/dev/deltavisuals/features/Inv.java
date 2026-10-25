package dev.deltavisuals.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;

/** Small read-only helpers shared by the item shortcut modules. */
final class Inv {
    private Inv() {}

    static void msg(MinecraftClient mc, String s) {
        if (mc.player != null) mc.player.sendMessage(Text.literal("[Delta] " + s), true);
    }

    static int hotbarSlot(ClientPlayerEntity p, Item item) {
        for (int i = 0; i < 9; i++) if (p.getInventory().getStack(i).isOf(item)) return i;
        return -1;
    }

    /** Counts hotbar + main inventory (+ off-hand when requested). */
    static int count(ClientPlayerEntity p, Item item, boolean offhand) {
        int n = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isOf(item)) n += s.getCount();
        }
        if (offhand && p.getOffHandStack().isOf(item)) n += p.getOffHandStack().getCount();
        return n;
    }

    /** Slot id inside PlayerScreenHandler (9..44 = main inventory + hotbar) holding the item, or -1. */
    static int handlerSlot(PlayerScreenHandler h, Item item) {
        for (int i = 9; i <= 44; i++) if (h.getSlot(i).getStack().isOf(item)) return i;
        return -1;
    }

    static boolean isChestplate(ItemStack s) {
        if (s.isEmpty() || s.isOf(Items.ELYTRA)) return false;
        EquippableComponent eq = s.get(DataComponentTypes.EQUIPPABLE);
        return eq != null && eq.slot() == EquipmentSlot.CHEST;
    }

    static int chestplateSlot(PlayerScreenHandler h) {
        for (int i = 9; i <= 44; i++) if (isChestplate(h.getSlot(i).getStack())) return i;
        return -1;
    }
}
