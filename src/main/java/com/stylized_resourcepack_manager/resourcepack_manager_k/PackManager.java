package com.stylized_resourcepack_manager.resourcepack_manager_k;

import com.stylized_resourcepack_manager.resourcepack_manager_k.cache_tables.ModOverrideSettings;
import com.stylized_resourcepack_manager.resourcepack_manager_k.cache_tables.PackManagerCache;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.BlackListsConfigs;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.ModOverrideConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;


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

    public static DynamicResourcePack getVirtualPack(File targetPackFile, Set<String> allKnownPaths1){
        try {
            return new DynamicResourcePack( targetPackFile,allKnownPaths1);
        }catch (Exception e1) {
            PackManager.LOGGER.error("Zip Reader Filed");
            return null;
        }
    }

    public static void scanAndInitialize() {
        LOGGER.info("Attempting to directly locate and scan target resource pack: '{}'", TARGET_PACK_FILENAME);

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

        LOGGER.info("Found target pack file '{}'. Opening and scanning contents or loading cache...", targetPackFile.getAbsolutePath());
        PackManagerCache cache_used = load_cache(used_assets_cache_file);
        PackManagerCache cache_light = load_cache(light_assets_cache_file);
        if(cache_used != null&&cache_light!=null){
            ModOverrideConfigManager.loadConfig();
            BlackListsConfigs.loadConfig();
            SCANNED_NAMESPACES = new HashSet<>();
            SCANNED_NAMESPACES.addAll(cache_light.scannedNameSpaces);
            Set<String> ss = update_used_cached(cache_light.UpdateFlags,ModOverrideConfigManager.modSettings,cache_used.ActiveInUseDataCache);
            LOGGER.info("Loaded {} active used resources.", ss.size());
            VIRTUAL_PACK = getVirtualPack(targetPackFile,ss);

            return;
        }
        generate_cache_1_20_2();
    }

    /**
     * Scans the resource pack file using Java's ZipFile class to be compatible with Minecraft 1.20.2+.
     * This function replaces the original generate_cache method that relied on the now-inaccessible FilePackResources constructor.
     */
    private static void generate_cache_1_20_2() {
        // --- The primary change is in this try-with-resources block ---
        try (ZipFile packZipFile = new ZipFile(targetPackFile)) {

            // 1. Replicate "resources.getNamespaces(PackType.CLIENT_RESOURCES)"
            // We find all unique namespaces by looking at the directory structure inside "assets/".
            SCANNED_NAMESPACES.clear();
            Set<String> namespaces = packZipFile.stream()
                    .filter(entry -> !entry.isDirectory() && entry.getName().startsWith("assets/"))
                    .map(entry -> {
                        // Extract the namespace from a path like "assets/minecraft/textures/block/stone.png" -> "minecraft"
                        String path = entry.getName().substring("assets/".length());
                        int separatorIndex = path.indexOf('/');
                        return (separatorIndex != -1) ? path.substring(0, separatorIndex) : null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            SCANNED_NAMESPACES.addAll(namespaces);
            ResourceManagerK.SendToLoggerDebug("Found namespaces in pack: " + SCANNED_NAMESPACES, ChatFormatting.WHITE);

            // 2. Replicate "resources.listResources" for textures
            // We iterate through our found namespaces and list all files under their "textures" folder.
            Map<String, ArrayList<String>> all_resources = new HashMap<>();
            for (String namespace : namespaces) {
                ArrayList<String> paths = new ArrayList<>();
                final String texturePathPrefix = "assets/" + namespace + "/textures/";

                packZipFile.stream()
                        .filter(entry -> !entry.isDirectory() && entry.getName().startsWith(texturePathPrefix))
                        .forEach(entry -> paths.add(entry.getName())); // The path inside the zip is what we need

                // Only add the namespace if it actually contains texture files
                if (!paths.isEmpty()) {
                    all_resources.put(namespace, paths);
                }
            }

            // --- All the logic below this point remains identical to your original function ---

            if (SCANNED_NAMESPACES.isEmpty()) return;
            LOGGER.info(all_resources.size() + " Cached and Saved.");
            ModOverrideConfigManager.loadConfig();
            BlackListsConfigs.loadConfig();

            Map<String, Boolean> update_flags = new HashMap<>();
            SCANNED_NAMESPACES.forEach(name -> update_flags.put(name, true));
            save_cache(all_assets_cache_file, all_resources, null, null, null);
            save_cache(light_assets_cache_file, null, null, SCANNED_NAMESPACES, update_flags);
            save_cache(used_assets_cache_file, null, new HashSet<>(), null, null);
            VIRTUAL_PACK = new DynamicResourcePack(targetPackFile, new HashSet<>());

        } catch (IOException e) { // Catch IOException specifically for ZipFile errors
            LOGGER.error("An error occurred while reading the resource pack zip file.", e);
            VIRTUAL_PACK = getVirtualPack(targetPackFile, Collections.emptySet());
        } catch (Exception e) { // Keep a general catch for your other logic
            LOGGER.error("An unexpected error occurred after scanning the resource pack file.", e);
            VIRTUAL_PACK = getVirtualPack(targetPackFile, Collections.emptySet());
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
