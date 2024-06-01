package com.zenesta.itemtagconverter.client.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class ClientConfig {
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;
    static {
        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();
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
