package com.example.backpacks.capability;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class BackpackCapabilityProvider implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {
    
    public static final int BACKPACK_SLOTS = 9;
    
    private final ItemStack stack;
    private NonNullList<ItemStack> stacks;
    
    public BackpackCapabilityProvider(ItemStack stack) {
        this.stack = stack;
        this.stacks = NonNullList.withSize(BACKPACK_SLOTS, ItemStack.EMPTY);
        loadNBTData(stack.getOrCreateTag());
    }
    
    @Override
    public void setStackInSlot(int slot, ItemStack itemStack) {
        validateSlotIndex(slot);
        stacks.set(slot, itemStack);
        onContentsChanged(slot);
    }
    
    @Override
    public int getSlots() {
        return BACKPACK_SLOTS;
    }
    
    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return stacks.get(slot);
    }
    
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        validateSlotIndex(slot);
        
        ItemStack existing = stacks.get(slot);
        int limit = Math.min(getStackLimit(slot, stack), stack.getMaxStackSize());
        
        if (!existing.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
                return stack;
            }
            
            int toAdd = Math.min(stack.getCount(), limit - existing.getCount());
            if (toAdd <= 0) {
                return stack;
            }
            
            if (!simulate) {
                existing.grow(toAdd);
                onContentsChanged(slot);
            }
            
            if (toAdd == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            
            return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - toAdd);
        }
        
        int toAdd = Math.min(stack.getCount(), limit);
        if (toAdd <= 0) {
            return stack;
        }
        
        if (!simulate) {
            stacks.set(slot, ItemHandlerHelper.copyStackWithSize(stack, toAdd));
            onContentsChanged(slot);
        }
        
        if (toAdd == stack.getCount()) {
            return ItemStack.EMPTY;
        }
        
        return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - toAdd);
    }
    
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        }
        
        validateSlotIndex(slot);
        
        ItemStack existing = stacks.get(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        int toExtract = Math.min(amount, existing.getMaxStackSize());
        
        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
            }
            return existing;
        } else {
            if (!simulate) {
                stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
                onContentsChanged(slot);
            }
            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }
    
    @Override
    public int getSlotLimit(int slot) {
        validateSlotIndex(slot);
        return 64;
    }
    
    protected int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }
    
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < BACKPACK_SLOTS; i++) {
            if (!stacks.get(i).isEmpty()) {
                tag.put("slot_" + i, stacks.get(i).save(new CompoundTag()));
            }
        }
        return tag;
    }
    
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (int i = 0; i < BACKPACK_SLOTS; i++) {
            String key = "slot_" + i;
            if (nbt.contains(key)) {
                stacks.set(i, ItemStack.parseOptional(nbt.getCompound(key)));
            } else {
                stacks.set(i, ItemStack.EMPTY);
            }
        }
    }
    
    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= BACKPACK_SLOTS) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + BACKPACK_SLOTS + ")");
        }
    }
    
    protected void onContentsChanged(int slot) {
        // Можно добавить синхронизацию при необходимости
    }
    
    public void loadNBTData(CompoundTag tag) {
        if (tag.contains("BackpackItems")) {
            CompoundTag itemsTag = tag.getCompound("BackpackItems");
            deserializeNBT(itemsTag);
        }
    }
    
    public void saveNBTData(CompoundTag tag) {
        tag.put("BackpackItems", serializeNBT());
    }
}
