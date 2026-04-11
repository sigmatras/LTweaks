package com.example.backpacks.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import com.example.backpacks.BackpacksMod;

public class BackpackModelLayer {
    public static final ModelLayerLocation LAYER_LOCATION = 
        new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(BackpacksMod.MOD_ID, "backpack"),
            "main"
        );
}
