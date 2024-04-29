package com.mattdahepic.autooredictconv.common.convert;

import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.IReverseTag;
import net.minecraft.nbt.NbtUtils;

import javax.annotation.Nonnull;
import java.util.*;

public class Conversions {
	public static Map<ResourceLocation, Item> tagConversionMap = new HashMap<>();
	public static Map<Item, Item> itemConversionMap = new HashMap<>();
	public static final List<String> tagBlacklist = Arrays.asList("forge:ores", "forge:nuggets", "forge:ingots",
			"forge:blocks", "forge:dusts", "forge:gears", "forge:coins", "forge:plates",
			"minecraft:beacon_payment_items", "minecraft:piglin_loved", "flux:market_accept",
			"minecolonies:sawmill_ingredient_excluded", "minecolonies:blacksmith_product",
			"minecolonies:blacksmith_ingredient", "minecolonies:reduceable_product_excluded",
			"minecolonies:reduceable_ingredient", "resourcefulbees:valid_apiary", "ae2:metal_ingots");

	/* UTIL */
	private static boolean isSameIgnoreStackSize(ItemStack template, ItemStack compare, boolean compareNBT) {
		if (template == null && compare == null)
			return true;
		if ((template == null || compare == null) && !(template == null && compare == null))
			return false; // if either are null but not both
		return (template.getItem() == compare.getItem())
				&& (!compareNBT || NbtUtils.compareNbt(template.getTag(), compare.getTag(), true));
	}

	/* HELPERS */
	public static boolean itemHasConversion(ItemStack stack) {
		if (stack.isEmpty())
			return false;
		if (itemConversionMap.containsKey(stack.getItem())
				&& !isSameIgnoreStackSize(new ItemStack(itemConversionMap.get(stack.getItem())), stack, false))
			return true;
		for (IReverseTag<Item> reverseTag : ForgeRegistries.ITEMS.tags().getReverseTag(stack.getItem()).stream()
				.toList()) {
			for (TagKey<Item> key : reverseTag.getTagKeys().toList()) {
				ResourceLocation tag = key.location();
				if (tagConversionMap.containsKey(tag)
						&& !isSameIgnoreStackSize(new ItemStack(tagConversionMap.get(tag)), stack, false))
					return true;
			}
		}
		return false;
	}

	/* CONVERSION */
	public static void convert(Player player) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) { // for every item
			ItemStack playerStack = player.getInventory().getItem(i);
			if (!playerStack.isEmpty()) { // not empty slot
				if (itemHasConversion(playerStack)) {
					ItemStack convertedItem = convert(playerStack);
					player.getInventory().setItem(i, ItemStack.EMPTY); // clear out the converted items slot
					ItemHandlerHelper.insertItemStacked(new PlayerInvWrapper(player.getInventory()), convertedItem,
							false); // put the converted items in, stacking with items already there
					player.getInventory().setChanged(); // refresh the client
				}
			}
		}
	}

	/**
	 * Attempts to convert an item and returns the converted item if possible, the
	 * original otherwise
	 * 
	 * @param item the item to attempt to convert
	 * @return the converted item or the original if not possible
	 */
	public static ItemStack convert(@Nonnull ItemStack item) {
		if (itemConversionMap.containsKey(item.getItem())) {
			return new ItemStack(itemConversionMap.get(item.getItem()), item.getCount());
		}
		for (IReverseTag<Item> reverseTag : ForgeRegistries.ITEMS.tags().getReverseTag(item.getItem()).stream()
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
}
