package com.mattdahepic.autooredictconv.common;

import com.mattdahepic.autooredictconv.common.block.ConverterBlock;
import com.mattdahepic.autooredictconv.common.block.ConverterTile;
import com.mattdahepic.autooredictconv.common.command.CommandODC;
import com.mattdahepic.autooredictconv.common.config.ConversionsConfig;
import com.mattdahepic.autooredictconv.common.config.OptionsConfig;
import com.mattdahepic.autooredictconv.common.convert.Conversions;
import com.mattdahepic.autooredictconv.common.keypress.KeyHandler;
import com.mattdahepic.autooredictconv.common.keypress.PacketHandler;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.registries.MissingMappingsEvent.Mapping;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegisterEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Paths;
import java.util.ArrayList;

@Mod("autooredictconv")
public class AutoOreDictConv {
	public static final String MODID = "autooredictconv";
	public static final Logger LOGGER = LogManager.getLogger();
	/*
	 * public static final DeferredRegister<Block> BLOCKS =
	 * DeferredRegister.create(ForgeRegistries.BLOCKS, MODID); public static final
	 * DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
	 * MODID); public static final DeferredRegister<BlockEntityType<?>>
	 * BLOCK_ENTITY_TYPES =
	 * DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
	 */
	public static final RegistryObject<Block> CONVERTER_BLOCK = RegistryObject
			.create(new ResourceLocation(MODID, "converter"), ForgeRegistries.BLOCKS);
	public static final RegistryObject<Item> CONVERTER_ITEM = RegistryObject
			.create(new ResourceLocation(MODID, "converter"), ForgeRegistries.ITEMS);
	public static final RegistryObject<BlockEntityType<?>> CONVERTER_BLOCK_ENTITY = RegistryObject
			.create(new ResourceLocation(MODID, "converter"), ForgeRegistries.BLOCK_ENTITY_TYPES);

	public static final ArrayList<String> PAUSED_PLAYERS = new ArrayList<String>();

	public AutoOreDictConv() {
		loadConfig();
	}

	public void loadConfig() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, OptionsConfig.COMMON_SPEC);
		ConversionsConfig.file = Paths.get(FMLPaths.CONFIGDIR.get().toString(), "autooredictconv-conversions.cfg")
				.toFile();
	}

	@Mod.EventBusSubscriber(modid = AutoOreDictConv.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public class ClientModEvents {
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
	public class CommonModEvents {
		@SubscribeEvent
		public static void commonSetup(final FMLCommonSetupEvent event) {
			PacketHandler.initPackets();
			// CommandRegistry.registerCommand(CommandODC::register);
			ConversionsConfig.load();
			LOGGER.info("Ready to convert with " + Conversions.tagConversionMap.keySet().size()
					+ " entries in the config.");
		}

		@SubscribeEvent
		public static void register(final RegisterEvent event) {
			event.register(ForgeRegistries.Keys.BLOCKS, (helper) -> {
				helper.register(new ResourceLocation(MODID, "converter"), new ConverterBlock());
			});
			event.register(ForgeRegistries.Keys.ITEMS, (helper) -> {
				helper.register(new ResourceLocation(MODID, "converter"),
						new BlockItem(CONVERTER_BLOCK.get(), new Item.Properties().rarity(Rarity.COMMON)));
			});
			event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, (helper) -> {
				helper.register(new ResourceLocation(MODID, "converter"), ConverterTile.TYPE);
			});
		}
	}

	@Mod.EventBusSubscriber(modid = AutoOreDictConv.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
	public class ClientForgeEvents {
		@SubscribeEvent
		public static void onTooltip(final ItemTooltipEvent event) {
			if (event.getItemStack().getItem() == ForgeRegistries.ITEMS
					.getValue(new ResourceLocation(MODID, ConverterBlock.NAME))) {
				for (int i = 0; i < 3; i++) {
					event.getToolTip().add(Component.translatable("tooltip.autooredictconv.converter." + i));
				}
			}
		}
	}

	@Mod.EventBusSubscriber(modid = AutoOreDictConv.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public class CommonForgeEvents {
		@SubscribeEvent
		public static void registerCommands(final RegisterCommandsEvent event) {
			CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

			CommandODC.register(dispatcher);
		}

		@SubscribeEvent
		public static void onTick(final TickEvent.ServerTickEvent event) {
			if (!OptionsConfig.COMMON.enableKeypress.get()) {
				for (Player p : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
					if (PAUSED_PLAYERS.contains(p.getScoreboardName()))
						continue;
					Conversions.convert(p);
				}
			}
		}

		@SubscribeEvent
		public static void onMissing(final MissingMappingsEvent event) {
			event.getMappings(ForgeRegistries.Keys.ITEMS, MODID).stream()
					.filter(mapping -> mapping.getKey().getPath().contains("test")).forEach(Mapping::ignore);
		}
	}
}
