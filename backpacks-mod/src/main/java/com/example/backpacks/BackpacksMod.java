package com.example.backpacks;

import com.example.backpacks.item.BackpackItem;
import com.example.backpacks.capability.BackpackCapabilityProvider;
import com.example.backpacks.config.BackpackConfig;
import com.example.backpacks.network.BackpackNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

@Mod("backpacks")
public class BackpacksMod {
    public static final String MOD_ID = "backpacks";
    
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    
    public static final DeferredRegister<Item> BACKPACK = ITEMS;
    public static final DeferredRegister.ItemHolder<BackpackItem> BACKPACK_ITEM = BACKPACK.registerItem(
        "backpack", 
        BackpackItem::new
    );
    
    public BackpacksMod(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        
        modEventBus.addListener(this::registerCapabilities);
        
        BackpackConfig.init();
        BackpackNetwork.init();
        
        modContainer.registerExtensionPoint(net.neoforged.fml.ModLoadingContext.RegisteringClass.class, 
            () -> new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
                @Override
                public boolean canPerformAction(net.minecraft.world.item.ItemStack stack, net.neoforged.neoforge.common.ToolAction toolAction) {
                    return false;
                }
            });
    }
    
    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItemHandlerCapability(
            BackpackCapabilityProvider.BACKPACK_HANDLER,
            null,
            (stack, context) -> new BackpackCapabilityProvider(stack),
            BACKPACK_ITEM.get()
        );
    }
}
