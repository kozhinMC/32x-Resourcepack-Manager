package com.stylized_resourcepack_manager.resourcepack_manager_k;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.BlackListsConfigs;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.ModOverrideConfigManager;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.ResourceManagerConfigK;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;

@Mod(ResourceManagerK.MOD_ID)
public class ResourceManagerK {

    public static final String MOD_ID = "resourcepack_manager_k";
    public static final String MOD_CONFIG_ID = "32x_mods_resources-manager";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public ResourceManagerK() {
        // Register the config file
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ResourceManagerConfigK.SPEC, ResourceManagerK.MOD_CONFIG_ID+"/resource-manager-client.toml");

        //Register the config screen factory
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> ConfigScreenProvider.createConfigScreen(screen))
        );

        ModOverrideConfigManager.initialize();
        BlackListsConfigs.initialize();
        ResourceManagerConfigK.loadConfig();
    }

    public static void SendToChatInfo(@NotNull Minecraft mc, String ms, ChatFormatting style){
        if (!ResourceManagerConfigK.ENABLE_CHAT_MESSAGES.get())return;
        if (mc.player!=null)
        mc.player.sendSystemMessage(Component.literal(ms).withStyle(style));
    }

    public static void SendToChatDebug(@NotNull Minecraft mc, String ms, ChatFormatting style){
        if (!ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES.get())return;
        if (mc.player!=null)
            mc.player.sendSystemMessage(Component.literal(ms).withStyle(style));
    }

    public static void SendToLoggerInfo(String ms, @NotNull ChatFormatting style){
        if (!ResourceManagerConfigK.ENABLE_CHAT_MESSAGES.get())return;
        if (style.equals(ChatFormatting.RED))PackManager.LOGGER.error(ms);
        else if (style.equals(ChatFormatting.YELLOW))PackManager.LOGGER.warn(ms);
        else if (style.equals(ChatFormatting.WHITE))PackManager.LOGGER.info(ms);
    }

    public static void SendToLoggerDebug(String ms, @NotNull ChatFormatting style){
        if (!ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES.get())return;
        if (style.equals(ChatFormatting.RED))PackManager.LOGGER.error(ms);
        else if (style.equals(ChatFormatting.YELLOW))PackManager.LOGGER.warn(ms);
        else if (style.equals(ChatFormatting.WHITE))PackManager.LOGGER.info(ms);
    }

    public static class BorderedImageButton extends Button {

        protected final ResourceLocation texture;

        public BorderedImageButton(int x, int y, int width, int height, ResourceLocation texture, OnPress onPress) {
            // We pass Component.empty() because we are not drawing a text label.
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
            this.texture = texture;
        }

        // This method is the only thing we need to change.
        // REVISED AND BETTER renderWidget method for BorderedImageButton.java

        @Override
        public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);

            // 2. Draw your image on top of the button.
            graphics.blit(
                    this.texture,
                    this.getX() + 1, this.getY() + 1, // We add a small offset to fit it nicely inside the border
                    this.width - 2, this.height - 2,   // Render it slightly smaller than the full button
                    0, 0, 64, 64,                    // Use the full 64x64 source image
                    64, 64
            );
        }
    }

    @Mod.EventBusSubscriber(modid =  ResourceManagerK.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientEvents {

        //private static final ResourceLocation CONFIG_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(ResourceManagerK.MOD_ID, "textures/gui/2_logo.png"); // Main 1.20.1
        private static final ResourceLocation CONFIG_BUTTON_TEXTURE = ResourceLocation.tryBuild(ResourceManagerK.MOD_ID, "textures/gui/2_logo.png");

        @SubscribeEvent
        public static void onScreenInit(ScreenEvent.Init.Post event) {
            if (!ResourceManagerConfigK.ENABLE_TITLE_SCREEN_BUTTON.get() || !ResourceManagerConfigK.ENABLE_RESOURCEPACK.get())return;
            // We only want to add the button to the TitleScreen
            if (event.getScreen() instanceof TitleScreen screen) {
                int buttonWidth = 15;
                int buttonHeight = 15;
                int x = 4;
                int y = screen.height / 4 + 48;

                BorderedImageButton configButton = new BorderedImageButton(
                        x, y,
                        buttonWidth, buttonHeight,
                        CONFIG_BUTTON_TEXTURE,
                        (button) ->
                                Minecraft.getInstance().setScreen(
                                        ConfigScreenProvider.createConfigScreen(Minecraft.getInstance().screen)
                                )
                );

                // Add the button to the screen's list of renderable widgets
                event.addListener(configButton);
            }
        }
    }
}
