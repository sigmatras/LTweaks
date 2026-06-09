package com.ltweaks.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "renderBlurredBackground", at = @At("HEAD"), cancellable = true)
    private void ltweaks$noBlur(float partialTick, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null) ci.cancel();
    }

    @Inject(method = "renderTransparentBackground", at = @At("HEAD"), cancellable = true)
    private void ltweaks$noDim(GuiGraphics graphics, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null) ci.cancel();
    }


}
