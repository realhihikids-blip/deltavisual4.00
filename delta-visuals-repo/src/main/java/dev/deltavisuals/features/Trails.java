package dev.deltavisuals.features;

import dev.deltavisuals.core.Category;
import dev.deltavisuals.core.Module;
import dev.deltavisuals.core.Setting;
import dev.deltavisuals.util.Chroma;
import dev.deltavisuals.util.Fx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.ArrayDeque;

/** Cosmetic particle trail behind your own character (walking, sprinting, jumping, falling). */
public final class Trails extends Module {
    private record Pt(double x, double y, double z, long tick) { }

    private final Setting.Mode colorMode;
    private final Setting.Color c1, c2;
    private final Setting.Num rainbowSpeed, thickness, fadeTime, heightOffset, opacity, interval;
    private final Setting.Bool onlyMoving, firstPerson;

    private final ArrayDeque<Pt> points = new ArrayDeque<>();
    private long ticks;
    private double lastX, lastY, lastZ;
    private boolean hasLast;

    public Trails() {
        super("Trails", "Glowing particle line behind your character", Category.COSMETICS, false);
        colorMode = add(new Setting.Mode("Color Mode", Chroma.MODES, 2));
        c1 = add(new Setting.Color("Main Color", 0xFF55FFFF, false));
        c2 = add(new Setting.Color("Second Color", 0xFFFF4DA6, false));
        c1.visibleIf(() -> !colorMode.is("Rainbow"));
        c2.visibleIf(() -> colorMode.is("Gradient"));
        rainbowSpeed = add(new Setting.Num("Color Flow Speed", 0.2, 5, 0.1, 1, "x"));
        rainbowSpeed.visibleIf(() -> !colorMode.is("Static"));
        thickness = add(new Setting.Num("Thickness", 0.3, 2.0, 0.1, 0.9));
        fadeTime = add(new Setting.Num("Fade Time", 0.3, 4, 0.1, 1.5, " s"));
        heightOffset = add(new Setting.Num("Height Above Feet", 0, 1.8, 0.1, 0.1, " blocks"));
        opacity = add(new Setting.Num("Opacity", 10, 100, 5, 90, "%"));
        interval = add(new Setting.Num("Update Interval", 1, 4, 1, 2, " ticks"));
        onlyMoving = add(new Setting.Bool("Only When Moving", true));
        firstPerson = add(new Setting.Bool("Show In First Person", true));
        add(new Setting.Info("Note", () -> "Made of vanilla dust particles (no real 3D ribbon yet)"));
    }

    @Override
    public void onDisable() { points.clear(); hasLast = false; }

    @Override
    public void onTick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null || mc.isPaused()) return;
        ticks++;

        double x = p.getX(), y = p.getY() + heightOffset.get(), z = p.getZ();
        if (hasLast) {
            double dx = x - lastX, dy = y - lastY, dz = z - lastZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > 0.01 || !onlyMoving.get()) {
                int steps = (int) Math.max(1, Math.min(20, Math.ceil(dist / 0.25)));
                if (dist > 0.01) {
                    for (int i = 1; i <= steps; i++) {
                        double f = i / (double) steps;
                        points.addLast(new Pt(lastX + dx * f, lastY + dy * f, lastZ + dz * f, ticks));
                    }
                }
            }
        }
        lastX = x; lastY = y; lastZ = z; hasLast = true;

        long life = Math.max(1, Math.round(fadeTime.get() * 20));
        while (!points.isEmpty() && ticks - points.peekFirst().tick > life) points.pollFirst();
        while (points.size() > 400) points.pollFirst();

        if (!firstPerson.get() && mc.options.getPerspective().isFirstPerson()) return;
        if (ticks % interval.intValue() != 0) return;

        float base = opacity.floatValue() / 100f;
        float sz = thickness.floatValue();
        int idx = 0;
        for (Pt pt : points) {
            float age = (ticks - pt.tick) / (float) life;
            float chance = base * (float) Math.pow(1f - age, 1.5);
            int rgb = Chroma.pick(colorMode.get(), c1.rgb(), c2.rgb(), rainbowSpeed.get(), idx * 0.015, ticks);
            Fx.dust(mc, rgb, sz * (1f - 0.5f * age), chance, pt.x, pt.y, pt.z);
            idx++;
        }
    }
}
