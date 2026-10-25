package dev.deltavisuals.features;

import dev.deltavisuals.core.Category;
import dev.deltavisuals.core.Module;
import dev.deltavisuals.core.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;

/** Selects the hotbar slot that already holds an ender pearl. Never uses, moves or creates items. */
public final class PearlSwap extends Module {
    private final Setting.Bool toggleBack, message;
    private int previous = -1;

    public PearlSwap() {
        super("Pearl Swap", "Key: select the hotbar slot with an ender pearl (does not throw it)", Category.PLAYER, true);
        action = true;
        toggleBack = add(new Setting.Bool("Press Again To Go Back", true));
        message = add(new Setting.Bool("Show Messages", true));
    }

    @Override
    public void onBind(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null) return;
        int slot = Inv.hotbarSlot(p, Items.ENDER_PEARL);
        int cur = p.getInventory().getSelectedSlot();
        if (slot < 0) {
            if (message.get()) Inv.msg(mc, "No ender pearl in your hotbar");
            return;
        }
        if (slot == cur) {
            if (toggleBack.get() && previous >= 0 && previous != cur) {
                p.getInventory().setSelectedSlot(previous);
                if (message.get()) Inv.msg(mc, "Back to slot " + (previous + 1));
                previous = -1;
            } else if (message.get()) Inv.msg(mc, "Ender pearl already selected");
            return;
        }
        previous = cur;
        p.getInventory().setSelectedSlot(slot);
        if (message.get()) Inv.msg(mc, "Ender pearl selected (slot " + (slot + 1) + ")");
    }
}
