package com.example.backpacks.client;

import com.example.backpacks.BackpacksMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

@OnlyIn(Dist.CLIENT)
public class BackpackRenderer implements IClientItemExtensions {
    
    public static final ResourceLocation BACKPACK_TEXTURE = 
        ResourceLocation.fromNamespaceAndPath(BackpacksMod.MOD_ID, "textures/entity/backpack.png");
    
    private final BackpackModel model;
    
    public BackpackRenderer() {
        this.model = new BackpackModel(Minecraft.getInstance().getEntityModels()
            .bakeLayer(BackpackModelLayer.LAYER_LOCATION));
    }
    
    @Override
    public void renderHumanoidArmorLayer(ItemStack stack, LivingEntity entity, 
                                         EquipmentSlot slot, PoseStack poseStack, 
                                         MultiBufferSource buffer, int packedLight) {
        if (entity instanceof AbstractClientPlayer player) {
            // Проверяем, экипирована ли сумка
            if (BackpackKeyHandler.isBackpackEquipped(player)) {
                poseStack.pushPose();
                
                // Масштабируем и позиционируем сумку
                poseStack.scale(1.0F, 1.0F, 1.0F);
                poseStack.translate(0.0F, 0.0F, 0.0F);
                
                Minecraft.getInstance().getTextureManager()
                    .getTexture(BACKPACK_TEXTURE)
                    .bindForReading();
                
                model.renderToBuffer(poseStack, buffer.getBuffer(model.renderType(BACKPACK_TEXTURE)), 
                                    packedLight, 0xFFFFFF, 1.0F, 1.0F, 1.0F, 1.0F);
                
                poseStack.popPose();
            }
        }
    }
}
