package com.ltweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class FadeController {

    public static final FadeController INSTANCE = new FadeController();

    private static final float HEALTH_FADE_DURATION = 1.0f;
    private static final float HOTBAR_FADE_DURATION = 1.0f;
    private static final long IDLE_THRESHOLD = 3_000_000_000L;
    private static final float HEALTH_TARGET_ALPHA = 0.4f;
    private static final float HOTBAR_TARGET_ALPHA = 0.2f;

    private long lastSlotChange = System.nanoTime();
    private long lastHealthArmorChange = System.nanoTime();
    private int lastSelectedSlot = -1;
    private float lastHealth = Float.NaN;
    private int lastArmor = -1;

    private boolean healthFading;
    private long healthFadeStart;
    private float healthFadeFrom;
    private float healthFadeCurrent;

    private boolean hotbarFading;
    private long hotbarFadeStart;
    private float hotbarFadeFrom;
    private float hotbarFadeCurrent = 1f;

    private FadeController() {}

    public void update() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        long now = System.nanoTime();

        // Проверка смены слота
        int currentSlot = player.getInventory().selected;
        if (currentSlot != lastSelectedSlot) {
            lastSelectedSlot = currentSlot;
            lastSlotChange = now;
        }

        // Проверка изменения HP или брони
        float currentHealth = player.getHealth();
        int currentArmor = player.getArmorValue();
        if (Float.isNaN(lastHealth) || currentHealth != lastHealth || currentArmor != lastArmor) {
            lastHealth = currentHealth;
            lastArmor = currentArmor;
            lastHealthArmorChange = now;
        }

        // HOTBAR FADE (как в HotbarAnimator)
        long hotbarIdle = now - lastSlotChange;
        boolean hotbarShouldFade = hotbarIdle >= IDLE_THRESHOLD;

        if (hotbarShouldFade != hotbarFading) {
            hotbarFading = hotbarShouldFade;
            hotbarFadeFrom = hotbarFadeCurrent;
            hotbarFadeStart = now;
        }

        float hotbarDur = hotbarFading ? HOTBAR_FADE_DURATION : HOTBAR_FADE_DURATION;
        float hotbarT = Easing.clamp01((now - hotbarFadeStart) / 1_000_000_000f / hotbarDur);
        float hotbarEased = Easing.easeExpoOut(hotbarT);
        hotbarFadeCurrent = hotbarFadeFrom + ((hotbarFading ? HOTBAR_TARGET_ALPHA : 1f) - hotbarFadeFrom) * hotbarEased;

        // HEALTH FADE (точно такая же логика)
        long healthIdle = now - lastHealthArmorChange;
        boolean healthShouldFade = healthIdle >= IDLE_THRESHOLD;

        if (healthShouldFade != healthFading) {
            healthFading = healthShouldFade;
            healthFadeFrom = healthFadeCurrent;
            healthFadeStart = now;
        }

        float healthDur = healthShouldFade ? HEALTH_FADE_DURATION : HEALTH_FADE_DURATION;
        float healthT = Easing.clamp01((now - healthFadeStart) / 1_000_000_000f / healthDur);
        float healthEased = Easing.easeExpoOut(healthT);
        healthFadeCurrent = healthFadeFrom + ((healthShouldFade ? HEALTH_TARGET_ALPHA : 1f) - healthFadeFrom) * healthEased;
    }

    public float getHotbarAlpha() {
        return hotbarFadeCurrent;
    }

    public float getHealthAlpha() {
        return healthFadeCurrent;
    }
}
