package com.mattdahepic.itemtagconverter.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class OptionsConfig {
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static class Client {
        public final ForgeConfigSpec.BooleanValue enableKeypress;

        public Client(ForgeConfigSpec.Builder builder) {
            enableKeypress = builder
                    .comment("If true, conversions will only occur upon pressing the defined key (defaults to end).")
                    .define("enableKeypress", false);
        }
    }
}
