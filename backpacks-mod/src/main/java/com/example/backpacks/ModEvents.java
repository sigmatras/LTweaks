package com.example.backpacks;

import com.example.backpacks.capability.BackpackCapabilityProvider;
import com.example.backpacks.config.BackpackConfig;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = BackpacksMod.MOD_ID)
public class ModEvents {
    
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        
        // Проверяем, экипирована ли сумка
        if (isBackpackEquipped(player)) {
            applyBackpackEffects(player);
        }
    }
    
    private static boolean isBackpackEquipped(Player player) {
        // Проверяем, есть ли у игрока сумка в инвентаре и экипирована ли она
        // В реальной реализации нужно использовать Capability или синхронизированное состояние
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof com.example.backpacks.item.BackpackItem) {
                // Для простоты считаем, что если сумка есть в инвентаре, она экипирована
                // В полной версии нужно отслеживать состояние экипировки
                return true;
            }
        }
        return false;
    }
    
    private static void applyBackpackEffects(Player player) {
        // Находим сумку и проверяем заполненность слотов
        BackpackCapabilityProvider handler = null;
        
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof com.example.backpacks.item.BackpackItem) {
                handler = new BackpackCapabilityProvider(stack);
                break;
            }
        }
        
        if (handler == null) return;
        
        // Считаем количество заполненных слотов
        int filledSlots = 0;
        for (int i = 0; i < BackpackCapabilityProvider.BACKPACK_SLOTS; i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                filledSlots++;
            }
        }
        
        // Применяем замедление от заполненных слотов 5-9
        double slowdownMultiplier = calculateSlowdown(filledSlots);
        
        // Проверяем, находится ли игрок под водой
        boolean isUnderwater = player.isEyeInFluidType(net.minecraft.tags.FluidTags.WATER);
        if (isUnderwater) {
            slowdownMultiplier *= (1.0 + BackpackConfig.UNDERWATER_SLOWDOWN_PERCENT.get());
        }
        
        // Применяем замедление к скорости игрока
        if (slowdownMultiplier > 1.0) {
            player.setDeltaMovement(
                player.getDeltaMovement().x / slowdownMultiplier,
                player.getDeltaMovement().y,
                player.getDeltaMovement().z / slowdownMultiplier
            );
        }
    }
    
    /**
     * Вычисляет множитель замедления на основе заполненных слотов.
     * Замедление начинается с 5-го слота и растет экспоненциально до 30% на 9-м слоте.
     */
    private static double calculateSlowdown(int filledSlots) {
        if (filledSlots < 5) {
            return 1.0;
        }
        
        // Нормализуем значение от 0 до 1 для слотов 5-9
        double normalizedSlot = (filledSlots - 4) / 5.0; // 0.0 для 5 слотов, 1.0 для 9 слотов
        
        // Используем экспоненциальную функцию для плавного нарастания
        // x^2 дает более мягкое начало и более резкий конец
        double exponentialFactor = Math.pow(normalizedSlot, 2);
        
        // Максимальное замедление 30%
        double maxSlowdown = BackpackConfig.MAX_SLOWDOWN_PERCENT.get();
        
        return 1.0 + (exponentialFactor * maxSlowdown);
    }
}
