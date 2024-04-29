package com.mattdahepic.autooredictconv.common.command;

import com.mattdahepic.autooredictconv.common.AutoOreDictConv;
import com.mattdahepic.autooredictconv.common.config.ConversionsConfig;
import com.mattdahepic.autooredictconv.common.convert.Conversions;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.IReverseTag;
import net.minecraftforge.registries.tags.ITag;

public class CommandODC {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("odc").requires(s -> s.hasPermission(4))
				.executes((CommandContext<CommandSourceStack> ctx) -> {
					throw new SimpleCommandExceptionType(
							Component.translatable("autooredictconv.command.missingarguments")).create();
				}).then(Commands.literal("help").executes(CommandODC::help))
				.then(Commands.literal("detect").executes(CommandODC::detect))
				.then(Commands.literal("dump").executes(CommandODC::dump))
				.then(Commands.literal("find").executes((CommandContext<CommandSourceStack> ctx) -> {
					throw new SimpleCommandExceptionType(
							Component.translatable("autooredictconv.command.missingarguments")).create();
				}).then(Commands.argument("tag", ResourceLocationArgument.id())
						.suggests((ctx,
								bld) -> SharedSuggestionProvider.suggest(ForgeRegistries.ITEMS.tags().getTagNames()
										.map(TagKey::location).map(ResourceLocation::toString), bld))
						.executes(CommandODC::find)))
				.then(Commands.literal("list").executes(CommandODC::list))
				.then(Commands.literal("add").executes(CommandODC::add))
				.then(Commands.literal("set").executes((CommandContext<CommandSourceStack> ctx) -> {
					throw new SimpleCommandExceptionType(
							Component.translatable("autooredictconv.command.missingarguments")).create();
				}).then(Commands.argument("tag", ResourceLocationArgument.id())
						.suggests((ctx,
								bld) -> SharedSuggestionProvider.suggest(ForgeRegistries.ITEMS.tags().getTagNames()
										.map(TagKey::location).map(ResourceLocation::toString), bld))
						.executes(CommandODC::set)))
				.then(Commands.literal("reload").executes(CommandODC::reload))
				.then(Commands.literal("remove").executes(CommandODC::removeHand).then(Commands
						.argument("tag", ResourceLocationArgument.id())
						.suggests((ctx, bld) -> SharedSuggestionProvider.suggest(
								Conversions.tagConversionMap.keySet().stream().map(ResourceLocation::toString), bld))
						.executes(CommandODC::removeTag)))
				.then(Commands.literal("pause").requires(s -> s.hasPermission(0)).executes(CommandODC::pause));
		LiteralCommandNode<CommandSourceStack> command = dispatcher.register(builder);
	}

	public static int help(CommandContext<CommandSourceStack> ctx) {
		for (int i = 0; i < 9; i++) {
			String translatable = "autooredictconv.command.odc.help." + i;
			ctx.getSource().sendSuccess(() -> Component.translatable(translatable), false);
		}
		return Command.SINGLE_SUCCESS;
	}

	public static int detect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ItemStack item = ctx.getSource().getPlayerOrException().getItemInHand(InteractionHand.MAIN_HAND);
		ctx.getSource().sendSuccess(() -> Component.translatable("autooredictconv.command.odc.detect",
				ForgeRegistries.ITEMS.getKey(item.getItem())), true);
		ForgeRegistries.ITEMS.tags().getReverseTag(item.getItem()).stream()
				.forEach(rt -> rt.getTagKeys()
						.forEach(tk -> ctx.getSource().sendSuccess(
								() -> Component.translatable("autooredictconv.command.odc._each", tk.location()),
								true)));
		return Command.SINGLE_SUCCESS;
	}

	public static int dump(CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSuccess(() -> Component.translatable("autooredictconv.command.odc.dump"), true);
		ForgeRegistries.ITEMS.tags().getTagNames()
				.forEach(r -> ctx.getSource().sendSuccess(() -> Component.literal(r.location().toString()), true));
		return Command.SINGLE_SUCCESS;
	}

	public static int find(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ResourceLocation loc = ResourceLocationArgument.getId(ctx, "tag");
		ITag<Item> tag = ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(loc));
		if (tag.isEmpty())
			throw new SimpleCommandExceptionType(Component.translatable("autooredictconv.command.odc._no_tag", loc))
					.create();
		ctx.getSource().sendSuccess(() -> Component.translatable("autooredictconv.command.odc.find", loc), true);
		tag.stream().forEach(i -> {
			ctx.getSource().sendSuccess(
					() -> Component.translatable("autooredictconv.command.odc._each", ForgeRegistries.ITEMS.getKey(i)),
					true);
		});
		return Command.SINGLE_SUCCESS;
	}

	public static int list(CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSuccess(() -> Component.translatable("autooredictconv.command.odc.list"), false);
		if (!Conversions.tagConversionMap.isEmpty()) {
			ctx.getSource().sendSuccess(() -> Component.translatable("autooredictconv.command.odc.list.tag"), true);
			Conversions.tagConversionMap.forEach((tag, item) -> {
				ctx.getSource().sendSuccess(() -> Component.translatable("autooredictconv.command.odc._each_pair", tag,
						ForgeRegistries.ITEMS.getKey(item)), true);
			});
		}
		if (!Conversions.itemConversionMap.isEmpty()) {
			ctx.getSource().sendSuccess(() -> Component.translatable("autooredictconv.command.odc.list.item"), true);
			Conversions.itemConversionMap.forEach((in, out) -> {
				ctx.getSource().sendSuccess(() -> Component.translatable("autooredictconv.command.odc._each_pair",
						ForgeRegistries.ITEMS.getKey(in), ForgeRegistries.ITEMS.getKey(out)), true);
			});
		}
		return Command.SINGLE_SUCCESS;
	}

	public static int add(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ItemStack held = ctx.getSource().getPlayerOrException().getItemInHand(InteractionHand.MAIN_HAND);
		if (held.getItem() == Items.AIR)
			throw new SimpleCommandExceptionType(Component.translatable("autooredictconv.command.odc._must_be_holding"))
					.create();
		int addedTo = 0;
		for (IReverseTag<Item> reverseTag : ForgeRegistries.ITEMS.tags().getReverseTag(held.getItem()).stream()
				.toList()) {
			for (TagKey<Item> key : reverseTag.getTagKeys().toList()) {
				ResourceLocation tag = key.location();
				if (Conversions.tagBlacklist.contains(tag.toString()))
					continue; // ignore tags that everything has in addition to their actual entries.
				Conversions.tagConversionMap.put(tag, held.getItem());
				addedTo++;
				ctx.getSource().sendSuccess(() -> Component.translatable("autooredictconv.command.odc.add",
						ForgeRegistries.ITEMS.getKey(held.getItem()), tag), true);
			}
		}
		if (addedTo == 0)
			ctx.getSource().sendSuccess(() -> Component.translatable("autooredictconv.command.odc.add.none"), true);
		ConversionsConfig.save();
		return Command.SINGLE_SUCCESS;
	}

	public static int set(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ResourceLocation loc = ResourceLocationArgument.getId(ctx, "tag");
		ITag<Item> tag = ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(loc));
		if (tag.isEmpty())
			throw new SimpleCommandExceptionType(Component.translatable("autooredictconv.command.odc._no_tag", loc))
					.create();
		ItemStack held = ctx.getSource().getPlayerOrException().getItemInHand(InteractionHand.MAIN_HAND);
		if (held.getItem() == Items.AIR)
			throw new SimpleCommandExceptionType(Component.translatable("autooredictconv.command.odc._must_be_holding"))
					.create();
		Conversions.tagConversionMap.put(loc, held.getItem());
		ctx.getSource().sendSuccess(() -> Component.translatable("autooredictconv.command.odc.add",
				ForgeRegistries.ITEMS.getKey(held.getItem()), loc), true);
		return Command.SINGLE_SUCCESS;
	}

	public static int reload(CommandContext<CommandSourceStack> ctx) {
		ConversionsConfig.reload();
		ctx.getSource().sendSuccess(() -> Component.translatable("autooredictconv.command.odc.reload"), false);
		return Command.SINGLE_SUCCESS;
	}

	public static int removeHand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ItemStack held = ctx.getSource().getPlayerOrException().getItemInHand(InteractionHand.MAIN_HAND);
		if (held.getItem() == Items.AIR)
			throw new SimpleCommandExceptionType(Component.translatable("autooredictconv.command.odc._must_be_holding"))
					.create();
		for (IReverseTag<Item> reverseTag : ForgeRegistries.ITEMS.tags().getReverseTag(held.getItem()).stream()
				.toList()) {
			for (TagKey<Item> key : reverseTag.getTagKeys().toList()) {
				ResourceLocation tag = key.location();
				remove(tag, ctx);
			}
		}
		ConversionsConfig.save();
		return Command.SINGLE_SUCCESS;
	}

	public static int removeTag(CommandContext<CommandSourceStack> ctx) {
		ResourceLocation tag = ResourceLocationArgument.getId(ctx, "tag");
		remove(tag, ctx);
		ConversionsConfig.save();
		return Command.SINGLE_SUCCESS;
	}

	private static void remove(ResourceLocation tag, CommandContext<CommandSourceStack> ctx) {
		Conversions.tagConversionMap.remove(tag);
		ctx.getSource().sendSuccess(() -> Component.translatable("autooredictconv.command.odc.remove", tag), true);
	}

	public static int pause(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		try {
			String name = ctx.getSource().getPlayerOrException().getScoreboardName();
			if (AutoOreDictConv.PAUSED_PLAYERS.contains(name)) {
				AutoOreDictConv.PAUSED_PLAYERS.remove(name);
				ctx.getSource().sendSuccess(() -> Component.translatable("autooredictconv.command.odc.pause.unpause"),
						false);
			} else {
				AutoOreDictConv.PAUSED_PLAYERS.add(name);
				ctx.getSource().sendSuccess(() -> Component.translatable("autooredictconv.command.odc.pause.pause"),
						false);
			}
			return Command.SINGLE_SUCCESS;
		} catch (Exception ex) {
			throw new SimpleCommandExceptionType(Component.translatable("autooredictconv.command.odc.pause.invalid"))
					.create();
		}
	}
}
