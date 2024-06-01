package com.zenesta.itemtagconverter.common.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkManager {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("itemtagconverter", "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int nextPacketId = 0;

    public static void register() {
        INSTANCE.registerMessage(nextPacketId, ConvertMessage.class, ConvertMessage::encode, ConvertMessage::decode, ConvertMessage::handle);
        ++nextPacketId;
    }

    public static void sendToServer(Object message) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), message);
    }
}
