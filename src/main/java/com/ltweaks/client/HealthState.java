package com.ltweaks.client;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public final class HealthState {
    public static final int COUNT = 8;

    private static final int WITHER = 1;
    private static final int POISON = 2;
    private static final int FREEZE = 4;

    private HealthState() {}

    public static int of(Player player) {
        int mask = 0;
        if (player.hasEffect(MobEffects.WITHER)) {
            mask |= WITHER;
        }
        if (player.hasEffect(MobEffects.POISON)) {
            mask |= POISON;
        }
        if (player.isFullyFrozen()) {
            mask |= FREEZE;
        }
        return mask;
    }
}
