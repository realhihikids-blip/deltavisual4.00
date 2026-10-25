package dev.deltavisuals.features;

import dev.deltavisuals.core.Category;
import dev.deltavisuals.core.Module;
import dev.deltavisuals.core.Setting;
import dev.deltavisuals.util.ColorUtil;
import dev.deltavisuals.util.Compat;
import dev.deltavisuals.util.Fx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleTypes;

/** Purely cosmetic particle aura around your own character. No targeting, no combat interaction. */
public final class Aura extends Module {
    private static final double TAU = Math.PI * 2;
    private static final String[] TYPES = {
        "Foot Ring", "Soft Glow", "Stars", "Energy Rings", "Pulse", "Spiral", "Orbit Dots", "Rainbow Ring", "Double Helix"};

    private final Setting.Mode type;
    private final Setting.Color c1, c2;
    private final Setting.Bool twoColor, firstPerson;
    private final Setting.Num speed, size, density, radius, opacity;
    private long ticks;
    private long previewUntil;

    public Aura() {
        super("Aura", "Cosmetic particle aura around your character", Category.COSMETICS, false);
        tickWhenDisabled = true; // needed for the preview button
        type = add(new Setting.Mode("Effect", TYPES, 0));
        c1 = add(new Setting.Color("Main Color", 0xFF8B5CF6, false));
        c2 = add(new Setting.Color("Second Color", 0xFF38BDF8, false));
        twoColor = add(new Setting.Bool("Two-Color Blend", true));
        speed = add(new Setting.Num("Speed", 0.2, 3, 0.1, 1, "x"));
        size = add(new Setting.Num("Particle Size", 0.3, 2.0, 0.1, 0.8));
        density = add(new Setting.Num("Density", 1, 12, 1, 4));
        radius = add(new Setting.Num("Radius", 0.4, 3, 0.1, 1.1, " blocks"));
        opacity = add(new Setting.Num("Opacity", 10, 100, 5, 85, "%"));
        firstPerson = add(new Setting.Bool("Show In First Person", true));
        add(new Setting.Info("Note", () -> "Stars use vanilla white sparks (no tint)"));
        add(new Setting.Action("Preview (4 s)", () -> previewUntil = System.currentTimeMillis() + 4000));
    }

    private int color(double u) {
        if (!twoColor.get()) return c1.rgb();
        double w = 0.5 + 0.5 * Math.sin(u * TAU);
        return ColorUtil.lerp(c1.get(), c2.get(), (float) w) & 0xFFFFFF;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        boolean preview = System.currentTimeMillis() < previewUntil;
        if (!isEnabled() && !preview) return;
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null || mc.isPaused()) return;
        if (!preview && !firstPerson.get() && mc.options.getPerspective().isFirstPerson()) return;

        ticks++;
        double t = ticks * 0.05 * speed.get();
        int n = density.intValue();
        float sz = size.floatValue();
        float ch = opacity.floatValue() / 100f;
        double r = radius.get();
        double x = p.getX(), y = p.getY(), z = p.getZ();
        double h = Math.max(1.0, p.getHeight());

        switch (type.get()) {
            case "Foot Ring" -> {
                for (int i = 0; i < n; i++) {
                    double a = t * 2 + i * TAU / n;
                    Fx.dust(mc, color(a / TAU), sz, ch, x + Math.cos(a) * r, y + 0.05, z + Math.sin(a) * r);
                }
            }
            case "Soft Glow" -> {
                for (int i = 0; i < n; i++) {
                    double a = Fx.rnd() * TAU, v = Fx.rnd() * 2 - 1, rr = r * (0.75 + 0.25 * Fx.rnd());
                    double rad = Math.sqrt(1 - v * v) * rr;
                    Fx.dust(mc, color(Fx.rnd()), sz * 1.6f, ch * 0.6f, x + Math.cos(a) * rad, y + h * 0.5 + v * h * 0.6, z + Math.sin(a) * rad);
                }
            }
            case "Stars" -> {
                for (int i = 0; i < Math.max(1, n / 2); i++) {
                    if (Fx.rnd() > ch) continue;
                    double a = Fx.rnd() * TAU, rr = r * Math.sqrt(Fx.rnd());
                    Compat.spawn(mc, ParticleTypes.END_ROD, x + Math.cos(a) * rr, y + Fx.rnd() * h, z + Math.sin(a) * rr, 0, 0.02, 0);
                }
            }
            case "Energy Rings" -> {
                double hy = ((t * 0.6) % 1.0) * h;
                int pts = n * 3;
                for (int i = 0; i < pts; i++) {
                    double a = i * TAU / pts;
                    Fx.dust(mc, color(hy / h), sz, ch, x + Math.cos(a) * r * 0.8, y + hy, z + Math.sin(a) * r * 0.8);
                }
            }
            case "Pulse" -> {
                double rr = r * (0.35 + 0.65 * (0.5 + 0.5 * Math.sin(t * 3)));
                int pts = n * 3;
                for (int i = 0; i < pts; i++) {
                    double a = i * TAU / pts;
                    Fx.dust(mc, color(a / TAU), sz, ch, x + Math.cos(a) * rr, y + h * 0.5, z + Math.sin(a) * rr);
                }
            }
            case "Spiral" -> {
                for (int i = 0; i < n; i++) {
                    double f = (t * 0.5 + i / (double) n) % 1.0;
                    double a = f * TAU * 3 + t;
                    Fx.dust(mc, color(f), sz, ch, x + Math.cos(a) * r * 0.8, y + f * h, z + Math.sin(a) * r * 0.8);
                }
            }
            case "Orbit Dots" -> {
                for (int k = 0; k < 4; k++) {
                    double a = t * (1.2 + k * 0.35) + k * 1.7;
                    Fx.dust(mc, color(k / 4.0), sz * 1.2f, ch, x + Math.cos(a) * r, y + h * (0.2 + 0.25 * k), z + Math.sin(a) * r);
                }
            }
            case "Rainbow Ring" -> {
                for (int i = 0; i < n * 2; i++) {
                    double a = t * 2 + i * TAU / (n * 2);
                    int rgb = ColorUtil.hsv((float) (a / TAU + t * 0.2), 0.85f, 1f, 255) & 0xFFFFFF;
                    Fx.dust(mc, rgb, sz, ch, x + Math.cos(a) * r, y + 0.05, z + Math.sin(a) * r);
                }
            }
            case "Double Helix" -> {
                for (int i = 0; i < n; i++) {
                    double f = (t * 0.5 + i / (double) n) % 1.0;
                    double a = f * TAU * 2.5 + t;
                    Fx.dust(mc, c1.rgb(), sz, ch, x + Math.cos(a) * r * 0.8, y + f * h, z + Math.sin(a) * r * 0.8);
                    Fx.dust(mc, c2.rgb(), sz, ch, x + Math.cos(a + Math.PI) * r * 0.8, y + f * h, z + Math.sin(a + Math.PI) * r * 0.8);
                }
            }
            default -> { }
        }
    }
}
