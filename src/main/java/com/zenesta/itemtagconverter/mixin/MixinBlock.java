package com.zenesta.itemtagconverter.mixin;

import com.zenesta.itemtagconverter.common.convert.Converter;
import com.zenesta.itemtagconverter.config.OptionsConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Block.class)
public class MixinBlock {
    @ModifyVariable(method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static ItemStack injected(ItemStack defaultItem) {
        // return if the "enableBlockDropsConversion" is false in the common config
        if (!OptionsConfig.COMMON.enableBlockDropsConversion.get())
            return defaultItem;

        // Get matched item
        ItemStack matchedItemStack = Converter.getMatchedConvertedItemStack(defaultItem);

        // Return matched item
        if (matchedItemStack != null)
            return matchedItemStack;

        // Else return default
        return defaultItem;
    }
}
