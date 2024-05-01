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
import com.zenesta.itemtagconverter.common.registry.ItemRegistryManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

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
                        .suggests((ctx, bld) -> SharedSuggestionProvider.suggest(ItemRegistryManager.getAllItemTagResourceName(), bld))
                        .executes(Command::find)))
                .then(Commands.literal("list").executes(Command::list))
                .then(Commands.literal("add")
                        .executes(Command::add)
                .then(Commands.argument("tag_or_item", ResourceLocationArgument.id())
                        .suggests((ctx, bld) -> SharedSuggestionProvider.suggest(Stream.concat(ItemRegistryManager.getAllItemTagResourceName().stream(), ItemRegistryManager.getAllItemResourceName().stream()), bld))
                        .executes(Command::addToHand)
                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                .suggests((ctx, bld) -> SharedSuggestionProvider.suggest(ItemRegistryManager.getAllItemResourceName(), bld))
                                .executes(Command::addTo))))
                .then(Commands.literal("reload").executes(Command::reload))
                .then(Commands.literal("remove").executes(Command::removeHand).then(Commands
                        .argument("tag", ResourceLocationArgument.id())
                        .suggests((ctx, bld) -> SharedSuggestionProvider.suggest(
                                Stream.concat(Converter.TAG_CONVERSION_MAP.keySet().stream().map(ResourceLocation::toString), Converter.ITEM_CONVERSION_MAP.keySet().stream().map(ResourceLocation::toString)), bld))
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
                ItemRegistryManager.getItemResource(item.getItem()).toString()), true);

        ItemRegistryManager.getItemTagResources(item.getItem()).forEach(resource ->
                ctx.getSource().sendSuccess(() ->
                        Component.translatable("itemtagconverter.command.tagconverter._each", resource.toString()), true));

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int dump(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.dump"), true);

        ItemRegistryManager.getAllItemTagResourceName().forEach(resource ->
                ctx.getSource().sendSuccess(() -> Component.literal(resource), true));

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int find(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ResourceLocation loc = ResourceLocationArgument.getId(ctx, "tag");
        ITag<Item> tag = ItemRegistryManager.getItemTagFromResource(loc);

        if (tag.isEmpty())
            throw new SimpleCommandExceptionType(Component.translatable("itemtagconverter.command.tagconverter._no_tag", loc.toString()))
                    .create();

        ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.find", loc.toString()), true);
        tag.stream().forEach(i -> {
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("itemtagconverter.command.tagconverter._each", ItemRegistryManager.getItemResource(i).toString()),
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
                        item.toString()), true);
            });
        }

        if (!Converter.ITEM_CONVERSION_MAP.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.list.item"), true);
            Converter.ITEM_CONVERSION_MAP.forEach((in, out) -> {
                ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter._each_pair",
                        in.toString(), out.toString()), true);
            });
        }

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int add(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ItemStack held = ctx.getSource().getPlayerOrException().getItemInHand(InteractionHand.MAIN_HAND);
        if (held.getItem() == Items.AIR)
            throw new SimpleCommandExceptionType(Component.translatable("itemtagconverter.command.tagconverter._must_be_holding"))
                    .create();

        final AtomicReference<Boolean> hasAdded = new AtomicReference<Boolean>(false);
        ItemRegistryManager.getItemTagResources(held.getItem()).forEach(tag -> {
            if (Converter.TAG_BLACKLIST.contains(tag.toString()) || Converter.TAG_BLACKLIST.contains(tag.toString().split(":")[0]))
                return;
            hasAdded.set(true);
            Converter.TAG_CONVERSION_MAP.put(tag, ItemRegistryManager.getItemResource(held.getItem()));
            ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.add",
                    ItemRegistryManager.getItemResource(held.getItem()).toString(), tag.toString()), true);

        });

        if (!hasAdded.get())
            ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.add.none"), true);

        ConversionsConfig.save();

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int addToHand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ResourceLocation loc = ResourceLocationArgument.getId(ctx, "tag_or_item");
        ITag<Item> tag = ItemRegistryManager.getItemTagFromResource(loc);
        Item item = ItemRegistryManager.getItemFromResource(loc);

        if (tag.isEmpty() && item == null)
            throw new SimpleCommandExceptionType(Component.translatable("itemtagconverter.command.tagconverter._no_tag_or_item", loc.toString())).create();

        ItemStack held = ctx.getSource().getPlayerOrException().getItemInHand(InteractionHand.MAIN_HAND);
        if (held.getItem() == Items.AIR)
            throw new SimpleCommandExceptionType(Component.translatable("itemtagconverter.command.tagconverter._must_be_holding")).create();

        if (!tag.isEmpty())
            Converter.TAG_CONVERSION_MAP.put(loc, ItemRegistryManager.getItemResource(held.getItem()));
        else
            Converter.ITEM_CONVERSION_MAP.put(loc, ItemRegistryManager.getItemResource(held.getItem()));

        ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.add",
                ItemRegistryManager.getItemResource(held.getItem()).toString(), loc.toString()), true);

        ConversionsConfig.save();

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int addTo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ResourceLocation loc = ResourceLocationArgument.getId(ctx, "tag_or_item");
        ResourceLocation itemLoc = ResourceLocationArgument.getId(ctx, "item");
        ITag<Item> tag = ItemRegistryManager.getItemTagFromResource(loc);
        Item item1 = ItemRegistryManager.getItemFromResource(loc);

        if (tag.isEmpty() && item1 == null)
            throw new SimpleCommandExceptionType(Component.translatable("itemtagconverter.command.tagconverter._no_tag_or_item", loc.toString()))
                    .create();

        Item item2 = ItemRegistryManager.getItemFromResource(itemLoc);
        if (item2 == null || ItemRegistryManager.getItemResource(item2).toString().equals("minecraft:air"))
            throw new SimpleCommandExceptionType(Component.translatable("itemtagconverter.command.tagconverter._no_item", itemLoc.toString()))
                    .create();

        if (!tag.isEmpty())
            Converter.TAG_CONVERSION_MAP.put(loc, itemLoc);
        else
            Converter.ITEM_CONVERSION_MAP.put(loc, itemLoc);

        ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.add",
                ItemRegistryManager.getItemResource(item2).toString(), loc.toString()), true);

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

        ItemRegistryManager.getItemTagResources(held.getItem()).forEach(resource -> remove(resource, ctx));

        ConversionsConfig.save();

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    public static int removeTag(CommandContext<CommandSourceStack> ctx) {
        ResourceLocation tag = ResourceLocationArgument.getId(ctx, "tag");

        remove(tag, ctx);

        ConversionsConfig.save();

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static void remove(ResourceLocation resource, CommandContext<CommandSourceStack> ctx) {
        ITag<Item> tag = ItemRegistryManager.getItemTagFromResource(resource);
        Item item = ItemRegistryManager.getItemFromResource(resource);

        if (!tag.isEmpty())
            Converter.TAG_CONVERSION_MAP.remove(resource);
        else if (item != null)
            Converter.ITEM_CONVERSION_MAP.remove(resource);

        ctx.getSource().sendSuccess(() -> Component.translatable("itemtagconverter.command.tagconverter.remove", resource.toString()), true);
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
