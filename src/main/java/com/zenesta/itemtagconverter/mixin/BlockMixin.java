package com.zenesta.itemtagconverter.mixin;

import com.zenesta.itemtagconverter.common.convert.Converter;
import com.zenesta.itemtagconverter.common.config.CommonConfig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Supplier;

@Mixin(Block.class)
public abstract class BlockMixin {
    /*
    @ModifyVariable(method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V", at = @At("STORE"), ordinal = 0)
    private static ItemEntity injectedPopResource(ItemEntity itemEntity) {
        // return if the "enableBlockDropsConversion" is false in the common config
        ItemTagConverter.LOGGER.debug("FIRED");
        if (!OptionsConfig.COMMON.enableBlockDropsConversion.get())
            return defaultItem;

        // Get matched item
        ItemStack matchedItemStack = Converter.getMatchedConvertedItemStack(itemEntity.getItem());

        // Return matched item
        if (matchedItemStack != null) {
            ItemTagConverter.LOGGER.debug("DROPPED : {}", matchedItemStack.getItem().getDescriptionId());
            return matchedItemStack;
        }

        // Else return default
        return defaultItem;
    }
    */

    @Inject(method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "net/minecraft/world/level/Level.addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void injectedPopResource(Level pLevel, Supplier<ItemEntity> pItemEntitySupplier, ItemStack pStack, CallbackInfo ci, ItemEntity itemEntity) {
        // return if the "enableBlockDropsConversion" is false in the common config
        if (CommonConfig.COMMON.enableBlockDropsConversion.get()) {
            // Get matched item
            ItemStack matchedItemStack = Converter.getMatchedConvertedItemStack(pStack);

            // Return matched item
            if (matchedItemStack != null) {
                itemEntity.setItem(matchedItemStack);
            }
        }
    }

    /*
    @Overwrite
    private static void popResource(Level pLevel, Supplier<ItemEntity> pItemEntitySupplier, ItemStack pStack) {
        if (OptionsConfig.COMMON.enableBlockDropsConversion.get()) {
            ItemStack matchedItemStack = Converter.getMatchedConvertedItemStack(pStack);
            if (matchedItemStack != null) {
                ItemTagConverter.LOGGER.debug("DROPPED : {}", matchedItemStack.getItem().getDescriptionId());
                pStack = matchedItemStack;
            }
        }

        if (!pLevel.isClientSide && !pStack.isEmpty() && pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !pLevel.restoringBlockSnapshots) {
            ItemEntity itementity = pItemEntitySupplier.get();
            itementity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(itementity);
        }
    }
    */
    /*
    @ModifyVariable(method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static ItemStack injectedPopResourcePub(ItemStack defaultItem) {
        // return if the "enableBlockDropsConversion" is false in the common config
        ItemTagConverter.LOGGER.debug("PUB FIRED");
        if (!OptionsConfig.COMMON.enableBlockDropsConversion.get())
            return defaultItem;

        // Get matched item
        ItemStack matchedItemStack = Converter.getMatchedConvertedItemStack(defaultItem);

        // Return matched item
        if (matchedItemStack != null) {
            ItemTagConverter.LOGGER.debug("PUB DROPPED : {}", matchedItemStack.getItem().getDescriptionId());
            return matchedItemStack;
        }

        // Else return default
        return defaultItem;
    }

    @ModifyVariable(method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static ItemStack injectedPopResourceFromFacePub(ItemStack defaultItem) {
        // return if the "enableBlockDropsConversion" is false in the common config
        ItemTagConverter.LOGGER.debug("FROMFACE FIRED");
        if (!OptionsConfig.COMMON.enableBlockDropsConversion.get())
            return defaultItem;

        // Get matched item
        ItemStack matchedItemStack = Converter.getMatchedConvertedItemStack(defaultItem);

        // Return matched item
        if (matchedItemStack != null) {
            ItemTagConverter.LOGGER.debug("FROMFACE DROPPED : {}", matchedItemStack.getItem().getDescriptionId());
            return matchedItemStack;
        }

        // Else return default
        return defaultItem;
    }
    */
}
