package dev.deltavisuals.util;

import net.minecraft.client.MinecraftClient;

import java.util.concurrent.ThreadLocalRandom;

/** Cosmetic particle helpers (client-side only). */
public final class Fx {
    private Fx() {}

    public static float rnd() { return ThreadLocalRandom.current().nextFloat(); }

    /** Spawns one coloured dust particle; opacity is emulated by spawn probability (dust has no alpha). */
    public static void dust(MinecraftClient mc, int rgb, float size, float chance, double x, double y, double z) {
        if (chance < 1f && rnd() > chance) return;
        Compat.spawn(mc, Compat.dust(rgb, size), x, y, z, 0, 0, 0);
    }

    public static void ring(MinecraftClient mc, double cx, double cy, double cz, double r, int points,
                            double phase, int rgb, float size, float chance, boolean dashed) {
        for (int i = 0; i < points; i++) {
            if (dashed && (i / 2) % 2 == 1) continue;
            double a = phase + i * (Math.PI * 2 / points);
            dust(mc, rgb, size, chance, cx + Math.cos(a) * r, cy, cz + Math.sin(a) * r);
        }
    }
}
