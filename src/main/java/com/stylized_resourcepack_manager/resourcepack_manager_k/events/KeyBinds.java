package com.stylized_resourcepack_manager.resourcepack_manager_k.events;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class KeyBinds {
    public static final String KEY_CATEGORY_RESOURCE32XMANAGER = "key.category.texturecontroller";

    public static final String KEY_OPEN_CONFIG = "key.texturecontroller.open_config"; // New unique name

    public static final String KEY_REPORT_BROKEN_BLOCK = "key.texturecontroller.get_texture_block";
    public static final String KEY_REPORT_BROKEN_ITEM = "key.texturecontroller.get_texture_item";
    public static final String KEY_BLACK_LIST_BLOCK_TEXTURE = "key.texturecontroller.black_list_block_texture"; //
    public static final String KEY_UN_BLACK_LIST_BLOCK_TEXTURE = "key.texturecontroller.un-black_list_block_texture";
    public static final String KEY_BLACK_LIST_ITEM_TEXTURE = "key.texturecontroller.black_list_item_texture"; //
    public static final String KEY_UN_BLACK_LIST_ITEM_TEXTURE = "key.texturecontroller.un-black_list_item_texture"; //


    public static final KeyMapping REPORT_BROKEN_BLOCK_TEXTURE_KEY = new KeyMapping(
            KEY_REPORT_BROKEN_BLOCK,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_SUBTRACT, // The default key is 'G'
            KEY_CATEGORY_RESOURCE32XMANAGER
    );
    public static final KeyMapping REPORT_BROKEN_ITEM_TEXTURE_KEY = new KeyMapping(
            KEY_REPORT_BROKEN_ITEM,
            KeyConflictContext.IN_GAME,
            KeyModifier.ALT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_SUBTRACT, // The default key is 'G'
            KEY_CATEGORY_RESOURCE32XMANAGER
    );

    public static final KeyMapping BLACK_LIST_BLOCK_TEXTURE_KEY = new KeyMapping(
            KEY_BLACK_LIST_BLOCK_TEXTURE,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B, // The default key is 'B'
            KEY_CATEGORY_RESOURCE32XMANAGER
    );
    public static final KeyMapping UN_BLACK_LIST_BLOCK_TEXTURE_KEY = new KeyMapping(
            KEY_UN_BLACK_LIST_BLOCK_TEXTURE,
            KeyConflictContext.IN_GAME,
            KeyModifier.ALT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B, // The default key is 'B'
            KEY_CATEGORY_RESOURCE32XMANAGER
    );
    public static final KeyMapping BLACK_LIST_ITEM_TEXTURE_KEY = new KeyMapping(
            KEY_BLACK_LIST_ITEM_TEXTURE,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N, // The default key is 'B'
            KEY_CATEGORY_RESOURCE32XMANAGER
    );
    public static final KeyMapping UN_BLACK_LIST_ITEM_TEXTURE_KEY = new KeyMapping(
            KEY_UN_BLACK_LIST_ITEM_TEXTURE,
            KeyConflictContext.IN_GAME,
            KeyModifier.ALT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N, // The default key is 'B'
            KEY_CATEGORY_RESOURCE32XMANAGER
    );


    public static final KeyMapping OPEN_CONFIG_KEY = new KeyMapping(
            KEY_OPEN_CONFIG,
            KeyConflictContext.IN_GAME, // Can also be KeyConflictContext.GUI for menus
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K, // Let's use 'K' as the default key
            KEY_CATEGORY_RESOURCE32XMANAGER
    );
}
