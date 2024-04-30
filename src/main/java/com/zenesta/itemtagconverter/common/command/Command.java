package com.zenesta.itemtagconverter.common.command;

import com.mattdahepic.itemtagconverter.common.config.ConversionsConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.zenesta.itemtagconverter.common.ItemTagConverter;
import com.zenesta.itemtagconverter.common.convert.Converter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.tags.IReverseTag;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Objects;

import static com.zenesta.itemtagconverter.common.ItemTagConverter.ITEMS_REGISTRIES;
import static com.zenesta.itemtagconverter.common.ItemTagConverter.ITEMS_TAG_MANAGER;

public class Command {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("tagconverter").requires(s -> s.hasPermission(4))
                .executes((CommandContext<CommandSourceStack> ctx) -> {
                    throw new SimpleCommandExceptionType(
                            Component.translatable("itemtagconverter.command.missingarguments")).create();
                }).then(Commands.literal("help").executes(Command::help))
                .then(Commands.literal("detect").executes(Command::detect))
                .then(Commands.literal("dump").executes(Command::dump))
                .then(Commands.literal("find").executes((CommandContext<CommandSourceStack> ctx) -> {
                    throw new SimpleCommandExceptionType(
                            Component.translatable("itemtagconverter.command.missingarguments")).create();
                }).then(Commands.argument("tag", ResourceLocationArgument.id())
                        .suggests((ctx, bld) -> SharedSuggestionProvider.suggest(ITEMS_TAG_MANAGER.getTagNames()
                                .map(TagKey::location).map(ResourceLocation::toString), bld))
                        .executes(Command::find)))
                .then(Commands.literal("list").executes(Command::list))
                .then(Commands.literal("add")
                        .executes(Command::add))
                .then(Commands.literal("set").executes((CommandContext<CommandSourceStack> ctx) -> {
                    throw new SimpleCommandExceptionType(
                            Component.translatable("itemtagconverter.command.missingarguments")).create();
                }).then(Commands.argument("tag", ResourceLocationArgument.id())
                        .suggests((ctx,
                                   bld) -> SharedSuggestionProvider.suggest(ITEMS_TAG_MANAGER.getTagNames()
                                .map(TagKey::location).map(ResourceLocation::toString), bld))
                        .executes(Command::set)
                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                .suggests((ctx, bld) -> SharedSuggestionProvider.suggest(ITEMS_REGISTRIES.getValues()
                                        .stream().map(item -> Objects.requireNonNull(ITEMS_REGISTRIES.getKey(item)).toString()), bld))
                                .executes(Command::setItem))))
                .then(Commands.literal("reload").executes(Command::reload))
                .then(Commands.literal("remove").executes(Command::removeHand).then(Commands
                        .argument("tag", ResourceLocationArgument.id())
                        .suggests((ctx, bld) -> SharedSuggestionProvider.suggest(
                                Converter.TAG_CONVERSION_MAP.keySet().stream().map(ResourceLocation::toString), bld))
                        .executes(Command::removeTag)))
                .then(Commands.literal("pause").requires(s -> s.hasPermission(0)).executes(Command::pause))
                .then(Commands.literal("convert").requires(s -> s.hasPermission(0)).executes(Command::convert)
                        .then(Commands.argument("type", StringArgumentType.string()).requires(s -> s.hasPermission(4))
                                .suggests((ctx, bld) -> SharedSuggestionProvider.suggest(List.of("all"), bld))
                                .executes(Command::convertWithType)));

        dispatcher.register(builder);
    }

    public static int help(CommandContext<CommandSourceStack> ctx) {
        for (int i = 0; i < 10; i++) {
            String translatable = "itemtagconverter.command.tagconverter.help." + i;
            ctx.getSource().sendSuccess(() -> Component.translatable(translatable), false);
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int detect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ItemStack item = ctx.getSource().getPlayerOrException().getItemInHand(InteractionHand.MAIN_HAND);
        ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.detect",
                Objects.requireNonNull(ITEMS_REGISTRIES.getKey(item.getItem())).toString()), true);
        ITEMS_TAG_MANAGER.getReverseTag(item.getItem())
                .ifPresent(rt -> rt.getTagKeys()
                        .forEach(tk -> ctx.getSource().sendSuccess(
                                () -> Component.translatable("itemtagconverter.command.tagconverter._each", tk.location().toString()),
                                true)));
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int dump(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.dump"), true);
        ITEMS_TAG_MANAGER.getTagNames()
                .forEach(r -> ctx.getSource().sendSuccess(() -> Component.literal(r.location().toString()), true));
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int find(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ResourceLocation loc = ResourceLocationArgument.getId(ctx, "tag");
        ITag<Item> tag = ITEMS_TAG_MANAGER.getTag(ItemTags.create(loc));
        if (tag.isEmpty())
            throw new SimpleCommandExceptionType(Component.translatable("itemtagconverter.command.tagconverter._no_tag", loc.toString()))
                    .create();
        ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.find", loc.toString()), true);
        tag.stream().forEach(i -> {
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("itemtagconverter.command.tagconverter._each", Objects.requireNonNull(ITEMS_REGISTRIES.getKey(i)).toString()),
                    true);
        });
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int list(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.list"), false);
        if (!Converter.TAG_CONVERSION_MAP.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.list.tag"), true);
            Converter.TAG_CONVERSION_MAP.forEach((tag, item) -> {
                ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter._each_pair", tag.toString(),
                        Objects.requireNonNull(ITEMS_REGISTRIES.getKey(item)).toString()), true);
            });
        }
        if (!Converter.ITEM_CONVERSION_MAP.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.list.item"), true);
            Converter.ITEM_CONVERSION_MAP.forEach((in, out) -> {
                ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter._each_pair",
                        Objects.requireNonNull(ITEMS_REGISTRIES.getKey(in)).toString(), Objects.requireNonNull(ITEMS_REGISTRIES.getKey(out)).toString()), true);
            });
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int add(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ItemStack held = ctx.getSource().getPlayerOrException().getItemInHand(InteractionHand.MAIN_HAND);
        if (held.getItem() == Items.AIR)
            throw new SimpleCommandExceptionType(Component.translatable("itemtagconverter.command.tagconverter._must_be_holding"))
                    .create();
        int addedTo = 0;
        for (IReverseTag<Item> reverseTag : ITEMS_TAG_MANAGER.getReverseTag(held.getItem()).stream()
                .toList()) {
            for (TagKey<Item> key : reverseTag.getTagKeys().toList()) {
                ResourceLocation tag = key.location();
                if (Converter.TAG_BLACKLIST.contains(tag.toString()) || Converter.TAG_BLACKLIST.contains(tag.toString().split(":")[0]))
                    continue; // ignore tags that everything has in addition to their actual entries.
                Converter.TAG_CONVERSION_MAP.put(tag, held.getItem());
                addedTo++;
                ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.add",
                        Objects.requireNonNull(ITEMS_REGISTRIES.getKey(held.getItem())).toString(), tag.toString()), true);
            }
        }
        if (addedTo == 0)
            ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.add.none"), true);
        ConversionsConfig.save();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int set(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ResourceLocation loc = ResourceLocationArgument.getId(ctx, "tag");
        ITag<Item> tag = ITEMS_TAG_MANAGER.getTag(ItemTags.create(loc));
        if (tag.isEmpty())
            throw new SimpleCommandExceptionType(Component.translatable("itemtagconverter.command.tagconverter._no_tag", loc.toString())).create();
        ItemStack held = ctx.getSource().getPlayerOrException().getItemInHand(InteractionHand.MAIN_HAND);
        if (held.getItem() == Items.AIR)
            throw new SimpleCommandExceptionType(Component.translatable("itemtagconverter.command.tagconverter._must_be_holding")).create();
        Converter.TAG_CONVERSION_MAP.put(loc, held.getItem());
        ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.add",
                Objects.requireNonNull(ITEMS_REGISTRIES.getKey(held.getItem())).toString(), loc.toString()), true);
        ConversionsConfig.save();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int setItem(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ResourceLocation loc = ResourceLocationArgument.getId(ctx, "tag");
        ResourceLocation itemLoc = ResourceLocationArgument.getId(ctx, "item");
        ITag<Item> tag = ITEMS_TAG_MANAGER.getTag(ItemTags.create(loc));
        if (tag.isEmpty())
            throw new SimpleCommandExceptionType(Component.translatable("itemtagconverter.command.tagconverter._no_tag", loc.toString()))
                    .create();
        Item item = ITEMS_REGISTRIES.getValue(itemLoc);
        if (item == null || Objects.requireNonNull(ITEMS_REGISTRIES.getKey(item)).toString().equals("minecraft:air"))
            throw new SimpleCommandExceptionType(Component.translatable("itemtagconverter.command.tagconverter._no_item", itemLoc.toString()))
                    .create();
        Converter.TAG_CONVERSION_MAP.put(loc, item);
        ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.add",
                Objects.requireNonNull(ITEMS_REGISTRIES.getKey(item)).toString(), loc.toString()), true);
        ConversionsConfig.save();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int reload(CommandContext<CommandSourceStack> ctx) {
        ConversionsConfig.reload();
        ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.reload"), false);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int removeHand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ItemStack held = ctx.getSource().getPlayerOrException().getItemInHand(InteractionHand.MAIN_HAND);
        if (held.getItem() == Items.AIR)
            throw new SimpleCommandExceptionType(Component.translatable("itemtagconverter.command.tagconverter._must_be_holding"))
                    .create();
        for (IReverseTag<Item> reverseTag : ITEMS_TAG_MANAGER.getReverseTag(held.getItem()).stream()
                .toList()) {
            for (TagKey<Item> key : reverseTag.getTagKeys().toList()) {
                ResourceLocation tag = key.location();
                remove(tag, ctx);
            }
        }
        ConversionsConfig.save();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int removeTag(CommandContext<CommandSourceStack> ctx) {
        ResourceLocation tag = ResourceLocationArgument.getId(ctx, "tag");
        remove(tag, ctx);
        ConversionsConfig.save();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static void remove(ResourceLocation tag, CommandContext<CommandSourceStack> ctx) {
        Converter.TAG_CONVERSION_MAP.remove(tag);
        ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.remove", tag.toString()), true);
    }

    public static int pause(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        try {
            String name = ctx.getSource().getPlayerOrException().getScoreboardName();
            if (ItemTagConverter.PAUSED_PLAYERS.contains(name)) {
                ItemTagConverter.PAUSED_PLAYERS.remove(name);
                ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.pause.unpause"),
                        false);
            } else {
                ItemTagConverter.PAUSED_PLAYERS.add(name);
                ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.pause.pause"),
                        false);
            }
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            throw new SimpleCommandExceptionType(Component.translatable("itemtagconverter.command.tagconverter.pause.invalid"))
                    .create();
        }
    }

    public static int convert(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Converter.convertInPlayer(ctx.getSource().getPlayerOrException());
        ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.convert",
                "your inventory"), true);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int convertWithType(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String arg = StringArgumentType.getString(ctx, "type");
        if (!arg.equals("all"))
            throw new SimpleCommandExceptionType(Component.translatable("itemtagconverter.command.tagconverter._wrong_convert_type", arg)).create();
        for (ServerPlayer p : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            Converter.convertInPlayer(p);
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.convert",
                "all player's inventory"), true);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
