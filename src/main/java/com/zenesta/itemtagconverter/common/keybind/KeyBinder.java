package com.zenesta.itemtagconverter.common.keybind;

import com.mattdahepic.itemtagconverter.common.config.OptionsConfig;
import com.zenesta.itemtagconverter.common.network.ConvertPacket;
import com.zenesta.itemtagconverter.common.network.NetworkManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.zenesta.itemtagconverter.common.ItemTagConverter;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public class KeyBinder {
    public static final Lazy<KeyMapping> KEYBINDING_CONVERT = Lazy.of(() -> new KeyMapping("key.itemtagconverter.category", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_END,
            "key.itemtagconverter.convert"));

    @Mod.EventBusSubscriber(modid = ItemTagConverter.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerKeyMapping(final RegisterKeyMappingsEvent event) {
            event.register(KEYBINDING_CONVERT.get());
        }
    }

    @Mod.EventBusSubscriber(modid = ItemTagConverter.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onClientTick(final TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) { // Only call code once as the tick event is called twice every tick
                if (KEYBINDING_CONVERT.get().consumeClick() && OptionsConfig.COMMON.enableKeypress.get()) {
                    NetworkManager.INSTANCE.sendToServer(new ConvertPacket());
                }
            }
        }
    }
}
