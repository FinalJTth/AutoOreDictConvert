package com.zenesta.itemtagconverter.client.event;

import com.mattdahepic.itemtagconverter.common.block.ConverterBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import static com.zenesta.itemtagconverter.ItemTagConverter.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvent {
    @SubscribeEvent
    public static void onTooltip(final ItemTooltipEvent event) {
        if (event.getItemStack().getItem() == ForgeRegistries.ITEMS.getValue(new ResourceLocation(MODID, ConverterBlock.NAME))) {
            for (int i = 0; i < 3; i++) {
                event.getToolTip().add(Component.translatable("tooltip.itemtagconverter.converter." + i));
            }
        }
    }
}