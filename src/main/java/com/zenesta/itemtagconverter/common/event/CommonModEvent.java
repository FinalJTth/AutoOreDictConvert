package com.zenesta.itemtagconverter.common.event;

import com.mattdahepic.itemtagconverter.common.block.ConverterBlock;
import com.mattdahepic.itemtagconverter.common.block.ConverterTile;
import com.mattdahepic.itemtagconverter.common.config.ConversionsConfig;
import com.zenesta.itemtagconverter.ItemTagConverter;
import com.zenesta.itemtagconverter.common.convert.Converter;
import com.zenesta.itemtagconverter.common.network.NetworkManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import static com.zenesta.itemtagconverter.ItemTagConverter.LOGGER;
import static com.zenesta.itemtagconverter.ItemTagConverter.MODID;
import static com.zenesta.itemtagconverter.ItemTagConverter.CONVERTER_BLOCK;

@Mod.EventBusSubscriber(modid = ItemTagConverter.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModEvent {
    @SubscribeEvent
    public static void commonSetup(final FMLCommonSetupEvent event) {
        NetworkManager.register();
        ConversionsConfig.load();
        LOGGER.info("Ready to convert with {} entries in the config.", Converter.TAG_CONVERSION_MAP.keySet().size());
    }

    @SubscribeEvent
    public static void register(final RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCKS, (helper) -> helper.register(new ResourceLocation(MODID, "converter"), new ConverterBlock()));
        event.register(ForgeRegistries.Keys.ITEMS, (helper) -> helper.register(new ResourceLocation(MODID, "converter"), new BlockItem(CONVERTER_BLOCK.get(), new Item.Properties().rarity(Rarity.COMMON))));
        event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, (helper) -> helper.register(new ResourceLocation(MODID, "converter"), ConverterTile.TYPE));
    }
}