package com.zenesta.itemtagconverter.common.network;

import com.zenesta.itemtagconverter.common.convert.Converter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.Objects;

public class ConvertMessage {
    public static ConvertMessage decode(FriendlyByteBuf buffer) {
        return new ConvertMessage();
    }

    public void encode(FriendlyByteBuf buffer) { }

    public void handle(CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = Objects.requireNonNull(ctx.getSender());
            Converter.convertInPlayer(player);
            player.displayClientMessage(Component.translatable("itemtagconverter.converting"), true);
        });
        ctx.setPacketHandled(true);
    }
}
