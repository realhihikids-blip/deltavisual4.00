package dev.deltavisuals.features;

import dev.deltavisuals.core.Category;
import dev.deltavisuals.core.Setting;
import dev.deltavisuals.hud.TextHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.List;
import java.util.Locale;

/**
 * INFORMATION ONLY. Shows the distance from your eyes to the entity currently under your crosshair
 * (the same target vanilla already selected). It changes no reach, hitbox, packet or targeting.
 */
public final class ReachDisplay extends TextHud {
    private final Setting.Num decimals, interval;
    private final Setting.Bool hideNoTarget, label, targetName;
    private String cachedDist = null, cachedName = "";
    private int tick;

    public ReachDisplay() {
        super("Reach Display", "Distance to the target under your crosshair (display only)", Category.VISUALS, false, 0.5, 0.56);
        add(new Setting.Info("Note", () -> "Display only - does not change your reach"));
        decimals = add(new Setting.Num("Decimals", 0, 2, 1, 2));
        interval = add(new Setting.Num("Update Interval", 1, 10, 1, 1, " ticks"));
        label = add(new Setting.Bool("Show Label", true));
        targetName = add(new Setting.Bool("Show Target Name", false));
        hideNoTarget = add(new Setting.Bool("Hide Without Target", true));
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (++tick < interval.intValue()) return;
        tick = 0;
        cachedDist = null;
        if (mc.player == null) return;
        HitResult hr = mc.crosshairTarget;
        if (hr != null && hr.getType() == HitResult.Type.ENTITY && hr instanceof EntityHitResult ehr) {
            double d = mc.player.getEyePos().distanceTo(ehr.getPos());
            cachedDist = String.format(Locale.ROOT, "%." + decimals.intValue() + "f", d);
            cachedName = ehr.getEntity().getName().getString();
        }
    }

    @Override
    protected void collect(MinecraftClient mc, List<Line> out, boolean editing) {
        String d = cachedDist;
        if (d == null) {
            if (hideNoTarget.get() && !editing) return;
            d = editing ? "3.00" : "-";
        }
        out.add(new Line((label.get() ? "Distance: " : "") + d + " m", color.get()));
        if (targetName.get() && cachedDist != null) out.add(new Line(cachedName, color.get()));
    }
}
