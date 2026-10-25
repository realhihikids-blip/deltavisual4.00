package dev.deltavisuals.core;

import dev.deltavisuals.features.*;
import dev.deltavisuals.hud.HudModule;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Modules {
    private static final Logger LOG = LoggerFactory.getLogger("DeltaVisuals/Modules");
    private static final List<Module> ALL = new ArrayList<>();
    private static final List<HudModule> HUD = new ArrayList<>();

    public static ThemeModule THEME;
    public static GuiModule GUI;
    public static HudEditorModule HUD_EDITOR;
    public static ProfilesModule PROFILES;
    public static InfoHud INFO;
    public static ArmorHud ARMOR;
    public static ItemStatusHud ITEM_STATUS;
    public static Watermark WATERMARK;
    public static ReachDisplay REACH;
    public static RadiusCircle CIRCLE;
    public static PearlSwap PEARL;
    public static TotemSwap TOTEM;
    public static ElytraSwap ELYTRA;
    public static Aura AURA;
    public static TargetEsp TARGET_ESP;
    public static DamageIndicators DAMAGE;
    public static Trails TRAILS;

    private Modules() {}

    private static <T extends Module> T reg(T m) {
        ALL.add(m);
        if (m instanceof HudModule h) HUD.add(h);
        return m;
    }

    public static void init() {
        if (!ALL.isEmpty()) return;
        // order = order inside each category
        INFO = reg(new InfoHud());
        ARMOR = reg(new ArmorHud());
        ITEM_STATUS = reg(new ItemStatusHud());
        WATERMARK = reg(new Watermark());
        REACH = reg(new ReachDisplay());
        TARGET_ESP = reg(new TargetEsp());
        DAMAGE = reg(new DamageIndicators());
        CIRCLE = reg(new RadiusCircle());
        PEARL = reg(new PearlSwap());
        TOTEM = reg(new TotemSwap());
        ELYTRA = reg(new ElytraSwap());
        AURA = reg(new Aura());
        TRAILS = reg(new Trails());
        GUI = reg(new GuiModule());
        HUD_EDITOR = reg(new HudEditorModule());
        THEME = reg(new ThemeModule());
        PROFILES = reg(new ProfilesModule());

        // client-side attack notification (Fabric API callback, no mixin needed)
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient()) {
                TARGET_ESP.onAttack(entity);
                DAMAGE.onAttack(entity);
            }
            return ActionResult.PASS;
        });
    }

    public static List<Module> all() { return Collections.unmodifiableList(ALL); }
    public static List<HudModule> hud() { return Collections.unmodifiableList(HUD); }

    public static List<Module> byCategory(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : ALL) if (m.category == c) out.add(m);
        return out;
    }

    /** Ticks enabled modules; a crashing module is disabled instead of crashing the game. */
    public static void tick(MinecraftClient mc) {
        for (Module m : ALL) {
            if (!(m.isEnabled() || m.tickWhenDisabled)) continue;
            try {
                m.onTick(mc);
            } catch (Throwable t) {
                LOG.error("Module {} crashed in onTick and was disabled", m.name, t);
                m.setEnabled(false);
                m.tickWhenDisabled = false;
            }
        }
    }
}
