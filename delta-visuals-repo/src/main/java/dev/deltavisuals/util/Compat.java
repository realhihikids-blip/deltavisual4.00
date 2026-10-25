package dev.deltavisuals.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;

/**
 * ALL version-sensitive Minecraft calls that are not plain Screen/DrawContext usage live here.
 * If a Minecraft/Yarn update (or a mistake of mine) breaks compilation, the fix is almost certainly in this file only.
 */
public final class Compat {
    private Compat() {}

    /** Coloured dust particle (1.21.2+ signature: int RGB colour, float scale). */
    public static ParticleEffect dust(int rgb, float scale) {
        return new DustParticleEffect(rgb & 0xFFFFFF, Math.max(0.2f, Math.min(3f, scale)));
    }

    public static void spawn(MinecraftClient mc, ParticleEffect effect, double x, double y, double z, double vx, double vy, double vz) {
        mc.particleManager.addParticle(effect, x, y, z, vx, vy, vz);
    }

    /** True if a vanilla Minecraft control is currently bound to the same physical key / button. */
    public static boolean conflictsWithVanilla(MinecraftClient mc, boolean mouse, int code) {
        try {
            InputUtil.Type t = mouse ? InputUtil.Type.MOUSE : InputUtil.Type.KEYSYM;
            String key = t.createFromCode(code).getTranslationKey();
            for (KeyBinding kb : mc.options.allKeys) {
                if (key.equals(kb.getBoundKeyTranslationKey())) return true;
            }
        } catch (Throwable ignored) { }
        return false;
    }
}
