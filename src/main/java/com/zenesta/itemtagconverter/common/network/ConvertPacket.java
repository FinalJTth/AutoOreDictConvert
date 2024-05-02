package com.zenesta.itemtagconverter.common.network;

import com.zenesta.itemtagconverter.common.convert.Converter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ConvertPacket {
    public static ConvertPacket decode(FriendlyByteBuf buffer) {
        return new ConvertPacket();
    }

    public void encode(FriendlyByteBuf buffer) {
        // NO-OP
    }

    public static void handle(ConvertPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
            Converter.convertInPlayer(player);
            player.displayClientMessage(Component.translatable("itemtagconverter.converting"), true);
        });
        ctx.get().setPacketHandled(true);
    }
}
