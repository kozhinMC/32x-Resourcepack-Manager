package com.stylized_resourcepack_manager.resourcepack_manager_k;

import com.stylized_resourcepack_manager.resourcepack_manager_k.cache_tables.PackManagerCache;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.BlackListsConfigs;
import com.stylized_resourcepack_manager.resourcepack_manager_k.cache_tables.ModOverrideSettings;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.ModOverrideConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;

import net.minecraft.server.packs.FilePackResources; // <-- IMPORTANT IMPORT


public class PackManager {

    // A logger for printing messages to the console/log file for debugging
    public static final Logger LOGGER = LogManager.getLogger(ResourceManagerK.MOD_ID);

    // --- CONFIGURATION ---
    // This is the MOST IMPORTANT variable. It must exactly match the filename of the main resource pack zip.
    public static final String TARGET_PACK_FILENAME = "32X_Stylized_Mods_Resources.zip";
    public static Set<String> SCANNED_NAMESPACES = new HashSet<>();
    public static DynamicResourcePack VIRTUAL_PACK;
    private static final File targetPackFile = new File(new File(Minecraft.getInstance().gameDirectory, "resourcepacks"), TARGET_PACK_FILENAME);
    public static final File all_assets_cache_file = new File(Minecraft.getInstance().gameDirectory, "config/"+ResourceManagerK.MOD_CONFIG_ID+"/cache/" + ResourceManagerK.MOD_ID + "_all_assets_cache.json");
    private static final File used_assets_cache_file = new File(Minecraft.getInstance().gameDirectory, "config/"+ResourceManagerK.MOD_CONFIG_ID+"/cache/" + ResourceManagerK.MOD_ID + "_used_assets_cache.json");
    public static final File light_assets_cache_file = new File(Minecraft.getInstance().gameDirectory, "config/"+ResourceManagerK.MOD_CONFIG_ID+"/cache/" + ResourceManagerK.MOD_ID + "_light_assets_cache.json");

    public static void scanAndInitialize() {
        LOGGER.info("trying to load main resource pack manually: '{}'", TARGET_PACK_FILENAME);

        // 2. Check if the target zip file actually exists
        if (!targetPackFile.exists() || !targetPackFile.isFile()) {
            LOGGER.warn("Target resource pack '{}' not found. Dynamic features disabled.", TARGET_PACK_FILENAME);
            // If the pack is gone, delete any old cache
            if(all_assets_cache_file.exists()) {
                all_assets_cache_file.delete();
            }
            if(used_assets_cache_file.exists()) {
                used_assets_cache_file.delete();
            }
            if(light_assets_cache_file.exists()) {
                light_assets_cache_file.delete();
            }
            VIRTUAL_PACK = null; // Ensure the virtual pack is not created
            return;
        }

//        LOGGER.info("Found target pack file '{}'. Opening and scanning contents or loading cache...", targetPackFile.getAbsolutePath());
        PackManagerCache cache_used = load_cache(used_assets_cache_file);
        PackManagerCache cache_light = load_cache(light_assets_cache_file);
        if(cache_used != null&&cache_light!=null&&!cache_light.scannedNameSpaces.isEmpty()&&!cache_used.ActiveInUseDataCache.isEmpty()){
            LOGGER.info("Found resource pack file: loading cached assets...");
            ModOverrideConfigManager.loadConfig();
            BlackListsConfigs.loadConfig();
            SCANNED_NAMESPACES = new HashSet<>();
            SCANNED_NAMESPACES.addAll(cache_light.scannedNameSpaces);
            Set<String> ss = update_used_cached(cache_light.UpdateFlags,ModOverrideConfigManager.modSettings,cache_used.ActiveInUseDataCache);
            LOGGER.info("Loaded {} active used resources.", ss.size());
            VIRTUAL_PACK = new DynamicResourcePack(targetPackFile,ss);
            return;
        }

        generate_cache();
    }

    private static void generate_cache(){
        // A try-with-resources block ensures the file is closed automatically
        try (PackResources resources = new FilePackResources(TARGET_PACK_FILENAME,targetPackFile, true)) {
            Set<String> namespaces = resources.getNamespaces(PackType.CLIENT_RESOURCES);
            SCANNED_NAMESPACES.clear();
            SCANNED_NAMESPACES.addAll(namespaces);
            ResourceManagerK.SendToLoggerDebug("Found namespaces in pack: " + SCANNED_NAMESPACES, ChatFormatting.WHITE);

            Map<String,ArrayList<String>> all_resources = new HashMap<>();
            for (String namespace : namespaces) {
                ArrayList<String> paths = new ArrayList<>();
                resources.listResources(PackType.CLIENT_RESOURCES, namespace, "textures", (location, ioSupplier) -> paths.add("assets/" + namespace + "/" + location.getPath()));
                all_resources.put(namespace,paths);
            }

//            for (String path : allScannedPaths) {
//                if (path.contains("/textures/particle/")) {
//                    PARTICLE_TEXTURE_PATHS.add(path);
//                } else if (path.contains("/textures/block/")) {
//                    BLOCK_TEXTURE_PATHS.add(path);
//                } else if (path.contains("/textures/item/")) {
//                    ITEM_TEXTURE_PATHS.add(path);
//                } else if (path.contains("/textures/painting/")) {
//                    PAINTING_TEXTURE_PATHS.add(path);
//                }
//            }

            if (SCANNED_NAMESPACES.isEmpty()||all_resources.isEmpty()){
                LOGGER.error("An unexpected error occurred while scanning the resource pack file: Couldn't Find Any Assets.");
                VIRTUAL_PACK = new DynamicResourcePack( targetPackFile,Collections.emptySet());
                return;
            }
            LOGGER.info(all_resources.size()+" Cached and Saved.");
            ModOverrideConfigManager.loadConfig();
            BlackListsConfigs.loadConfig();

            Map<String,Boolean> update_flags = new HashMap<>();
            SCANNED_NAMESPACES.forEach(name->update_flags.put(name,true));
            save_cache(all_assets_cache_file,all_resources,null,null,null);
            save_cache(light_assets_cache_file,null,null,SCANNED_NAMESPACES,update_flags);
            save_cache(used_assets_cache_file,null,new HashSet<>(),null,null);
            VIRTUAL_PACK = new DynamicResourcePack(targetPackFile,new HashSet<>());
            LOGGER.info("Found resource pack file': resource pack is active now and up to date. caching assets...");
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred while scanning the resource pack file.", e);
            VIRTUAL_PACK = new DynamicResourcePack(targetPackFile,Collections.emptySet());
        }
    }

