package com.example.backpacks.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

@OnlyIn(Dist.CLIENT)
public class BackpackModel extends HumanoidModel<LivingEntity> {
    
    private final ModelPart backpack;
    
    public BackpackModel(ModelPart root) {
        super(root);
        this.backpack = root.getChild("backpack");
    }
    
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        
        // Создаем модель сумки на спине
        PartDefinition backpack = partdefinition.addOrReplaceChild(
            "backpack",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, 2.0F, -6.0F, 8.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        
        // Ремни сумки
        backpack.addOrReplaceChild(
            "strap_left",
            CubeListBuilder.create()
                .texOffs(0, 13)
                .addBox(-3.5F, 2.0F, -5.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.ZERO
        );
        
        backpack.addOrReplaceChild(
            "strap_right",
            CubeListBuilder.create()
                .texOffs(0, 13)
                .addBox(2.5F, 2.0F, -5.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.ZERO
        );
        
        return LayerDefinition.create(meshdefinition, 32, 32);
    }
    
    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, 
                         float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        
        // Сумка следует за движением тела
        backpack.yRot = body.yRot;
        backpack.xRot = body.xRot;
    }
}
