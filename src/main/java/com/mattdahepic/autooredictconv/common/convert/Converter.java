package com.mattdahepic.autooredictconv.common.convert;

import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.IReverseTag;
import net.minecraftforge.registries.tags.ITagManager;

import javax.annotation.Nullable;
import java.util.*;

import static com.mattdahepic.autooredictconv.common.AutoOreDictConv.ITEMS_TAG_MANAGER;

public class Converter extends Thread {
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
    public static final Map<ResourceLocation, Item> TAG_CONVERSION_MAP = new HashMap<>();
    public static final Map<Item, Item> ITEM_CONVERSION_MAP = new HashMap<>();

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
        if (ITEM_CONVERSION_MAP.containsKey(stack.getItem())) {
            return new ItemStack(ITEM_CONVERSION_MAP.get(stack.getItem()), stack.getCount());
        }
        ITagManager<Item> itemTags = Objects.requireNonNull(ITEMS_TAG_MANAGER);
        for (IReverseTag<Item> reverseTag : itemTags.getReverseTag(stack.getItem()).stream()
                .toList()) {
            for (TagKey<Item> key : reverseTag.getTagKeys().toList()) {
                ResourceLocation tag = key.location();
                if (TAG_CONVERSION_MAP.containsKey(tag)) {
                    ItemStack matchedStack = new ItemStack(TAG_CONVERSION_MAP.get(tag), stack.getCount());
                    if (!isItemStackMatchIgnoreAmount(stack, matchedStack, false)) {
                        return matchedStack;
                    }
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

    /*
     * Attempts to convert an item and returns the converted item if possible, the
     * original otherwise
     *
     * @param item the item to attempt to convert
     * @return the converted item or the original if not possible
     */
    /*
    public static ItemStack convert(@Nonnull ItemStack item) {
        if (itemConversionMap.containsKey(item.getItem())) {
            return new ItemStack(itemConversionMap.get(item.getItem()), item.getCount());
        }
        ITagManager<Item> itemTags = ForgeRegistries.ITEMS.tags();
        if (itemTags == null)
            throw new NullPointerException("Getting tag registries return null even after checking");
        for (IReverseTag<Item> reverseTag : itemTags.getReverseTag(item.getItem()).stream()
                .toList()) {
            for (TagKey<Item> key : reverseTag.getTagKeys().toList()) {
                ResourceLocation tag = key.location();
                if (tagConversionMap.containsKey(tag)) {
                    return new ItemStack(tagConversionMap.get(tag), item.getCount());
                }
            }
        }
        return item;
    }
    */
}
