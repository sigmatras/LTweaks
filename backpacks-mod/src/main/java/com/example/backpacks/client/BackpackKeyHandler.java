package com.example.backpacks.client;

import com.example.backpacks.BackpacksMod;
import com.example.backpacks.capability.BackpackCapabilityProvider;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = BackpacksMod.MOD_ID, value = Dist.CLIENT)
public class BackpackKeyHandler {
    
    public static final KeyMapping OPEN_BACKPACK_KEY = new KeyMapping(
        "key.backpacks.open",
        GLFW.GLFW_KEY_B,
        "category.backpacks"
    );
    
    private static final Map<UUID, Boolean> equippedBackpacks = new HashMap<>();
    private static boolean wasKeyDown = false;
    
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_BACKPACK_KEY);
    }
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        
        if (player == null) return;
        
        boolean isKeyDown = OPEN_BACKPACK_KEY.isDown();
        
        // Обработка нажатия клавиши для экипировки/снятия сумки
        if (isKeyDown && !wasKeyDown) {
            // Ищем сумку в инвентаре
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() instanceof com.example.backpacks.item.BackpackItem) {
                    toggleBackpackEquipped(player);
                    openBackpackGUI(stack, player);
                    break;
                }
            }
        }
        
        wasKeyDown = isKeyDown;
    }
    
    private static void toggleBackpackEquipped(Player player) {
        UUID playerId = player.getUUID();
        equippedBackpacks.put(playerId, !equippedBackpacks.getOrDefault(playerId, false));
    }
    
    public static boolean isBackpackEquipped(Player player) {
        return equippedBackpacks.getOrDefault(player.getUUID(), false);
    }
    
    private static void openBackpackGUI(ItemStack stack, Player player) {
        if (player.level().isClientSide()) {
            BackpackCapabilityProvider handler = new BackpackCapabilityProvider(stack);
            Minecraft.getInstance().setScreen(new BackpackScreen(handler, player.getInventory(), stack));
        }
    }
    
    public static void clearEquippedState(Player player) {
        equippedBackpacks.remove(player.getUUID());
    }
}
