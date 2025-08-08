package com.stylized_resourcepack_manager.resourcepack_manager_k.configs;
import com.google.gson.reflect.TypeToken;
import com.stylized_resourcepack_manager.resourcepack_manager_k.cache_tables.BlackListsCache;
import com.stylized_resourcepack_manager.resourcepack_manager_k.PackManager;
import com.stylized_resourcepack_manager.resourcepack_manager_k.ResourceManagerK;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class BlackListsConfigs {
    private static File configFile;
    public static Set<String> BLOCK_BLACK_LIST;
    public static Set<String> ITEM_BLACK_LIST;
    public static Set<String> PARTICLE_BLACK_LIST;

    public static void initialize() {
        // Use a sub-directory for your mod's configs
        File configDir = new File(Minecraft.getInstance().gameDirectory, "config/"+ResourceManagerK.MOD_CONFIG_ID+"/");
        File configDir1 = new File(Minecraft.getInstance().gameDirectory, "config/"+ResourceManagerK.MOD_CONFIG_ID+"/cache");
        if (!configDir.exists()) configDir.mkdirs();
        if (!configDir1.exists()) configDir1.mkdirs();
        configFile = new File(configDir, "black_lists.json");
    }

    public static void loadConfig() {
        BLOCK_BLACK_LIST = new HashSet<>();
        ITEM_BLACK_LIST = new HashSet<>();
        PARTICLE_BLACK_LIST = new HashSet<>();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Type type = new TypeToken<BlackListsCache>() {
                }.getType();
                BlackListsCache loaded = ResourceManagerK.GSON.fromJson(reader, type);
                if (loaded != null) {
                    ITEM_BLACK_LIST.addAll(loaded.ITEM_BLACK_LIST);
                    BLOCK_BLACK_LIST.addAll(loaded.BLOCK_BLACK_LIST);
                    PARTICLE_BLACK_LIST.addAll(loaded.PARTICLE_BLACK_LIST);
                }
            } catch (IOException e) {
                PackManager.LOGGER.error("Failed to load black lists config", e);
            }
        }
    }

    public static void saveConfig() {
        if (BLOCK_BLACK_LIST != null && ITEM_BLACK_LIST!=null&& PARTICLE_BLACK_LIST!=null){
            try (FileWriter writer = new FileWriter(configFile)) {
                ResourceManagerK.GSON.toJson(new BlackListsCache(ITEM_BLACK_LIST,BLOCK_BLACK_LIST,PARTICLE_BLACK_LIST), writer);
                BLOCK_BLACK_LIST.clear();
                BLOCK_BLACK_LIST = null;
                ITEM_BLACK_LIST.clear();
                ITEM_BLACK_LIST = null;
                PARTICLE_BLACK_LIST.clear();
                PARTICLE_BLACK_LIST = null;
            } catch (IOException e) {
                PackManager.LOGGER.error("Failed to save mod override config", e);
            }
        }
    }
}
