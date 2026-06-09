package com.ltweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;

public final class HotbarAnimator {
    public static final HotbarAnimator INSTANCE = new HotbarAnimator();

    private static final float DURATION = 0.25f;
    private static final float CLOSE_DURATION = 0.25f;
    private static final float MENU_DROP = 60f;
    private static final float CHAT_RISE = 16f;

    private boolean menuOpen;
    private long menuStart;
    private float menuFrom;
    private float menuCurrent;

    private boolean chatOpen;
    private long chatStart;
    private float chatFrom;
    private float chatCurrent;

    private HotbarAnimator() {}

    public void update() {
        long now = System.nanoTime();

        Screen screen = Minecraft.getInstance().screen;
        boolean chatNow = screen instanceof ChatScreen;
        boolean menuNow = screen != null && !chatNow;

        if (menuNow != menuOpen) {
            menuOpen = menuNow;
            menuFrom = menuCurrent;
            menuStart = now;
        }
        if (chatNow != chatOpen) {
            chatOpen = chatNow;
            chatFrom = chatCurrent;
            chatStart = now;
        }

        float dur = menuOpen ? DURATION : CLOSE_DURATION;
        float menuT = Easing.clamp01((now - menuStart) / 1_000_000_000f / dur);
        float menuEased = menuOpen ? Easing.easeInOutQuad(menuT) : Easing.easeOutQuad(menuT);
        menuCurrent = menuFrom + ((menuOpen ? 1f : 0f) - menuFrom) * menuEased;

        float chatT = Easing.clamp01((now - chatStart) / 1_000_000_000f / DURATION);
        chatCurrent = chatFrom + ((chatOpen ? 1f : 0f) - chatFrom) * Easing.easeExpoOut(chatT);
    }

    public float yOffset() {
        return (menuCurrent * MENU_DROP) - (chatCurrent * CHAT_RISE);
    }

    public boolean menuOpen() {
        return menuOpen;
    }

    public void forceChatStart() {
        chatOpen = true;
        chatFrom = 0f;
        chatCurrent = 0f;
        chatStart = System.nanoTime();
    }

    public boolean hotbarHidden() {
        return menuCurrent >= 0.999f;
    }
}
