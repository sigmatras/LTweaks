package com.example.backpacks.item;

import com.example.backpacks.capability.BackpackCapabilityProvider;
import com.example.backpacks.network.BackpackNetwork;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.CapabilityTypes;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

public class BackpackItem extends Item {
    
    public BackpackItem(Properties properties) {
        super(properties.stacksTo(1));
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide()) {
            // Открываем сумку на клиенте
            BackpackNetwork.openBackpack(stack);
            return InteractionResultHolder.success(stack);
        }
        
        return InteractionResultHolder.pass(stack);
    }
}
