package com.example.backpacks.client;

import com.example.backpacks.BackpacksMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = BackpacksMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientModEvents {
    
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
            BackpackModelLayer.LAYER_LOCATION,
            BackpackModel::createBodyLayer
        );
    }
    
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        BackpackKeyHandler.registerKeyMappings(event);
    }
}
