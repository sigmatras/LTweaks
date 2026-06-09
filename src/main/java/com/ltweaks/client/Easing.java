package com.ltweaks.client;

public final class Easing {
    private Easing() {}

    public static float easeInOutQuad(float t) {
        return t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2f * t + 2f, 2) / 2f;
    }

    public static float easeOutQuad(float t) {
        return 1f - (1f - t) * (1f - t);
    }

    public static float easeExpoOut(float t) {
        return t >= 1f ? 1f : 1f - (float) Math.pow(2f, -10f * t);
    }

    public static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }
}
