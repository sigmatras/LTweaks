package com.ltweaks.client;

import com.ltweaks.LTweaks;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = LTweaks.MOD_ID, value = Dist.CLIENT)
public final class HudHandler {

    private static boolean containerPushed;

    @SubscribeEvent
    public static void onLayerPre(RenderGuiLayerEvent.Pre event) {
        var name = event.getName();

        if (name.equals(VanillaGuiLayers.PLAYER_HEALTH)
                || name.equals(VanillaGuiLayers.FOOD_LEVEL)
                || name.equals(VanillaGuiLayers.AIR_LEVEL)
                || name.equals(VanillaGuiLayers.ARMOR_LEVEL)
                || name.equals(VanillaGuiLayers.EXPERIENCE_BAR)
                || name.equals(VanillaGuiLayers.EXPERIENCE_LEVEL)
                || name.equals(VanillaGuiLayers.JUMP_METER)
                || name.equals(VanillaGuiLayers.VEHICLE_HEALTH)) {
            event.setCanceled(true);
            return;
        }

        if (name.equals(VanillaGuiLayers.HOTBAR)) {
            HotbarAnimator.INSTANCE.update();
            ItemNameAnim.INSTANCE.checkSlot();
            FadeController.INSTANCE.update();

            float alpha = FadeController.INSTANCE.getHotbarAlpha();
            event.getGuiGraphics().setColor(1f, 1f, 1f, alpha);

            PoseStack pose = event.getGuiGraphics().pose();
            pose.pushPose();
            pose.translate(0f, HotbarAnimator.INSTANCE.yOffset(), 0f);

            Minecraft mc = Minecraft.getInstance();
            mc.gui.renderHotbar(event.getGuiGraphics(), mc.getTimer());
            HealthBar.INSTANCE.render(event.getGuiGraphics());

            event.setCanceled(true);
            return;
        }

        if (name.equals(VanillaGuiLayers.SELECTED_ITEM_NAME)) {
            float scale = ItemNameAnim.INSTANCE.scale();
            float cx = event.getGuiGraphics().guiWidth() / 2f;
            float cy = event.getGuiGraphics().guiHeight() - 59 + 4.5f;
            PoseStack pose = event.getGuiGraphics().pose();
            pose.pushPose();
            pose.translate(0f, HotbarAnimator.INSTANCE.yOffset(), 0f);
            pose.translate(cx, cy, 0f);
            pose.scale(scale, scale, 1f);
            pose.translate(-cx, -cy, 0f);
            return;
        }


    }

    @SubscribeEvent
    public static void onLayerPost(RenderGuiLayerEvent.Post event) {
        var name = event.getName();
        if (name.equals(VanillaGuiLayers.HOTBAR)) {
            event.getGuiGraphics().pose().popPose();
            event.getGuiGraphics().setColor(1f, 1f, 1f, 1f);
        }
        if (name.equals(VanillaGuiLayers.SELECTED_ITEM_NAME)) {
            event.getGuiGraphics().pose().popPose();
        }
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof AbstractContainerScreen) {
            ContainerAnim.INSTANCE.open();
        }
        if (event.getNewScreen() instanceof ChatScreen) {
            HotbarAnimator.INSTANCE.forceChatStart();
        }
    }

    @SubscribeEvent
    public static void onScreenPre(ScreenEvent.Render.Pre event) {
        HotbarAnimator.INSTANCE.update();

        if (event.getScreen() instanceof AbstractContainerScreen) {
            PoseStack pose = event.getGuiGraphics().pose();
            pose.pushPose();
            pose.translate(0f, ContainerAnim.INSTANCE.openSlideOffset(), 0f);
            containerPushed = true;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        FadeController.INSTANCE.update();

        if (mc.screen != null) {
            long window = mc.getWindow().getWindow();
            syncKey(mc.options.keyUp, window);
            syncKey(mc.options.keyDown, window);
            syncKey(mc.options.keyLeft, window);
            syncKey(mc.options.keyRight, window);
            syncKey(mc.options.keyJump, window);
            syncKey(mc.options.keyShift, window);
            syncKey(mc.options.keySprint, window);
        }
    }

    private static void syncKey(KeyMapping key, long window) {
        if (key.getKey().getType() != InputConstants.Type.KEYSYM) {
            return;
        }
        boolean down = InputConstants.isKeyDown(window, key.getKey().getValue());
        key.setDown(down);
    }

    @SubscribeEvent
    public static void onScreenPost(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen) {
            if (containerPushed) {
                event.getGuiGraphics().pose().popPose();
                containerPushed = false;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiPre(RenderGuiEvent.Pre event) {
        if (!ContainerAnim.INSTANCE.hasGhost()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            return;
        }
        if (ContainerAnim.INSTANCE.closeDone()) {
            ContainerAnim.INSTANCE.clearGhost();
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        Screen ghost = ContainerAnim.INSTANCE.ghost();
        float slide = ContainerAnim.INSTANCE.closeSlideOffset(graphics.guiHeight());

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0f, slide, 0f);
        ghost.render(graphics, -1, -1, mc.getTimer().getGameTimeDeltaPartialTick(false));
        pose.popPose();
    }
}