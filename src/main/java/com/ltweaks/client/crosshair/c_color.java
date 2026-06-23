package com.ltweaks.client.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class c_color {

    private static int currentEntityColor = 0xFFFFFFFF;

    public static int getCurrentEntityColor() { return currentEntityColor; }

    public static int lerpColor(int current, int target, float delta) {
        int a1 = (current >> 24) & 0xFF, r1 = (current >> 16) & 0xFF, g1 = (current >> 8) & 0xFF, b1 = current & 0xFF;
        int a2 = (target >> 24) & 0xFF, r2 = (target >> 16) & 0xFF, g2 = (target >> 8) & 0xFF, b2 = target & 0xFF;
        return ((int) (a1 + (a2 - a1) * delta) << 24) | ((int) (r1 + (r2 - r1) * delta) << 16) | ((int) (g1 + (g2 - g1) * delta) << 8) | (int) (b1 + (b2 - b1) * delta);
    }

    @EventBusSubscriber(modid = "ltweaks", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            Entity target = mc.crosshairPickEntity;
            int destColor = 0xFFFFFFFF;
            if (target != null) {
                if (target instanceof NeutralMob) {
                    destColor = 0xFFFFFF88;
                } else if (target instanceof Enemy) {
                    destColor = 0xFFFF8888;
                } else if (target instanceof Animal || target instanceof WaterAnimal || target instanceof AbstractVillager || target instanceof AgeableMob) {
                    destColor = 0xFF88FF88;
                } else {
                    destColor = 0xFFFF88FF;
                }
                currentEntityColor = destColor;
            } else {
                currentEntityColor = lerpColor(currentEntityColor, 0xFFFFFFFF, 0.2f);
            }
        }
    }
}