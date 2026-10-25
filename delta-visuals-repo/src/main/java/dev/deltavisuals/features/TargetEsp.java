package dev.deltavisuals.features;

import dev.deltavisuals.core.Category;
import dev.deltavisuals.core.Module;
import dev.deltavisuals.core.Setting;
import dev.deltavisuals.util.Chroma;
import dev.deltavisuals.util.Fx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * Cosmetic particle effect around the entity you just hit (or the one under your crosshair).
 * Particles are only drawn while the target is visible like any other particle - it is NOT a see-through ESP.
 */
public final class TargetEsp extends Module {
    private static final double TAU = Math.PI * 2;

    private final Setting.Mode effect, colorMode;
    private final Setting.Color c1, c2;
    private final Setting.Num rainbowSpeed, speed, size, density, padding, opacity, duration;
    private final Setting.Bool holdCrosshair, playersOnly;

    private Entity target;
    private long lastRefresh;
    private long ticks;

    public TargetEsp() {
        super("Target Effects", "Rings / spiral / arrows around the player you hit", Category.VISUALS, false);
        effect = add(new Setting.Mode("Effect", new String[]{"Rings", "Spiral", "Arrows"}, 0));
        colorMode = add(new Setting.Mode("Color Mode", Chroma.MODES, 2));
        c1 = add(new Setting.Color("Main Color", 0xFFFF4DA6, false));
        c2 = add(new Setting.Color("Second Color", 0xFF38BDF8, false));
        c1.visibleIf(() -> !colorMode.is("Rainbow"));
        c2.visibleIf(() -> colorMode.is("Gradient"));
        rainbowSpeed = add(new Setting.Num("Color Flow Speed", 0.2, 5, 0.1, 1, "x"));
        rainbowSpeed.visibleIf(() -> !colorMode.is("Static"));
        speed = add(new Setting.Num("Animation Speed", 0.2, 3, 0.1, 1, "x"));
        size = add(new Setting.Num("Particle Size", 0.3, 2.0, 0.1, 0.8));
        density = add(new Setting.Num("Density", 1, 12, 1, 5));
        padding = add(new Setting.Num("Radius Padding", 0.1, 1.5, 0.05, 0.4, " blocks"));
        opacity = add(new Setting.Num("Opacity", 10, 100, 5, 90, "%"));
        duration = add(new Setting.Num("Duration After Hit", 1, 10, 0.5, 3, " s"));
        holdCrosshair = add(new Setting.Bool("Keep While Aiming At Target", true));
        playersOnly = add(new Setting.Bool("Players Only", true));
    }

    /** Called from the Fabric attack callback (client side). */
    public void onAttack(Entity e) {
        if (!isEnabled() || !(e instanceof LivingEntity)) return;
        if (playersOnly.get() && !(e instanceof PlayerEntity)) return;
        target = e;
        lastRefresh = System.currentTimeMillis();
    }

    @Override
    public void onDisable() { target = null; }

    private int color(double phase) {
        return Chroma.pick(colorMode.get(), c1.rgb(), c2.rgb(), rainbowSpeed.get(), phase, ticks);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || mc.isPaused()) return;
        long now = System.currentTimeMillis();

        if (holdCrosshair.get() && mc.crosshairTarget instanceof EntityHitResult ehr
                && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity e = ehr.getEntity();
            if (e instanceof LivingEntity && e != mc.player && (!playersOnly.get() || e instanceof PlayerEntity)) {
                target = e;
                lastRefresh = now;
            }
        }
        if (target == null) return;
        long lifeMs = (long) (duration.get() * 1000);
        if (target.isRemoved() || !target.isAlive() || now - lastRefresh > lifeMs) { target = null; return; }

        ticks++;
        float fade = (float) Math.max(0, Math.min(1, (lifeMs - (now - lastRefresh)) / 400.0));
        float ch = opacity.floatValue() / 100f * fade;
        float sz = size.floatValue();
        int n = density.intValue();
        double t = ticks * 0.05 * speed.get();
        double x = target.getX(), y = target.getY(), z = target.getZ();
        double h = Math.max(0.6, target.getHeight());
        double r = target.getWidth() * 0.5 + padding.get();

        switch (effect.get()) {
            case "Rings" -> {
                int pts = n * 4;
                for (int ring = 0; ring < 2; ring++) {
                    double p = (t * 0.6 + ring * 0.5) % 1.0;
                    double tri = p < 0.5 ? p * 2 : 2 - p * 2;
                    double yy = y + h * tri;
                    for (int i = 0; i < pts; i++) {
                        double a = i * TAU / pts + t;
                        Fx.dust(mc, color(a / TAU + ring * 0.3), sz, ch, x + Math.cos(a) * r, yy, z + Math.sin(a) * r);
                    }
                }
            }
            case "Spiral" -> {
                int tail = n * 4;
                for (int s = 0; s < 3; s++) {
                    for (int k = 0; k < tail; k++) {
                        double a = t * 2.4 + s * TAU / 3 - k * 0.11;
                        double yy = y + h * (0.5 + 0.5 * Math.sin(t * 1.8 - k * 0.1 + s));
                        float f = 1f - k / (float) tail;
                        Fx.dust(mc, color(s * 0.33 + k * 0.01), sz * (0.6f + 0.4f * f), ch * f,
                                x + Math.cos(a) * r, yy, z + Math.sin(a) * r);
                    }
                }
            }
            case "Arrows" -> {
                for (int i = 0; i < 4; i++) {
                    double a = t * 2.0 + i * TAU / 4;
                    double yy = y + h * 0.5 + Math.sin(t * 2 + i) * h * 0.15;
                    double cs = Math.cos(a), sn = Math.sin(a);
                    double px = x + cs * r, pz = z + sn * r;
                    double tx = -sn, tz = cs;          // direction of motion
                    double rx = cs, rz = sn;           // radial
                    int col = color(i / 4.0);
                    int steps = Math.max(2, n / 2 + 1);
                    for (int k = 0; k <= steps; k++) {
                        double u = k / (double) steps;
                        // two wings of a chevron meeting at the tip
                        Fx.dust(mc, col, sz, ch, px + tx * 0.2 - tx * 0.22 * u + rx * 0.16 * u, yy, pz + tz * 0.2 - tz * 0.22 * u + rz * 0.16 * u);
                        Fx.dust(mc, col, sz, ch, px + tx * 0.2 - tx * 0.22 * u - rx * 0.16 * u, yy, pz + tz * 0.2 - tz * 0.22 * u - rz * 0.16 * u);
                    }
                }
            }
            default -> { }
        }
    }
}
