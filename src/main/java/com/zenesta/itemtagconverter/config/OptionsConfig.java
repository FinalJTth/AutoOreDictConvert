package com.zenesta.itemtagconverter.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class OptionsConfig {
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        CLIENT_SPEC = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();
        COMMON_SPEC = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();
    }

    public static class Client {
        public final ForgeConfigSpec.BooleanValue enableKeypress;

        public Client(ForgeConfigSpec.Builder builder) {
            enableKeypress = builder
                    .comment("If true, conversions will only occur upon pressing the defined key (defaults to end).")
                    .define("enableKeypress", false);
        }
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
