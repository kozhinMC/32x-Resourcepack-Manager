package com.stylized_resourcepack_manager.resourcepack_manager_k.configs;

import com.google.gson.reflect.TypeToken;
import com.stylized_resourcepack_manager.resourcepack_manager_k.PackManager;
import com.stylized_resourcepack_manager.resourcepack_manager_k.ResourceManagerK;
import com.stylized_resourcepack_manager.resourcepack_manager_k.cache_tables.ModOverrideSettings;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModOverrideConfigManager {
    private static File configFile;
    // Use a ConcurrentHashMap for thread safety, though not strictly necessary here, it's good practice.
    public static Map<String, ModOverrideSettings> modSettings = new ConcurrentHashMap<>();

    public static void initialize() {
        // Use a sub-directory for your mod's configs
        File configDir = new File(Minecraft.getInstance().gameDirectory, "config/"+ResourceManagerK.MOD_CONFIG_ID+"/");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        configFile = new File(configDir, "mod_overrides.json");
        loadConfig();
    }

    public static void loadConfig() {
        modSettings = new ConcurrentHashMap<>();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Type type = new TypeToken<ConcurrentHashMap<String, ModOverrideSettings>>() {}.getType();
                Map<String, ModOverrideSettings> loaded = ResourceManagerK.GSON.fromJson(reader, type);
                if (loaded != null) {
                    modSettings.putAll(loaded);
                    PackManager.LOGGER.info("Successfully loaded {} mod override configurations.", loaded.size());
                }
            } catch (IOException e) {
                PackManager.LOGGER.error("Failed to load mod override config", e);
            }
        }
        // Ensure all currently scanned namespaces have a default entry if they are missing
        if (PackManager.SCANNED_NAMESPACES != null) {
            for (String namespace : PackManager.SCANNED_NAMESPACES) {
                // This adds a new default entry for any mod that doesn't have one in the config file yet
                modSettings.computeIfAbsent(namespace, k -> new ModOverrideSettings());
            }
        }
        saveConfig(); // Save to add any new entries to the file
    }

    public static void saveConfig() {
        if (modSettings!=null)
        try (FileWriter writer = new FileWriter(configFile)) {
            ResourceManagerK.GSON.toJson(modSettings, writer);
        } catch (IOException e) {
            PackManager.LOGGER.error("Failed to save mod override config", e);
        }
    }

    public static ModOverrideSettings getSettings(String namespace) {
        // Guarantees you never get a null, provides a default if one doesn't exist.
        if (modSettings==null) modSettings = new ConcurrentHashMap<>();
        return modSettings.computeIfAbsent(namespace, k -> new ModOverrideSettings());
    }

    /**
     * The main logic check for the resource pack system.
     * @param resourcePath The full asset path (e.g., "assets/thermal/textures/block/machine.png")
     * @return True if the resource should be provided by the dynamic pack.
     */
    public static boolean shouldProvideModResource(String resourcePath) {
        if (resourcePath == null || !resourcePath.startsWith("assets/")) {
            return false;
        }

        String pathWithoutAssets = resourcePath.substring(7); // "thermal/textures/block/machine.png"
        int slashIndex = pathWithoutAssets.indexOf('/');
        if (slashIndex == -1) {
            return false; // Invalid path
        }

        String namespace = pathWithoutAssets.substring(0, slashIndex);

        // Let the main config handle vanilla textures
        if ("minecraft".equals(namespace)) {
            return ResourceManagerConfigK.ENABLE_MINECRAFT_OVERRIDES.get(); // Example
        }

        ModOverrideSettings settings = getSettings(namespace);
        // If the entire mod is disabled, deny immediately.
        if (!settings.enabled) {
            return false;
        }

        // Check based on path category
        if (pathWithoutAssets.contains("/textures/block/")) return settings.overrideBlocks;
        if (pathWithoutAssets.contains("/textures/item/")) return settings.overrideItems;
        if (pathWithoutAssets.contains("/textures/particle/")) return settings.overrideParticles;
        if (pathWithoutAssets.contains("/textures/painting/")) return settings.overridePaintings;

        // Default to true if the mod is enabled but the asset doesn't fit a specific category (e.g., models, sounds)
        return true;
    }

    public static void clearData(){
        if (modSettings!=null) {
            modSettings.clear();
            modSettings = null;
        }
    }
}