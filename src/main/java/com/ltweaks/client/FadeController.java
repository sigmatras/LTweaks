package com.ltweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class FadeController {

    public static final FadeController INSTANCE = new FadeController();

    private static final long IDLE_THRESHOLD = 3_000_000_000L; // 3 секунды
    private static final float FADE_DURATION = 1.0f;           // 1 секунда
    private static final float HOTBAR_TARGET_ALPHA = 0.2f;
    private static final float HEALTH_TARGET_ALPHA = 0.1f;

    private long lastSlotChange = System.nanoTime();
    private long lastHealthArmorChange = System.nanoTime();

    private float hotbarAlpha = 1f;
    private float healthAlpha = 1f;

    private int lastSelectedSlot = -1;
    private float lastHealth = Float.NaN;
    private int lastArmor = -1;

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

        // Вычисление альфы хотбара
        long hotbarIdle = now - lastSlotChange;
        hotbarAlpha = calculateAlpha(hotbarIdle, HOTBAR_TARGET_ALPHA);

        // Вычисление альфы полоски здоровья
        long healthIdle = now - lastHealthArmorChange;
        healthAlpha = calculateAlpha(healthIdle, HEALTH_TARGET_ALPHA);
    }

    private float calculateAlpha(long idleTime, float targetAlpha) {
        if (idleTime < IDLE_THRESHOLD) {
            return 1f;
        }
        float progress = Math.min(1f, (idleTime - IDLE_THRESHOLD) / 1_000_000_000f / FADE_DURATION);
        float eased = Easing.easeExpoOut(progress);
        return 1f + (targetAlpha - 1f) * eased;
    }

    public float getHotbarAlpha() {
        return hotbarAlpha;
    }

    public float getHealthAlpha() {
        return healthAlpha;
    }
}