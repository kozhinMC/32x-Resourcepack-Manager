package com.stylized_resourcepack_manager.resourcepack_manager_k.configs;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.stylized_resourcepack_manager.resourcepack_manager_k.ResourceManagerK;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ResourceManagerConfigK {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_MINECRAFT_OVERRIDES;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_BLOCK_TEXTURE_OVERRIDE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_ITEM_TEXTURE_OVERRIDE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_PAINTING_TEXTURE_OVERRIDE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_PARTICLE_TEXTURE_OVERRIDE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SHOULD_BLACK_LIST_TEXTURES_BE_RENDERED;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_RESOURCEPACK;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_LOAD_RESOURCE_UPON_SAVE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_KEY_EVENTS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_TITLE_SCREEN_BUTTON;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_DEBUG_MESSAGES;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_CHAT_MESSAGES;

    public static final ForgeConfigSpec.ConfigValue<String> API_REPORT_URL;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_REORDERABLE_PACK;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_TOP_OR_BOTTOM;
    public static final ForgeConfigSpec.ConfigValue<Integer> PACK_POSITION;

    static {
        BUILDER.push("Texture Controller Settings");

        // global settings
        ENABLE_RESOURCEPACK = BUILDER.comment("This disables the mod and the resource pack, while keeping all the settings.")
                .define("ENABLE_RESOURCEPACK", true);
        SHOULD_BLACK_LIST_TEXTURES_BE_RENDERED = BUILDER.comment("Should black listed textures be rendered or not, to black list a texture you can use the key events in the key binds or here in black list tab.")
                .define("SHOULD_BLACK_LIST_TEXTURES_BE_RENDERED", false);
        ENABLE_LOAD_RESOURCE_UPON_SAVE = BUILDER.comment("Set this to true if you want to reload resources upon config save.")
                .define("ENABLE_LOAD_RESOURCE_UPON_SAVE", false);
        ENABLE_TITLE_SCREEN_BUTTON = BUILDER.comment("A button in the title screen opens mod configs menu, If you set this to false, it will remove it.")
                .define("ENABLE_TITLE_SCREEN_BUTTON", true);
        ENABLE_KEY_EVENTS = BUILDER.comment("If you want to remove this mods key events from the KeyRegistry and KeyBinds, set this to false, and you will lose the functions as well, requires restart.")
                .define("enableKeyEvents", true);

        API_REPORT_URL = BUILDER.comment("This is the current used URL don't change it, unless it gets an update.")
                .define("API_REPORT_URL", "https://32x-texture-reporter-api.vercel.app/api/log");

        ENABLE_MINECRAFT_OVERRIDES = BUILDER.comment("\n\nEnable to use the resource pack's override textures for vanilla Minecraft blocks and items.")
                .define("enableMinecraftOverrides", false);
        ENABLE_BLOCK_TEXTURE_OVERRIDE = BUILDER.comment("Enable the global block texture override if you want to disable this resource pack block texture overrides set this to false.")
                .define("enableGlobalBlockTextureOverride", true);
        ENABLE_ITEM_TEXTURE_OVERRIDE = BUILDER.comment("Enable the global item texture override if you want to disable this resource pack item texture overrides set this to false.")
                .define("enableGlobalItemTextureOverride", true);
        ENABLE_PAINTING_TEXTURE_OVERRIDE = BUILDER.comment("Enable the global painting texture override if you want to disable this resource pack painting texture overrides set this to false.")
                .define("enableGlobalPaintingTextureOverride", true);
        ENABLE_PARTICLE_TEXTURE_OVERRIDE = BUILDER.comment("Enable the global particle texture override if you want to disable this resource pack particle texture overrides set this to false.")
                .define("enableGlobalParticleTextureOverride", true);


        ENABLE_REORDERABLE_PACK = BUILDER.comment("\n\nIf you want to position the pack set this to true then use the pack position number.")
                .define("ENABLE_REORDERABLE_PACK", false);

        ENABLE_TOP_OR_BOTTOM = BUILDER.comment("\n\nposition always at top true or always at bottom false, still works with reorder as false, for always enable.")
                .define("ENABLE_TOP_OR_BOTTOM", true);
        PACK_POSITION = BUILDER.comment("\n\nposition of the pack from top, calculated TOP pos - PACK_POSITION.")
                .define("PACK_POSITION", 0);



        ENABLE_DEBUG_MESSAGES = BUILDER.comment("\n\nThis is useless only for development or dubbing, false by default.")
                .define("enableDebugMessages", false);
        ENABLE_CHAT_MESSAGES = BUILDER.comment("These messages are useful, they tell you if the mod done something right, or wrong in case of reporting, overall info.")
                .define("enableChatMessages", true);



        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void loadConfig() {
        // Construct the path to our specific config file
        Path configPath = FMLPaths.CONFIGDIR.get().resolve(ResourceManagerK.MOD_CONFIG_ID+"/resource-manager-client.toml");

        // Create a file config manager that can read our file
        final CommentedFileConfig configData = CommentedFileConfig.builder(configPath)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        // Load the data from the file on disk
        configData.load();
        // Feed the loaded data into our ForgeConfigSpec
        SPEC.setConfig(configData);
    }
}