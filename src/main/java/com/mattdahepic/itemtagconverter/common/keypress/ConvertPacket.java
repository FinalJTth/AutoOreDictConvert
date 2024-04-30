package com.mattdahepic.itemtagconverter.common.keypress;

import com.zenesta.itemtagconverter.common.convert.Converter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ConvertPacket {
    public static ConvertPacket decode(FriendlyByteBuf buffer) {
        return new ConvertPacket();
    }

    public static void handle(ConvertPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Converter.convertInPlayer(Objects.requireNonNull(ctx.get().getSender()));
            ctx.get().getSender().displayClientMessage(Component.translatable("itemtagconverter.converting"), true);
        });
        ctx.get().setPacketHandled(true);
    }

    public void encode(FriendlyByteBuf buffer) {
        // NO-OP
    }
}
