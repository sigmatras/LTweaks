package com.example.backpacks.client;

import com.example.backpacks.capability.BackpackCapabilityProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackpackMenu extends AbstractContainerMenu {
    
    private final BackpackCapabilityProvider handler;
    private final Inventory playerInventory;
    private final ItemStack backpackStack;
    
    public BackpackMenu(BackpackCapabilityProvider handler, Inventory playerInventory, ItemStack backpackStack) {
        super(null, 0);
        this.handler = handler;
        this.playerInventory = playerInventory;
        this.backpackStack = backpackStack;
        
        // Добавляем слоты сумки (9 слотов в одном ряду)
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(handler, i, 8 + i * 18, 18));
        }
        
        // Добавляем слоты инвентаря игрока (3 ряда по 9 слотов)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 54 + row * 18));
            }
        }
        
        // Добавляем горячую панель (9 слотов)
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 112));
        }
    }
    
    @Override
    public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            
            if (index < 9) {
                // Перемещение из сумки в инвентарь
                if (!moveItemStackTo(itemstack1, 9, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Перемещение из инвентаря в сумку
                if (!moveItemStackTo(itemstack1, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        
        return itemstack;
    }
    
    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return true;
    }
}
