package com.ltweaks.client.crosshair;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class c_render {

    public static void renderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;

        float pt = deltaTracker.getGameTimeDeltaPartialTick(true);

        float renderBobX = Mth.lerp(pt, c_bobbing.getPrevBobX(), c_bobbing.getCurrentBobX());
        float renderBobY = Mth.lerp(pt, c_bobbing.getPrevBobY(), c_bobbing.getCurrentBobY());

        int color = c_color.getCurrentEntityColor();
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        int currentUiScale = (int) mc.getWindow().getGuiScale();
        float targetCustomScale = Math.max(1.0f, (float) (currentUiScale - 1));
        float uiScaleFactor = targetCustomScale / (float) currentUiScale;

        ResourceLocation atlas = c_texture.getAtlasTexture();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(guiGraphics.guiWidth() / 2.0f, (guiGraphics.guiHeight() / 2.0f), 0);
        guiGraphics.pose().translate(renderBobX, renderBobY, 0);

        if (c_indicator.isIndicatorActive()) {
            float progress = c_indicator.getIndicatorProgress();
            int indicatorColor = c_indicator.getIndicatorColor();
            float ir = ((indicatorColor >> 16) & 0xFF) / 255f;
            float ig = ((indicatorColor >> 8) & 0xFF) / 255f;
            float ib = (indicatorColor & 0xFF) / 255f;
            float ia = ((indicatorColor >> 24) & 0xFF) / 255f;

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(ir, ig, ib, ia);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(uiScaleFactor, uiScaleFactor, 1.0f);
            guiGraphics.pose().translate(-5.0f, 6.0f, 0.0f);

            int emptyU = 9 * 11 + 1;
            guiGraphics.blit(atlas, 0, 0, emptyU, 0, 11, 9, 123, 9);

            if (progress > 0.0f) {
                int fullU = 10 * 11 + 1;
                int w = (int)(11 * progress);
                guiGraphics.blit(atlas, 0, 0, fullU, 0, w, 9, 123, 9);
            }

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
            guiGraphics.pose().popPose();
        }

        RenderSystem.enableBlend();

        guiGraphics.setColor(r, g, b, a);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(uiScaleFactor, uiScaleFactor, 1.0f);
        guiGraphics.pose().translate(-4.5f, -4.5f, 0.0f);

        int baseU = c_texture.getBaseIndex() * 11 + 1;
        guiGraphics.blit(atlas, 0, 0, baseU, 0, 9, 9, 123, 9);

        if (c_texture.hasOverlay()) {
            int overlayU = c_texture.getOverlayIndex() * 11 + 1;
            guiGraphics.blit(atlas, 0, 0, overlayU, 0, 9, 9, 123, 9);
        }

        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();
        guiGraphics.pose().popPose();
    }

    @EventBusSubscriber(modid = "ltweaks", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onRegisterLayers(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, ResourceLocation.fromNamespaceAndPath("ltweaks", "custom_crosshair"), c_render::renderCrosshair);
        }
    }

    @EventBusSubscriber(modid = "ltweaks", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {

        @SubscribeEvent
        public static void onRenderLayer(RenderGuiLayerEvent.Pre event) {
            if (event.getName().equals(VanillaGuiLayers.CROSSHAIR)) {
                event.setCanceled(true);
            }
        }
    }
}