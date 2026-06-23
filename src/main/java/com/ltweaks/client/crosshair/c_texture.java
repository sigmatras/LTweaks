package com.ltweaks.client.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "ltweaks", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class c_texture {
    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("ltweaks", "textures/gui/crosshairs.png");

    public enum BaseCrosshair {
        SPAWN(0),
        DEFAULT(1),
        CORRECT_TOOL(2),
        ENTITY(3),
        WRONG_TOOL(4),
        FUNCTIONAL(7),
        DISPOSABLE(8);

        private final int index;

        BaseCrosshair(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }
    }

    public enum OverlayCrosshair {
        NONE(-1),
        MINOR_INTERACT(5),
        MAJOR_INTERACT(6);

        private final int index;

        OverlayCrosshair(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

        public boolean isPresent() {
            return this != NONE;
        }
    }

    private static BaseCrosshair activeBase = BaseCrosshair.DEFAULT;
    private static OverlayCrosshair activeOverlay = OverlayCrosshair.NONE;

    public static ResourceLocation getAtlasTexture() {
        return ATLAS;
    }

    public static int getBaseIndex() {
        return activeBase.getIndex();
    }

    public static int getOverlayIndex() {
        return activeOverlay.getIndex();
    }

    public static boolean hasOverlay() {
        return activeOverlay.isPresent();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            activeBase = BaseCrosshair.DEFAULT;
            activeOverlay = OverlayCrosshair.NONE;
            return;
        }

        ItemStack mainHand = mc.player.getMainHandItem();
        HitResult hit = mc.hitResult;

        activeBase = BaseCrosshair.DEFAULT;
        activeOverlay = OverlayCrosshair.NONE;

        net.minecraft.world.item.Item heldItem = mainHand.getItem();
        if (heldItem == net.minecraft.world.item.Items.ENDER_PEARL ||
                heldItem == net.minecraft.world.item.Items.END_CRYSTAL ||
                heldItem == net.minecraft.world.item.Items.SNOWBALL ||
                heldItem == net.minecraft.world.item.Items.EGG ||
                heldItem == net.minecraft.world.item.Items.EXPERIENCE_BOTTLE) {
            activeBase = BaseCrosshair.DISPOSABLE;
            return;
        }

        if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
            net.minecraft.world.entity.Entity entity = ((EntityHitResult) hit).getEntity();
            if (entity instanceof net.minecraft.world.entity.decoration.ItemFrame) {
                activeOverlay = OverlayCrosshair.MINOR_INTERACT;
            } else {
                activeBase = BaseCrosshair.ENTITY;
            }
            return;
        }

        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            net.minecraft.world.level.block.state.BlockState state = mc.level.getBlockState(blockHit.getBlockPos());
            net.minecraft.world.level.block.Block block = state.getBlock();

            if (mainHand.getItem() instanceof net.minecraft.world.item.SpawnEggItem ||
                    mainHand.getItem() instanceof net.minecraft.world.item.BoatItem ||
                    mainHand.getItem() instanceof net.minecraft.world.item.HangingEntityItem ||
                    mainHand.getItem() instanceof net.minecraft.world.item.EndCrystalItem ||
                    mainHand.getItem() instanceof net.minecraft.world.item.ArmorStandItem ||
                    mainHand.getItem() instanceof net.minecraft.world.item.MinecartItem) {
                activeBase = BaseCrosshair.SPAWN;
                return;
            }

            if (mainHand.getItem() instanceof net.minecraft.world.item.AxeItem) {
                if (state.is(net.minecraft.tags.BlockTags.LOGS) || block.getDescriptionId().contains("copper")) {
                    activeOverlay = OverlayCrosshair.MAJOR_INTERACT;
                }
            }

            if (isFunctionalBlock(block, mainHand)) {
                activeOverlay = OverlayCrosshair.MINOR_INTERACT;
            }

            if (mainHand.getItem() instanceof net.minecraft.world.item.DiggerItem) {
                if (mainHand.getDestroySpeed(state) > 1.0F) {
                    activeBase = BaseCrosshair.CORRECT_TOOL;
                } else {
                    activeBase = BaseCrosshair.WRONG_TOOL;
                }
                return;
            }
        }

        if (activeOverlay.isPresent()) {
            return;
        }

        if (mainHand.getItem() instanceof net.minecraft.world.item.FishingRodItem ||
                mainHand.getItem() instanceof net.minecraft.world.item.FlintAndSteelItem ||
                mainHand.getItem() instanceof net.minecraft.world.item.ShearsItem ||
                mainHand.getItem() instanceof net.minecraft.world.item.BrushItem ||
                mainHand.getItem() instanceof net.minecraft.world.item.SpyglassItem ||
                mainHand.getItem() instanceof net.minecraft.world.item.AxeItem) {
            activeBase = BaseCrosshair.FUNCTIONAL;
        }
    }
    public static boolean isFunctionalBlock(net.minecraft.world.level.block.Block block, net.minecraft.world.item.ItemStack heldItem) {
        if (block instanceof net.minecraft.world.level.block.CauldronBlock) {
            return heldItem.getItem() instanceof net.minecraft.world.item.BucketItem ||
                    heldItem.getItem() instanceof net.minecraft.world.item.SolidBucketItem ||
                    heldItem.is(net.minecraft.world.item.Items.GLASS_BOTTLE) ||
                    heldItem.has(net.minecraft.core.component.DataComponents.POTION_CONTENTS) ||
                    heldItem.has(net.minecraft.core.component.DataComponents.DYED_COLOR);
        }
        if (block instanceof net.minecraft.world.level.block.ComposterBlock) {
            return !heldItem.isEmpty();
        }
        if (block instanceof net.minecraft.world.level.block.CampfireBlock) {
            return heldItem.getItem() instanceof net.minecraft.world.item.ShovelItem || heldItem.has(net.minecraft.core.component.DataComponents.FOOD);
        }
        if (block instanceof net.minecraft.world.level.block.BeehiveBlock) {
            return heldItem.getItem() instanceof net.minecraft.world.item.ShearsItem || heldItem.is(net.minecraft.world.item.Items.GLASS_BOTTLE);
        }
        if (block instanceof net.minecraft.world.level.block.JukeboxBlock) {
            return heldItem.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE);
        }

        return block instanceof net.minecraft.world.level.block.DoorBlock ||
                block instanceof net.minecraft.world.level.block.FenceGateBlock ||
                block instanceof net.minecraft.world.level.block.TrapDoorBlock ||
                block instanceof net.minecraft.world.level.block.ButtonBlock ||
                block instanceof net.minecraft.world.level.block.LeverBlock ||
                block instanceof net.minecraft.world.level.block.ChestBlock ||
                block instanceof net.minecraft.world.level.block.TrappedChestBlock ||
                block instanceof net.minecraft.world.level.block.EnderChestBlock ||
                block instanceof net.minecraft.world.level.block.FurnaceBlock ||
                block instanceof net.minecraft.world.level.block.BlastFurnaceBlock ||
                block instanceof net.minecraft.world.level.block.SmokerBlock ||
                block instanceof net.minecraft.world.level.block.BrewingStandBlock ||
                block instanceof net.minecraft.world.level.block.AnvilBlock ||
                block instanceof net.minecraft.world.level.block.CraftingTableBlock ||
                block instanceof net.minecraft.world.level.block.CartographyTableBlock ||
                block instanceof net.minecraft.world.level.block.SmithingTableBlock ||
                block instanceof net.minecraft.world.level.block.LoomBlock ||
                block instanceof net.minecraft.world.level.block.StonecutterBlock ||
                block instanceof net.minecraft.world.level.block.GrindstoneBlock ||
                block instanceof net.minecraft.world.level.block.BarrelBlock ||
                block instanceof net.minecraft.world.level.block.ShulkerBoxBlock ||
                block instanceof net.minecraft.world.level.block.DispenserBlock ||
                block instanceof net.minecraft.world.level.block.DropperBlock ||
                block instanceof net.minecraft.world.level.block.HopperBlock ||
                block instanceof net.minecraft.world.level.block.NoteBlock ||
                block instanceof net.minecraft.world.level.block.BedBlock ||
                block instanceof net.minecraft.world.level.block.BellBlock ||
                block == net.minecraft.world.level.block.Blocks.ENCHANTING_TABLE;
    }
}