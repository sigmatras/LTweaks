package com.ltweaks.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class HealthBar {
    public static final HealthBar INSTANCE = new HealthBar();

    private static final int BAR_W = 80;
    private static final int BAR_H = 5;
    private static final int HEART = 9;
    private static final int X_LEFT = 91;

    private static final int LAYER_BACK = 0;
    private static final int LAYER_FILL = 1;
    private static final int LAYER_GHOST = 2;
    private static final int LAYER_OUTLINE = 3;
    private static final int HEALTH_LAYERS = 4;

    private static final int ARMOR_BACK = 0;
    private static final int ARMOR_FILL = 1;
    private static final int ARMOR_LAYERS = 2;

    private static final int HEALTH_ATLAS_W = BAR_W * HEALTH_LAYERS;
    private static final int HEALTH_ATLAS_H = BAR_H * HealthState.COUNT;
    private static final int ARMOR_ATLAS_W = BAR_W * ARMOR_LAYERS;
    private static final int HEARTS_ATLAS_W = HEART * 2;
    private static final int HEARTS_ATLAS_H = HEART * HealthState.COUNT;

    private static final ResourceLocation TEX_HEALTH = rl("textures/gui/health.png");
    private static final ResourceLocation TEX_ARMOR = rl("textures/gui/armor.png");
    private static final ResourceLocation TEX_HEARTS = rl("textures/gui/hearts.png");

    private static final float GHOST_DURATION = 0.6f;
    private static final float ARMOR_DURATION = 0.4f;
    private static final float BLINK_PERIOD = 1.0f;
    private static final float LOW_THRESHOLD = 0.35f;
    private static final float SHAKE_THRESHOLD = 0.5f;
    private static final float SHAKE_MAX = 1.5f;
    private static final float SHAKE_SPEED = 55f;

    private float ghostPercent = 1f;
    private float ghostFrom = 1f;
    private float ghostTarget = 1f;
    private long ghostStart;

    private float armorAnim;
    private float armorFrom;
    private float armorTarget;
    private long armorStart;

    private float lastHealthPercent = 1f;
    private boolean initialized;

    private HealthBar() {}

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath("ltweaks", path);
    }

    public void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        float healthAlpha = FadeController.INSTANCE.getHealthAlpha();
        RenderSystem.setShaderColor(1f, 1f, 1f, healthAlpha);

        long now = System.nanoTime();

        float max = (float) player.getAttributeValue(Attributes.MAX_HEALTH);
        float health = Math.min(player.getHealth(), max);
        float healthPercent = max <= 0f ? 0f : health / max;
        float armorPercent = Easing.clamp01(player.getArmorValue() / 20f);

        if (!initialized) {
            ghostPercent = healthPercent;
            ghostFrom = healthPercent;
            ghostTarget = healthPercent;
            lastHealthPercent = healthPercent;
            armorAnim = armorPercent;
            armorFrom = armorPercent;
            armorTarget = armorPercent;
            initialized = true;
        }

        if (healthPercent < lastHealthPercent && ghostTarget != healthPercent) {
            ghostFrom = ghostPercent;
            ghostTarget = healthPercent;
            ghostStart = now;
        } else if (healthPercent >= lastHealthPercent) {
            ghostPercent = healthPercent;
            ghostFrom = healthPercent;
            ghostTarget = healthPercent;
        }
        lastHealthPercent = healthPercent;

        float gt = Easing.clamp01((now - ghostStart) / 1_000_000_000f / GHOST_DURATION);
        ghostPercent = ghostFrom + (ghostTarget - ghostFrom) * Easing.easeExpoOut(gt);

        if (gt >= 1f) {
            ghostPercent = healthPercent;
        }

        if (armorTarget != armorPercent) {
            armorFrom = armorAnim;
            armorTarget = armorPercent;
            armorStart = now;
        }
        float at = Easing.clamp01((now - armorStart) / 1_000_000_000f / ARMOR_DURATION);
        armorAnim = armorFrom + (armorTarget - armorFrom) * Easing.easeExpoOut(at);

        int state = HealthState.of(player);
        boolean low = healthPercent <= 0.5f;
        boolean critical = healthPercent < LOW_THRESHOLD;

        int baseX = graphics.guiWidth() / 2 - X_LEFT;
        int baseY = graphics.guiHeight() - 30;

        int x = baseX;
        int y = baseY;
        if (healthPercent < SHAKE_THRESHOLD) {
            float t = (SHAKE_THRESHOLD - healthPercent) / SHAKE_THRESHOLD;
            float amp = SHAKE_MAX * t * t;
            float time = now / 1_000_000_000f;
            x = baseX + Math.round((float) Math.sin(time * SHAKE_SPEED) * amp);
            y = baseY + Math.round((float) Math.cos(time * SHAKE_SPEED * 1.3f) * amp);
        }

        int vState = state * BAR_H;

        drawHealth(graphics, LAYER_BACK, x, y, vState, 1f);

        if (ghostPercent < healthPercent - 0.001f) {
            drawHealth(graphics, LAYER_GHOST, x, y, vState, ghostPercent);
        }

        drawHealth(graphics, LAYER_FILL, x, y, vState, healthPercent);
        drawArmor(graphics, ARMOR_BACK, x, y, 1f);
        drawArmor(graphics, ARMOR_FILL, x, y, armorAnim);

        float blink = critical ? blinkAlpha(now) : 0f;
        if (blink > 0f) {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, blink);
            drawHealth(graphics, LAYER_OUTLINE, x, y, vState, 1f);
            RenderSystem.setShaderColor(1f, 1f, 1f, healthAlpha);
        }

        drawHeart(graphics, x, y, state, low, blink, healthAlpha);
    }

    private void drawHealth(GuiGraphics graphics, int layer, int x, int y, int v, float percent) {
        int w = Math.round(BAR_W * Easing.clamp01(percent));
        if (w <= 0) {
            return;
        }
        graphics.blit(TEX_HEALTH, x, y, layer * BAR_W, v, w, BAR_H, HEALTH_ATLAS_W, HEALTH_ATLAS_H);
    }

    private void drawArmor(GuiGraphics graphics, int layer, int x, int y, float percent) {
        int w = Math.round(BAR_W * Easing.clamp01(percent));
        if (w <= 0) {
            return;
        }
        graphics.blit(TEX_ARMOR, x, y, layer * BAR_W, 0, w, BAR_H, ARMOR_ATLAS_W, BAR_H);
    }

    private void drawHeart(GuiGraphics graphics, int x, int y, int state, boolean low, float blink, float healthAlpha) {
        int hx = x - HEART / 2;
        int hy = y + BAR_H / 2 - HEART / 2;
        int u = low ? HEART : 0;
        int v = state * HEART;

        RenderSystem.setShaderColor(1f, 1f, 1f, healthAlpha);
        graphics.blit(TEX_HEARTS, hx, hy, u, v, HEART, HEART, HEARTS_ATLAS_W, HEARTS_ATLAS_H);

        if (blink > 0f) {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, blink);
            graphics.blit(TEX_HEARTS, hx, hy, u, v, HEART, HEART, HEARTS_ATLAS_W, HEARTS_ATLAS_H);
            RenderSystem.setShaderColor(1f, 1f, 1f, healthAlpha);
        }
    }

    private static float blinkAlpha(long now) {
        float phase = ((now / 1_000_000L) % (long) (BLINK_PERIOD * 1000f)) / (BLINK_PERIOD * 1000f);
        return phase < 0.5f ? 1f : 0f;
    }
}