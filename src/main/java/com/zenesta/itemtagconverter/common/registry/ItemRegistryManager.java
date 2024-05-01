package com.zenesta.itemtagconverter.common.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.tags.IReverseTag;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ItemRegistryManager {
    public static final IForgeRegistry<Item> ITEMS_REGISTRIES = ForgeRegistries.ITEMS;
    public static final ITagManager<Item> ITEMS_TAG_MANAGER = Objects.requireNonNull(ForgeRegistries.ITEMS.tags());

    public static @NotNull ResourceLocation getItemResource(ItemLike item) {
        return Objects.requireNonNull(ITEMS_REGISTRIES.getKey(item.asItem()));
    }

    public static @NotNull List<ResourceLocation> getItemTagResources(ItemLike item) {
        return ITEMS_TAG_MANAGER.getReverseTag(item.asItem()).stream().flatMap(IReverseTag::getTagKeys).map(TagKey::location).collect(Collectors.toList());
    }

    public static @Nullable Item getItemFromResource(ResourceLocation resource) {
        return ITEMS_REGISTRIES.getValue(resource);
    }

    public static @NotNull ITag<Item> getItemTagFromResource(ResourceLocation resource) {
        return ITEMS_TAG_MANAGER.getTag(ItemTags.create(resource));
    }

    public static @NotNull List<String> getAllItemTagResourceName() {
        return ITEMS_TAG_MANAGER.getTagNames().map(TagKey::location).map(ResourceLocation::toString).toList();
    }

    public static @NotNull List<String> getAllItemResourceName() {
        return ITEMS_REGISTRIES.getValues().stream().map(item -> Objects.requireNonNull(ITEMS_REGISTRIES.getKey(item)).toString()).toList();
    }
}
