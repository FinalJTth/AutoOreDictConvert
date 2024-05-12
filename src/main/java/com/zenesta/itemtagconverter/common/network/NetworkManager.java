package com.zenesta.itemtagconverter.common.network;

import com.zenesta.itemtagconverter.common.ItemTagConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;

public class NetworkManager {
    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static final SimpleChannel INSTANCE = ChannelBuilder.named(new ResourceLocation(ItemTagConverter.MOD_ID, "main"))
            .clientAcceptedVersions((status, versions) -> true)
            .serverAcceptedVersions((status, versions) -> true)
            .networkProtocolVersion(1)
            .simpleChannel();

    public static void register() {
        INSTANCE.messageBuilder(ConvertMessage.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ConvertMessage::decode)
                .encoder(ConvertMessage::encode)
                .consumerMainThread(ConvertMessage::handle)
                .add();
    }
    public static void sendToServer(Object message) {
        INSTANCE.send(message, PacketDistributor.SERVER.noArg());
    }
    public static void sendToPlayer(Object message, ServerPlayer pe) {//
        INSTANCE.send(message, PacketDistributor.PLAYER.with(pe));
    }
    public static void sendToAllClients(Object message) {
        INSTANCE.send(message, PacketDistributor.ALL.noArg());
    }
}
