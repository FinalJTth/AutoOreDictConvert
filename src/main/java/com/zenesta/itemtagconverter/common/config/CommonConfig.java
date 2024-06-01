package com.zenesta.itemtagconverter.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class CommonConfig {
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue enableBlockDropsConversion;

        public Common(ForgeConfigSpec.Builder builder) {
            enableBlockDropsConversion = builder
                    .comment("If true, then any items that drop from block breaking will be converted.")
                    .define("enableBlockDropsConversion", true);
        }
    }
}
