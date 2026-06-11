package com.ltweaks.client;

import net.minecraft.client.gui.screens.Screen;

public final class ContainerAnim {
    public static final ContainerAnim INSTANCE = new ContainerAnim();

    private static final float DURATION = 0.5f;
    private static final float OPEN_SLIDE = 24f;

    private long openStart;
    private boolean opening;

    private long closeStart;
    private Screen ghost;

    private ContainerAnim() {}

    public void open() {
        opening = true;
        openStart = System.nanoTime();
    }

    public void startClose(Screen screen) {
        ghost = screen;
        closeStart = System.nanoTime();
    }

    public boolean hasGhost() {
        return ghost != null;
    }

    public Screen ghost() {
        return ghost;
    }

    private float closeRaw() {
        return Easing.clamp01((System.nanoTime() - closeStart) / 1_000_000_000f / DURATION);
    }

    public boolean closeDone() {
        return ghost != null && closeRaw() >= 1f;
    }

    public void clearGhost() {
        ghost = null;
    }

    public float openSlideOffset() {
        if (!opening) {
            return 0f;
        }
        float t = Easing.clamp01((System.nanoTime() - openStart) / 1_000_000_000f / DURATION);
        if (t >= 1f) {
            opening = false;
        }
        return (1f - Easing.easeExpoOut(t)) * OPEN_SLIDE;
    }

    public float closeSlideOffset(float screenHeight) {
        return Easing.easeExpoOut(closeRaw()) * screenHeight;
    }
}
