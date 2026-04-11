package com.example.backpacks.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class BackpackConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    
    public static final ModConfigSpec.IntValue SLOWDOWN_START_SLOT;
    public static final ModConfigSpec.DoubleValue MAX_SLOWDOWN_PERCENT;
    public static final ModConfigSpec.DoubleValue UNDERWATER_SLOWDOWN_PERCENT;
    
    static {
        BUILDER.push("backpack_settings");
        
        SLOWDOWN_START_SLOT = BUILDER
            .comment("Слот, начиная с которого начинается замедление (1-9)")
            .defineInRange("slowdownStartSlot", 5, 1, 9);
        
        MAX_SLOWDOWN_PERCENT = BUILDER
            .comment("Максимальное замедление в процентах (0.0-1.0)")
            .defineInRange("maxSlowdownPercent", 0.3, 0.0, 1.0);
        
        UNDERWATER_SLOWDOWN_PERCENT = BUILDER
            .comment("Замедление под водой в процентах (0.0-1.0)")
            .defineInRange("underwaterSlowdownPercent", 0.15, 0.0, 1.0);
        
        BUILDER.pop();
        
        SPEC = BUILDER.build();
    }
    
    public static void init() {
        // Инициализация конфигурации
    }
}
