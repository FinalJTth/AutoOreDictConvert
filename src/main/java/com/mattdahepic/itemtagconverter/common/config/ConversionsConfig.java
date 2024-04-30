package com.mattdahepic.itemtagconverter.common.config;

import com.zenesta.itemtagconverter.common.convert.Converter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.TreeMap;

public class ConversionsConfig {
    private static final String[] comment = new String[]{
            "# Format for tag conversion (all items in tag `namespace:tag/name` will be converted to destination item `modid:item`):\n",
            "# namespace:tag/name=modid:item\n",
            "# Format for direct item conversion (the item `modid:sourceitem` will be converted to `modid:destitem`):\n",
            "# modid:sourceitem>modid:destitem\n", "\n"};
    public static File file;

    public static void load() {
        try {
            if (!file.exists()) {
                file.createNewFile();
                writeDefaults(file);
            }
            Scanner scn = new Scanner(file);
            while (scn.hasNextLine()) {
                parse(scn.nextLine());
            }
            scn.close();
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            for (String c : comment)
                writer.write(c);
            new TreeMap<>(Converter.TAG_CONVERSION_MAP).forEach((tag, item) -> {
                try {
                    writer.write(tag + "=" + ForgeRegistries.ITEMS.getKey(item) + "\n");
                } catch (IOException ignored) {
                }
            });
            writer.write("\n");
            new TreeMap<>(Converter.ITEM_CONVERSION_MAP).forEach((in, out) -> {
                try {
                    writer.write(ForgeRegistries.ITEMS.getKey(in) + ">" + ForgeRegistries.ITEMS.getKey(out) + "\n");
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    private static void parse(String line) {
        try {
            if (line.startsWith("#"))
                return;
            if (line.isEmpty())
                return;

            if (line.contains("=")) { // tag conversions
                // tag:name=modid:item
                ResourceLocation tag = ResourceLocation.of(line.substring(0, line.indexOf("=")), ':');
                ResourceLocation itemLoc = ResourceLocation.of(line.substring(line.indexOf("=") + 1), ':');
                Item item = ForgeRegistries.ITEMS.getValue(itemLoc);
                Converter.TAG_CONVERSION_MAP.put(tag, item);
            } else if (line.contains(">")) {
                // modid:item>modid:item
                Item in = ForgeRegistries.ITEMS
                        .getValue(ResourceLocation.of(line.substring(0, line.indexOf('>')), ':'));
                Item out = ForgeRegistries.ITEMS
                        .getValue(ResourceLocation.of(line.substring(line.indexOf('>') + 1), ':'));
                Converter.ITEM_CONVERSION_MAP.put(in, out);
            } else {
                throw new RuntimeException("Invalid conversion config on line \"" + line + "\"");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error processing entry \"" + line + "\"! Does the item exist?", e);
        }
    }

    public static void reload() {
        Converter.TAG_CONVERSION_MAP.clear();
        Converter.ITEM_CONVERSION_MAP.clear();
        load();
    }

    private static void writeDefaults(File file) {
        try (FileWriter out = new FileWriter(file)) {
            for (String c : comment)
                out.write(c);
            out.write("# Default conversions config\n");
            out.write("forge:ores/iron=minecraft:iron_ore\n");
            out.write("forge:ores/gold=minecraft:gold_ore\n");
            out.write("forge:ores/lapis=minecraft:lapis_ore\n");
            out.write("forge:ores/diamond=minecraft:diamond_ore\n");
            out.write("forge:ores/emerald=minecraft:emerald_ore\n");
        } catch (IOException e) {

        }
    }
}