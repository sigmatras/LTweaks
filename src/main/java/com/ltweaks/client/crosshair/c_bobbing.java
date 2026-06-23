package com.ltweaks.client.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class c_bobbing {

    private static float currentBobX = 0f, targetBobX = 0f, prevBobX = 0f;
    private static float currentBobY = 0f, targetBobY = 0f, prevBobY = 0f;

    public static float getCurrentBobX() { return currentBobX; }
    public static float getPrevBobX() { return prevBobX; }
    public static float getCurrentBobY() { return currentBobY; }
    public static float getPrevBobY() { return prevBobY; }

    @EventBusSubscriber(modid = "ltweaks", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            prevBobX = currentBobX;
            prevBobY = currentBobY;

            Vec3 delta = mc.player.getDeltaMovement();
            float yawRad = (float) Math.toRadians(mc.player.getYRot());
            float sinYaw = Mth.sin(yawRad);
            float cosYaw = Mth.cos(yawRad);
            float forwardSpeed = (float) (delta.x * -sinYaw + delta.z * cosYaw);
            float sideSpeed = (float) (delta.x * cosYaw + delta.z * sinYaw);
            float yawDelta = mc.player.getYRot() - mc.player.yRotO;
            float pitchDelta = mc.player.getXRot() - mc.player.xRotO;

            float fallSpeed = (float) delta.y;
            if (!mc.player.onGround()) {
                fallSpeed *= 0.25f;
            }

            targetBobX = -sideSpeed * 15f - yawDelta * 1.2f;
            targetBobY = -fallSpeed * 15f - pitchDelta * 1.2f + (float) Math.sin(mc.player.tickCount * 0.3f) * (forwardSpeed * 4f);

            currentBobX += (targetBobX - currentBobX) * 0.3f;
            currentBobY += (targetBobY - currentBobY) * 0.3f;
        }
    }
}