    /**
     * Calculates the SHA-256 hash of a file.
     * This is our reliable "fingerprint" to detect if the file has changed.
     * param file The file to hash.
     * @return The hex string representation of the hash.
     */
    private static String calculateFileHash() {
        try (FileInputStream fis = new FileInputStream(targetPackFile)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            LOGGER.error("Failed to calculate hash for file: {}", targetPackFile.getName(), e);
            return ""; // Return empty string on failure
        }
    }

    /**
     * Saves the currently loaded path sets to the cache file.
     * param map The hash of the file that was just scanned.
     */
    public static void save_cache(File file, Map<String, ArrayList<String>> ResourcePackDataCache, Set<String> activeUse, Set<String> name_spaces, Map<String, Boolean> UpdateFlags) {
        PackManagerCache data = new PackManagerCache();
        data.fileHash = calculateFileHash();
        data.ResourcePackDataCache = ResourcePackDataCache;
        data.ActiveInUseDataCache = activeUse;
        data.scannedNameSpaces = name_spaces;
        data.UpdateFlags = UpdateFlags;
        if (file.equals(light_assets_cache_file)&&!file.exists()&&data.UpdateFlags.size()==0)data.scannedNameSpaces.forEach((name)-> data.UpdateFlags.put(name,true));

        try (FileWriter writer = new FileWriter(file)) {
            ResourceManagerK.GSON.toJson(data, writer);
            LOGGER.info("Successfully saved resource pack asset cache."+file.getName());
        } catch (IOException e) {
            LOGGER.error("Failed to write cache file.", e);
        }
    }

    public static PackManagerCache load_cache(File file){
        if (file.exists()) {
            try {
                LOGGER.info("Cache file found. Validating...");
                PackManagerCache cachedData = ResourceManagerK.GSON.fromJson(new FileReader(file), PackManagerCache.class);
                String currentHash = calculateFileHash();

                if (cachedData != null && cachedData.fileHash != null && cachedData.fileHash.equals(currentHash)) {
                    LOGGER.info("Cache is valid! Loading asset paths from cache.");
                    return cachedData; // We are done, no need to scan!
                } else {
                    LOGGER.warn("Cache is invalid or outdated (hashes do not match). Re-scanning is required, attempting to regenerate cache...");
                    if(file.exists()) {
                        file.delete();
                    }
                    //generate_cache()
                    //return loadInUseCache(false)
                }
            } catch (Exception e) {
                LOGGER.warn("Could not read cache file. It might be corrupted. Re-scanning.", e);
            }
        }
        return null;
    }

    private static Set<String> update_used_cached(Map<String, Boolean> UpdateFlags, Map<String, ModOverrideSettings> modSettings,Set<String> current){
        if(!UpdateFlags.containsValue(true))return current;
        PackManagerCache all = load_cache(all_assets_cache_file);
        if (all==null||all.ResourcePackDataCache.isEmpty()){
            all_assets_cache_file.delete();
            used_assets_cache_file.delete();
            light_assets_cache_file.delete();
            return current;
        }
        for (String scannedNamespace : SCANNED_NAMESPACES) {
            if (UpdateFlags.get(scannedNamespace)){
                if(modSettings.get(scannedNamespace).enabled){
                    all.ResourcePackDataCache.get(scannedNamespace).forEach(current::remove);
                    current.addAll(all.ResourcePackDataCache.get(scannedNamespace));
                }else{
                    all.ResourcePackDataCache.get(scannedNamespace).forEach(current::remove);
                }
            }
        }

        ResourceManagerK.SendToLoggerDebug("Used list got updated.",ChatFormatting.WHITE);
        save_cache(used_assets_cache_file,null,current,null,null);
        Map<String, Boolean> UpdateFlagsNew = new HashMap<>();
        SCANNED_NAMESPACES.forEach((name)-> UpdateFlagsNew.put(name,false));
        save_cache(light_assets_cache_file,null,null,SCANNED_NAMESPACES,UpdateFlagsNew);
        return current;
    }

    public static void clearCachedPathData(){
        BlackListsConfigs.saveConfig();
        ModOverrideConfigManager.clearData();
        DynamicResourcePack.clearCachedPathData();
    }
}
