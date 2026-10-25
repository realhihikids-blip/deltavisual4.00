package dev.deltavisuals.features;

import dev.deltavisuals.core.Category;
import dev.deltavisuals.core.Module;
import dev.deltavisuals.core.Setting;
import dev.deltavisuals.util.Fx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * Decorative ring on the ground. It is a PARTICLE ring (safe, works with other mods) and a purely visual
 * radius marker - it is NOT your attack range and changes nothing about combat or targeting.
 */
public final class RadiusCircle extends Module {
    private final Setting.Num radius, thickness, interval, opacity;
    private final Setting.Color color, color2;
    private final Setting.Mode style;
    private final Setting.Bool self, target;
    private int tick;

    public RadiusCircle() {
        super("Radius Circle", "Decorative ground ring with a radius you choose (visual only)", Category.WORLD, false);
        add(new Setting.Info("Note", () -> "Visual marker only - not your reach"));
        self = add(new Setting.Bool("Around Me", true));
        target = add(new Setting.Bool("Around Crosshair Target", false));
        radius = add(new Setting.Num("Radius", 1, 8, 0.5, 3, " blocks"));
        style = add(new Setting.Mode("Style", new String[]{"Solid", "Dashed", "Double"}, 0));
        color = add(new Setting.Color("Color", 0xFF8B5CF6, false));
        color2 = add(new Setting.Color("Second Color", 0xFF38BDF8, false));
        color2.visibleIf(() -> style.is("Double"));
        thickness = add(new Setting.Num("Line Thickness", 0.4, 2.0, 0.1, 0.8));
        opacity = add(new Setting.Num("Opacity", 10, 100, 5, 90, "%"));
        interval = add(new Setting.Num("Update Interval", 2, 10, 1, 3, " ticks"));
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || mc.isPaused()) return;
        if (++tick < interval.intValue()) return;
        tick = 0;
        if (self.get()) ring(mc, mc.player.getX(), mc.player.getY() + 0.04, mc.player.getZ());
        if (target.get() && mc.crosshairTarget instanceof EntityHitResult ehr && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            ring(mc, ehr.getEntity().getX(), ehr.getEntity().getY() + 0.04, ehr.getEntity().getZ());
        }
    }

    private void ring(MinecraftClient mc, double x, double y, double z) {
        double r = radius.get();
        int pts = (int) Math.max(14, Math.min(48, Math.round(r * 7)));
        float size = thickness.floatValue();
        float chance = opacity.floatValue() / 100f;
        boolean dashed = style.is("Dashed");
        Fx.ring(mc, x, y, z, r, pts, 0, color.rgb(), size, chance, dashed);
        if (style.is("Double")) Fx.ring(mc, x, y, z, r + 0.18, pts, 0.1, color2.rgb(), size, chance, false);
    }
}
