package com.zenesta.itemtagconverter.common.convert;

import com.zenesta.itemtagconverter.common.registry.ItemRegistryManager;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import javax.annotation.Nullable;
import java.util.*;

public class Converter {
    public static final List<String> TAG_BLACKLIST = Arrays.asList("forge:ores", "forge:nuggets", "forge:ingots",
            "forge:blocks", "forge:dusts", "forge:gears", "forge:coins", "forge:plates", "forge:raw_materials",
            "forge:gems", "forge:crops", "forge:dyes", "forge:foods", "forge:fruits", "forge:glass", "forge:glass_pane",
            "forge:grain", "forge:nuts", "forge:raw_fishes", "forge:rods", "forge:seeds", "forge:storage_blocks",
            "forge:tools", "forge:vegetables", "forge:wires",
            "mekanism:clumps", "mekanism:crystals", "mekanism:dirty_dusts", "mekanism:enriched", "mekanism:shards",
            "balm",
            "minecraft:beacon_payment_items", "minecraft:piglin_loved", "flux:market_accept",
            "minecolonies:sawmill_ingredient_excluded", "minecolonies:blacksmith_product",
            "minecolonies:blacksmith_ingredient", "minecolonies:reduceable_product_excluded",
            "minecolonies:reduceable_ingredient", "resourcefulbees:valid_apiary", "ae2:metal_ingots");
    public static final Map<ResourceLocation, ResourceLocation> TAG_CONVERSION_MAP = new HashMap<>();
    public static final Map<ResourceLocation, ResourceLocation> ITEM_CONVERSION_MAP = new HashMap<>();

    /* UTIL */
    private static boolean isItemStackMatchIgnoreAmount(ItemStack template, ItemStack compare, boolean compareNBT) {
        if (template == null && compare == null)
            return true;
        if ((template == null || compare == null))
            return false; // if either are null but not both
        return (template.getItem() == compare.getItem())
                && (!compareNBT || NbtUtils.compareNbt(template.getTag(), compare.getTag(), true));
    }

    /**
     * Get ItemStack that you want to find the matching with in the list of tag conversion.
     *
     * @param stack ItemStack that you what to find the matching.
     * @return Matched ItemStack, return null if there's no matching.
     */
    public static @Nullable ItemStack getMatchedConvertedItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return null;
        ResourceLocation itemResource = ItemRegistryManager.getItemResource(stack.getItem());
        if (ITEM_CONVERSION_MAP.containsKey(itemResource)) {
            Item matched = ItemRegistryManager.getItemFromResource(ITEM_CONVERSION_MAP.get(itemResource));
            if (matched != null)
                return new ItemStack(matched, stack.getCount());
        }
        for (ResourceLocation resource : ItemRegistryManager.getItemTagResources(stack.getItem())) {
            if (TAG_CONVERSION_MAP.containsKey(resource)) {
                ItemStack matchedStack = new ItemStack(Objects.requireNonNull(ItemRegistryManager.getItemFromResource(TAG_CONVERSION_MAP.get(resource))), stack.getCount());
                if (!isItemStackMatchIgnoreAmount(stack, matchedStack, false)) {
                    return matchedStack;
                }
            }
        }
        return null;
    }

    /* CONVERSION */
    public static void convertInPlayer(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) { // for every item
            ItemStack playerStack = player.getInventory().getItem(i);
            if (!playerStack.isEmpty()) { // not empty slot
                ItemStack matchedItem = getMatchedConvertedItemStack(playerStack);
                if (matchedItem != null) {
                    player.getInventory().setItem(i, ItemStack.EMPTY); // clear out the converted items slot
                    ItemHandlerHelper.insertItemStacked(new PlayerInvWrapper(player.getInventory()), matchedItem,
                            false); // put the converted items in, stacking with items already there
                    player.getInventory().setChanged(); // refresh the client
                }
            }
        }
    }
}
