package com.zenesta.itemtagconverter.common.network;

import com.zenesta.itemtagconverter.common.convert.Converter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ConvertMessage {
    public static ConvertMessage decode(FriendlyByteBuf buffer) {
        return new ConvertMessage();
    }

    public void encode(FriendlyByteBuf buffer) {
        // NO-OP
    }

    public static void handle(ConvertMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
            Converter.convertInPlayer(player);
            player.displayClientMessage(Component.translatable("itemtagconverter.converting"), true);
        });
        ctx.get().setPacketHandled(true);
    }
}
