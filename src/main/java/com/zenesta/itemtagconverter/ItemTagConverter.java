package com.zenesta.itemtagconverter;

import com.mattdahepic.itemtagconverter.common.config.ConversionsConfig;
import com.zenesta.itemtagconverter.client.config.ClientConfig;
import com.zenesta.itemtagconverter.common.config.CommonConfig;
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

@Mod(ItemTagConverter.MODID)
public class ItemTagConverter {
    public static final String MODID = "itemtagconverter";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final RegistryObject<Block> CONVERTER_BLOCK = RegistryObject.create(new ResourceLocation(MODID, "converter"), ForgeRegistries.BLOCKS);
    public static final RegistryObject<Item> CONVERTER_ITEM = RegistryObject.create(new ResourceLocation(MODID, "converter"), ForgeRegistries.ITEMS);
    public static final RegistryObject<BlockEntityType<?>> CONVERTER_BLOCK_ENTITY = RegistryObject.create(new ResourceLocation(MODID, "converter"), ForgeRegistries.BLOCK_ENTITY_TYPES);

    public static final ArrayList<String> PAUSED_PLAYERS = new ArrayList<String>();

    public ItemTagConverter() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.COMMON_SPEC);
        ConversionsConfig.file = Paths.get(FMLPaths.CONFIGDIR.get().toString(), MODID + "-conversions.cfg").toFile();
    }
}

