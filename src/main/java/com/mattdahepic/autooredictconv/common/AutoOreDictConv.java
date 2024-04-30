package com.mattdahepic.autooredictconv.common;

import com.mattdahepic.autooredictconv.common.block.ConverterBlock;
import com.mattdahepic.autooredictconv.common.block.ConverterTile;
import com.mattdahepic.autooredictconv.common.command.CommandODC;
import com.mattdahepic.autooredictconv.common.config.ConversionsConfig;
import com.mattdahepic.autooredictconv.common.config.OptionsConfig;
import com.mattdahepic.autooredictconv.common.convert.Converter;
import com.mattdahepic.autooredictconv.common.keypress.KeyHandler;
import com.mattdahepic.autooredictconv.common.keypress.PacketHandler;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.*;
import net.minecraftforge.registries.MissingMappingsEvent.Mapping;
import net.minecraftforge.registries.tags.ITagManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

@Mod("autooredictconv")
public class AutoOreDictConv {
    public static final String MODID = "autooredictconv";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final IForgeRegistry<Item> ITEMS_REGISTRIES = ForgeRegistries.ITEMS;
    public static final ITagManager<Item> ITEMS_TAG_MANAGER = Objects.requireNonNull(ForgeRegistries.ITEMS.tags());
    public static final IForgeRegistry<Block> BLOCKS_REGISTRIES = ForgeRegistries.BLOCKS;
    public static final ITagManager<Block> BLOCKS_TAG_MANAGER = Objects.requireNonNull(ForgeRegistries.BLOCKS.tags());
    public static final IForgeRegistry<BlockEntityType<?>> BLOCK_ENTITIES_REGISTRIES = ForgeRegistries.BLOCK_ENTITY_TYPES;
    public static final ITagManager<BlockEntityType<?>> BLOCK_ENTITIES_TAG_MANAGER = Objects.requireNonNull(ForgeRegistries.BLOCK_ENTITY_TYPES.tags());

    public static final RegistryObject<Block> CONVERTER_BLOCK = RegistryObject.create(new ResourceLocation(MODID, "converter"), ForgeRegistries.BLOCKS);
    public static final RegistryObject<Item> CONVERTER_ITEM = RegistryObject.create(new ResourceLocation(MODID, "converter"), ForgeRegistries.ITEMS);
    public static final RegistryObject<BlockEntityType<?>> CONVERTER_BLOCK_ENTITY = RegistryObject.create(new ResourceLocation(MODID, "converter"), ForgeRegistries.BLOCK_ENTITY_TYPES);

    public static final ArrayList<String> PAUSED_PLAYERS = new ArrayList<String>();

    public AutoOreDictConv() {
        loadConfig();
    }

    public void loadConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, OptionsConfig.COMMON_SPEC);
        ConversionsConfig.file = Paths.get(FMLPaths.CONFIGDIR.get().toString(), MODID + "-conversions.cfg").toFile();
    }

    @Mod.EventBusSubscriber(modid = AutoOreDictConv.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void clientSetup(final FMLClientSetupEvent event) {
            if (OptionsConfig.COMMON.enableKeypress.get()) {
                FMLJavaModLoadingContext.get().getModEventBus().addListener(KeyHandler::register);
                MinecraftForge.EVENT_BUS.addListener(KeyHandler::onKeyInput);
            }
        }

        @SubscribeEvent
        public static void buildCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
                event.accept(AutoOreDictConv.CONVERTER_ITEM);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = AutoOreDictConv.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class CommonModEvents {
        @SubscribeEvent
        public static void commonSetup(final FMLCommonSetupEvent event) {
            PacketHandler.initPackets();
            // CommandRegistry.registerCommand(CommandODC::register);
            ConversionsConfig.load();
            LOGGER.info("Ready to convert with " + Converter.TAG_CONVERSION_MAP.keySet().size() + " entries in the config.");
        }

        @SubscribeEvent
        public static void register(final RegisterEvent event) {
            event.register(ForgeRegistries.Keys.BLOCKS, (helper) -> {
                helper.register(new ResourceLocation(MODID, "converter"), new ConverterBlock());
            });
            event.register(ForgeRegistries.Keys.ITEMS, (helper) -> {
                helper.register(new ResourceLocation(MODID, "converter"), new BlockItem(CONVERTER_BLOCK.get(), new Item.Properties().rarity(Rarity.COMMON)));
            });
            event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, (helper) -> {
                helper.register(new ResourceLocation(MODID, "converter"), ConverterTile.TYPE);
            });
        }
    }

    @Mod.EventBusSubscriber(modid = AutoOreDictConv.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onTooltip(final ItemTooltipEvent event) {
            if (event.getItemStack().getItem() == ForgeRegistries.ITEMS.getValue(new ResourceLocation(MODID, ConverterBlock.NAME))) {
                for (int i = 0; i < 3; i++) {
                    event.getToolTip().add(Component.translatable("tooltip.autooredictconv.converter." + i));
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = AutoOreDictConv.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class CommonForgeEvents {
        @SubscribeEvent
        public static void registerCommands(final RegisterCommandsEvent event) {
            CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

            CommandODC.register(dispatcher);
        }

        @SubscribeEvent
        public static void onItemPickup(final EntityItemPickupEvent event)
        {
            if (PAUSED_PLAYERS.contains(event.getEntity().getScoreboardName()))
                return;
            ItemEntity itemEntity = event.getItem();
            ItemStack matchedItemStack = Converter.getMatchedConvertedItemStack(itemEntity.getItem());
            if (matchedItemStack != null && event.isCancelable() ) {
                event.setCanceled(true);
                itemEntity.setItem(matchedItemStack);
                event.getEntity().onItemPickup(itemEntity);
            }
        }

        @SubscribeEvent
        public static void onMissing(final MissingMappingsEvent event) {
            event.getMappings(ForgeRegistries.Keys.ITEMS, MODID).stream().filter(mapping -> mapping.getKey().getPath().contains("test")).forEach(Mapping::ignore);
        }
    }
}
