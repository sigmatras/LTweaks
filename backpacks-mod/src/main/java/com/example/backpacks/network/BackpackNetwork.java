package com.example.backpacks.network;

import com.example.backpacks.BackpacksMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class BackpackNetwork {
    
    private static boolean registered = false;
    
    public static void init() {
        // Регистрация сетевых каналов будет вызвана из события
    }
    
    public static void register(RegisterPayloadHandlersEvent event) {
        if (registered) return;
        registered = true;
        
        PayloadRegistrar registrar = event.registrar(BackpacksMod.MOD_ID);
        // Здесь можно зарегистрировать пакеты для синхронизации
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void openBackpack(ItemStack stack) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            // Открываем GUI сумки
            Minecraft.getInstance().setScreen(new com.example.backpacks.client.BackpackScreen(
                new com.example.backpacks.capability.BackpackCapabilityProvider(stack),
                player.getInventory(),
                stack
            ));
        }
    }
}
