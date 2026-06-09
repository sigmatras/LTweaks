package com.ltweaks.client;

import net.minecraft.client.Minecraft;

public final class ItemNameAnim {
    public static final ItemNameAnim INSTANCE = new ItemNameAnim();

    private static final float DURATION = 0.3f;
    private static final float START_SCALE = 1.25f;
    private static final float END_SCALE = 1f;

    private int lastSlot = -1;
    private long animStart;

    private ItemNameAnim() {}

    public void trigger() {
        animStart = System.nanoTime();
    }

    public void checkSlot() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        int slot = mc.player.getInventory().selected;
        if (slot != lastSlot) {
            lastSlot = slot;
            trigger();
        }
    }

    public float scale() {
        float t = Easing.clamp01((System.nanoTime() - animStart) / 1_000_000_000f / DURATION);
        return START_SCALE + (END_SCALE - START_SCALE) * Easing.easeExpoOut(t);
    }
}
