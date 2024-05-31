package com.zenesta.itemtagconverter.common;

import com.mattdahepic.itemtagconverter.common.config.ConversionsConfig;
import com.zenesta.itemtagconverter.config.OptionsConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.ArrayList;

@Mod(ItemTagConverter.MOD_ID)
public class ItemTagConverter {
    public static final String MOD_ID = "itemtagconverter";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final RegistryObject<Block> CONVERTER_BLOCK = RegistryObject.create(new ResourceLocation(MOD_ID, "converter"), ForgeRegistries.BLOCKS);
    public static final RegistryObject<Item> CONVERTER_ITEM = RegistryObject.create(new ResourceLocation(MOD_ID, "converter"), ForgeRegistries.ITEMS);
    public static final RegistryObject<BlockEntityType<?>> CONVERTER_BLOCK_ENTITY = RegistryObject.create(new ResourceLocation(MOD_ID, "converter"), ForgeRegistries.BLOCK_ENTITY_TYPES);

    public static final ArrayList<String> PAUSED_PLAYERS = new ArrayList<String>();

    public ItemTagConverter() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, OptionsConfig.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, OptionsConfig.COMMON_SPEC);
        ConversionsConfig.file = Paths.get(FMLPaths.CONFIGDIR.get().toString(), MOD_ID + "-conversions.cfg").toFile();
    }
}
