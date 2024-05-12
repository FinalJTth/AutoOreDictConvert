package com.zenesta.itemtagconverter.common.event;

import com.mojang.brigadier.CommandDispatcher;
import com.zenesta.itemtagconverter.common.ItemTagConverter;
import com.zenesta.itemtagconverter.common.command.Command;
import com.zenesta.itemtagconverter.common.convert.Converter;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import static com.zenesta.itemtagconverter.common.ItemTagConverter.PAUSED_PLAYERS;

@Mod.EventBusSubscriber(modid = ItemTagConverter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
    public static void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        if(event.side == LogicalSide.CLIENT)
            return;

        if(event.phase != TickEvent.Phase.END)
            return;

        ServerPlayer player = (ServerPlayer) event.player;

        ServerPlayerGameMode gameMode = player.gameMode;

        HitResult hitResult =  player.pick(20.0D, 0.0F, false);
        BlockPos blockpos;

        if (hitResult.getType() != HitResult.Type.BLOCK)
            return;
        else
            blockpos = ((BlockHitResult) hitResult).getBlockPos();

        if(!gameMode.destroyBlock(blockpos))
            return;

        // Get reference to level and cast it to ServerLevel.
        ServerLevel serverLevel = (ServerLevel) player.level();

        // Get block state that is being destroyed.
        BlockState blockState = serverLevel.getBlockState(blockpos);

        blockState.onDestroyedByPlayer(serverLevel, blockpos, player, blockState.canHarvestBlock(serverLevel, blockpos, player), false);
        blockState.getBlock().
    }
    */
}