package com.example.backpacks.client;

import com.example.backpacks.capability.BackpackCapabilityProvider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class BackpackScreen extends AbstractContainerScreen<BackpackMenu> {
    
    private final BackpackCapabilityProvider handler;
    private final ItemStack backpackStack;
    
    private static final int SLOT_SIZE = 18;
    private static final int SLOTS_PER_ROW = 9;
    private static final int BACKPACK_ROWS = 1;
    
    public BackpackScreen(BackpackCapabilityProvider handler, Inventory playerInventory, ItemStack backpackStack) {
        super(new BackpackMenu(handler, playerInventory, backpackStack), playerInventory, Component.literal("Сумка"));
        this.handler = handler;
        this.backpackStack = backpackStack;
        
        this.imageWidth = 176;
        this.imageHeight = 114;
        this.inventoryLabelY = imageHeight - 94 + 5;
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Рисуем фон инвентаря
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        // Простой фон для сумки
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF555555);
        
        // Рисуем слоты сумки
        for (int row = 0; row < BACKPACK_ROWS; row++) {
            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                int slotX = x + 8 + col * SLOT_SIZE;
                int slotY = y + 18 + row * SLOT_SIZE;
                guiGraphics.fill(slotX - 1, slotY - 1, slotX + SLOT_SIZE + 1, slotY + SLOT_SIZE + 1, 0xFF8B8B8B);
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0xFF3F3F3F);
            }
        }
        
        // Рисуем слоты инвентаря игрока
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = x + 8 + col * SLOT_SIZE;
                int slotY = y + 54 + row * SLOT_SIZE;
                guiGraphics.fill(slotX - 1, slotY - 1, slotX + SLOT_SIZE + 1, slotY + SLOT_SIZE + 1, 0xFF8B8B8B);
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0xFF3F3F3F);
            }
        }
        
        // Рисуем горячую панель
        for (int col = 0; col < 9; col++) {
            int slotX = x + 8 + col * SLOT_SIZE;
            int slotY = y + 112;
            guiGraphics.fill(slotX - 1, slotY - 1, slotX + SLOT_SIZE + 1, slotY + SLOT_SIZE + 1, 0xFF8B8B8B);
            guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0xFF3F3F3F);
        }
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, 8, 6, 0x404040, false);
        guiGraphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
