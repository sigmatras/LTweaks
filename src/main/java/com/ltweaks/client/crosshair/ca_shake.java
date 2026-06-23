package com.ltweaks.client.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import java.util.Random;

public class ca_shake {

    private static final Random RANDOM = new Random();
    private static float intensity = 0.0f;
    private static float targetX = 0.0f;
    private static float targetY = 0.0f;
    private static float currentX = 0.0f;
    private static float currentY = 0.0f;

    public static float getShakeX() {
        return currentX;
    }

    public static float getShakeY() {
        return currentY;
    }

    public static void triggerShake(float amount) {
        if (amount > intensity) {
            intensity = amount;
        }
    }

    @EventBusSubscriber(modid = "ltweaks", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) {
                return;
            }

            if (mc.player.isUsingItem()) {
                ItemStack useItem = mc.player.getUseItem();
                if (useItem.has(DataComponents.FOOD) && mc.player.tickCount % 4 == 0) {
                    triggerShake(1.5f);
                }
            }

            if (intensity > 0.01f) {
                targetX = (RANDOM.nextFloat() - 0.5f) * 2.0f * intensity;
                targetY = (RANDOM.nextFloat() - 0.5f) * 2.0f * intensity;
                intensity *= 0.85f;
            } else {
                intensity = 0.0f;
                targetX = 0.0f;
                targetY = 0.0f;
            }

            currentX += (targetX - currentX) * 0.6f;
            currentY += (targetY - currentY) * 0.6f;
        }

        @SubscribeEvent
        public static void onExplosion(ExplosionEvent.Detonate event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }

            Vec3 pos = event.getExplosion().center();
            double dist = mc.player.position().distanceTo(pos);
            float amount = (float) Math.max(0, 20.0 - dist);
            triggerShake(amount);
        }

        @SubscribeEvent
        public static void onAttack(AttackEntityEvent event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || event.getEntity() != mc.player) {
                return;
            }

            ItemStack mainHand = mc.player.getMainHandItem();
            Item item = mainHand.getItem();

            if (item instanceof MaceItem) {
                if (!mc.player.onGround() && mc.player.fallDistance > 1.5f) {
                    triggerShake(15.0f);
                }
            } else {
                if (!mc.player.onGround() && mc.player.fallDistance > 0.0f) {
                    triggerShake(8.0f);
                }
            }
        }

        @SubscribeEvent
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            if (event.getEntity() instanceof LocalPlayer) {
                ItemStack stack = event.getItemStack();
                Item item = stack.getItem();

                if (item instanceof EggItem || item instanceof SnowballItem || item instanceof ThrowablePotionItem || item instanceof WindChargeItem) {
                    triggerShake(5.0f);
                } else if (item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem) {
                    triggerShake(10.0f);
                }
            }
        }

        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && event.getPlayer() == mc.player) {
                triggerShake(3.3f);
            }
        }
    }
}