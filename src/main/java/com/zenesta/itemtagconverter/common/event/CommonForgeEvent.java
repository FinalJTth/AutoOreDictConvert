package com.zenesta.itemtagconverter.common.event;

import com.mojang.brigadier.CommandDispatcher;
import com.zenesta.itemtagconverter.ItemTagConverter;
import com.zenesta.itemtagconverter.common.command.Command;
import com.zenesta.itemtagconverter.common.convert.Converter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.zenesta.itemtagconverter.ItemTagConverter.PAUSED_PLAYERS;

@Mod.EventBusSubscriber(modid = ItemTagConverter.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonForgeEvent {
    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        Command.register(dispatcher);
    }

    @SubscribeEvent
    public static void onItemPickup(final EntityItemPickupEvent event) {
        if (PAUSED_PLAYERS.contains(event.getEntity().getScoreboardName()))
            return;
        ItemEntity itemEntity = event.getItem();
        ItemStack matchedItemStack = Converter.getMatchedConvertedItemStack(itemEntity.getItem());
        if (matchedItemStack != null && event.isCancelable()) {
            event.setCanceled(true);
            itemEntity.setItem(matchedItemStack);
            event.getEntity().onItemPickup(itemEntity);
        }
    }
    /*
    @SubscribeEvent
    public static void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        Consumer<Container> convertContainer = (Container container) -> {
            int maxSlot = container.getContainerSize();

            for (int i = 0; i < maxSlot; i++) {
                ItemStack slotItem = container.getItem(i);

                ItemStack matchedItemStack = Converter.getMatchedConvertedItemStack(slotItem);

                if (matchedItemStack != null) {
                    container.setItem(i, matchedItemStack);
                }
            }
        };

        BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());

        if (blockEntity instanceof Container container) {
            convertContainer.accept(container);
        }
        if (blockEntity instanceof EnderChestBlockEntity) {
            Player player = event.getEntity();

            Container container = player.getEnderChestInventory();

            convertContainer.accept(container);
        }
    }
    */
    @SubscribeEvent
    public static void onPlayerContainerOpen(final PlayerContainerEvent.Open event) {
        AbstractContainerMenu container = event.getContainer();
        NonNullList<Slot> slots = container.slots;

        for (Slot slot : slots) {
            ItemStack slotItem = slot.getItem();

            ItemStack matchedItemStack = Converter.getMatchedConvertedItemStack(slotItem);

            if (matchedItemStack != null) {
                slot.set(matchedItemStack);
            }
        }
    }
}