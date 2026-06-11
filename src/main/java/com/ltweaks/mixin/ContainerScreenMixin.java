package com.ltweaks.mixin;

import com.ltweaks.client.ContainerAnim;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenMixin {

    @Inject(method = "onClose", at = @At("HEAD"))
    private void ltweaks$captureGhost(CallbackInfo ci) {
        ContainerAnim.INSTANCE.startClose((Screen) (Object) this);
    }
}
