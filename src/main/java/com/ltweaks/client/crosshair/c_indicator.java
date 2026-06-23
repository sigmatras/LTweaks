package com.ltweaks.client.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.lang.reflect.Field;

public class c_indicator {

    private static boolean indicatorActive = false;
    private static float indicatorProgress = 0.0f;
    private static int indicatorColor = 0xFFFFFFFF;

    private static Field destroyProgressField;

    static {
        try {
            destroyProgressField = MultiPlayerGameMode.class.getDeclaredField("destroyProgress");
            destroyProgressField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            destroyProgressField = null;
        }
    }

    public static boolean isIndicatorActive() { return indicatorActive; }
    public static float getIndicatorProgress() { return indicatorProgress; }
    public static int getIndicatorColor() { return indicatorColor; }

    @EventBusSubscriber(modid = "ltweaks", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            Player player = mc.player;
            indicatorActive = false;
            indicatorProgress = 0.0f;
            indicatorColor = 0xFFFFFFFF;

            if (player.isUsingItem()) {
                ItemStack useItem = player.getUseItem();
                int dur = useItem.getUseDuration(player);
                if (dur > 0) {
                    float p = (dur - player.getUseItemRemainingTicks()) / (float) dur;

                    if (useItem.has(DataComponents.FOOD) || useItem.getUseAnimation() == UseAnim.DRINK) {
                        indicatorProgress = Mth.clamp(p, 0f, 1f);
                        indicatorActive = true;
                    } else if (useItem.getItem() instanceof BowItem) {
                        indicatorProgress = Mth.clamp((dur - player.getUseItemRemainingTicks()) / 20f, 0f, 1f);
                        indicatorActive = true;
                    } else if (useItem.getItem() instanceof TridentItem) {
                        indicatorProgress = Mth.clamp((dur - player.getUseItemRemainingTicks()) / 10f, 0f, 1f);
                        indicatorActive = true;
                    } else if (useItem.getItem() instanceof CrossbowItem) {
                        indicatorProgress = Mth.clamp(p, 0f, 1f);
                        indicatorActive = true;
                    }
                }
            }

            if (!indicatorActive && mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
                if (mc.gameMode != null && destroyProgressField != null) {
                    try {
                        float bp = destroyProgressField.getFloat(mc.gameMode);
                        if (bp > 0f) {
                            indicatorProgress = bp;
                            indicatorActive = true;
                        }
                    } catch (Exception e) { }
                }
            }

            if (!indicatorActive) {
                float maxCd = 0.0f;
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();
                if (!mainHand.isEmpty()) maxCd = Math.max(maxCd, player.getCooldowns().getCooldownPercent(mainHand.getItem(), 1.0f));
                if (!offHand.isEmpty()) maxCd = Math.max(maxCd, player.getCooldowns().getCooldownPercent(offHand.getItem(), 1.0f));

                if (maxCd > 0.01f) {
                    indicatorProgress = 1.0f - maxCd;
                    indicatorColor = 0xFFFF0000;
                    indicatorActive = true;
                }
            }

            if (!indicatorActive) {
                float str = player.getAttackStrengthScale(1.0f);
                if (str < 0.99f) {
                    indicatorProgress = str;
                    indicatorActive = true;
                }
            }

            if (!indicatorActive && mc.hitResult != null) {
                if (mc.hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockPos pos = ((BlockHitResult) mc.hitResult).getBlockPos();
                    BlockState state = mc.level.getBlockState(pos);
                    Block block = state.getBlock();

                    if (block instanceof CropBlock crop) {
                        indicatorProgress = (float) state.getValue(CropBlock.AGE) / (float) crop.getMaxAge();
                        indicatorActive = true;
                    } else if (block instanceof SaplingBlock) {
                        indicatorProgress = (float) state.getValue(SaplingBlock.STAGE);
                        indicatorActive = true;
                    }
                } else if (mc.hitResult.getType() == HitResult.Type.ENTITY) {
                    Entity entity = ((EntityHitResult) mc.hitResult).getEntity();
                    if (entity instanceof AgeableMob mob) {
                        int age = mob.getAge();
                        if (age < 0) {
                            indicatorProgress = 1.0f + (age / -24000.0f);
                            indicatorActive = true;
                        } else if (age > 0) {
                            indicatorProgress = 1.0f - (age / 6000.0f);
                            indicatorActive = true;
                        }
                    }
                }
            }
        }
    }
}