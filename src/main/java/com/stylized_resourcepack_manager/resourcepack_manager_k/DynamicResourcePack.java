package com.stylized_resourcepack_manager.resourcepack_manager_k;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.BlackListsConfigs;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.ModOverrideConfigManager;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.ResourceManagerConfigK;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DynamicResourcePack implements PackResources{

    private final ZipFile zipFile; // The connection to the ZIP file
    private static Set<String> allKnownPaths; // Your filter data

    public DynamicResourcePack(File targetPackFile, Set<String> allKnownPaths1) throws IOException {
        // Open the zip file once and keep it open until close() is called
        this.zipFile = new ZipFile(targetPackFile);
        allKnownPaths = allKnownPaths1;
    }

    // --- DELEGATED METHODS NOW REIMPLEMENTED USING ZipFile ---

    @Override
    public @NotNull Set<String> getNamespaces(@NotNull PackType type) {
        // We find namespaces by looking for directories inside the "assets" folder
        return zipFile.stream()
                .filter(entry -> !entry.isDirectory() && entry.getName().startsWith("assets/"))
                .map(entry -> {
                    String path = entry.getName().substring("assets/".length());
                    int separatorIndex = path.indexOf('/');
                    return (separatorIndex != -1) ? path.substring(0, separatorIndex) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public IoSupplier<InputStream> getResource(@NotNull PackType type, @NotNull ResourceLocation location) {
//        ResourceManagerK.SendToChatDebug(Minecraft.getInstance(),"getResource: "+ location, ChatFormatting.WHITE);
        final ZipEntry entry = this.zipFile.getEntry("assets/" + location.getNamespace() + "/" + location.getPath());
        if (entry != null) return () -> this.zipFile.getInputStream(entry);
        return null;
    }

    @Override
    public void listResources(@NotNull PackType type, @NotNull String namespace, @NotNull String path, @NotNull ResourceOutput consumer) {
        // The search path within the zip file.
        final String searchPrefix = "assets/" + namespace + "/" + path + "/";

        this.zipFile.stream()
                .filter(entry -> !entry.isDirectory() && entry.getName().startsWith(searchPrefix))
                .forEach(entry -> {
                    try {
                        String resourcePathInPack = entry.getName().substring(("assets/" + namespace + "/").length());
                        ResourceLocation location = new ResourceLocation(namespace, resourcePathInPack);
                        if (this.shouldProvideResource("assets/" + location.getNamespace() + "/" + location.getPath())) {
                            consumer.accept(location, () -> {
                                try {
//                                    ResourceManagerK.SendToLoggerDebug("listResources: InputStream: "+ "assets/" + location.getNamespace() + "/" + location.getPath(), ChatFormatting.WHITE);
                                    return this.zipFile.getInputStream(entry);
                                } catch (IOException e) {
                                    // This inner catch handles errors if the zip is suddenly unreadable
                                    throw new UncheckedIOException(e);
                                }
                            });
                        }
                    } catch (ResourceLocationException e) {
                        // This entry has an invalid path. This is not an error for us, it just means
                        // the pack has a junk file. We log it and skip it, preventing the crash.
                        PackManager.LOGGER.warn("Skipping invalid entry in resource pack because its name is not a valid resource location: {}",  entry.getName());
                    }
                });
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(@NotNull MetadataSectionSerializer<T> deserializer) {
        // Find the pack.mcmeta file
        final ZipEntry metadataEntry = this.zipFile.getEntry("pack.mcmeta");
        if (metadataEntry == null) {
            return null; // The pack has no metadata file at all.
        }
        try (InputStream stream = this.zipFile.getInputStream(metadataEntry)) {
            // Read the entire file into a JsonObject
            JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            String sectionName = deserializer.getMetadataSectionName();
            if (json.has(sectionName)) {
                // Only if it exists, get the section and pass it to the deserializer.
                // We also ensure it's a JsonObject, as expected by the deserializer.
                if (json.get(sectionName).isJsonObject()) {
                    return deserializer.fromJson(json.getAsJsonObject(sectionName));
                }
            }
            return null;

        } catch (Exception e) {
            // It's good practice to catch potential parsing errors too.
            // You can log the error if you want.
            // LOGGER.error("Failed to parse pack.mcmeta for " + packId(), e);
            return null;
        }
    }

    @Override
    public void close() {
        // This is now very important! It closes the zip file handle.
        try {
            this.zipFile.close();
        } catch (IOException e) {
            PackManager.LOGGER.error("Couldn't Close Dynamic Resource Pack");
        }
    }

    public static void clearCachedPathData(){
        if (allKnownPaths!=null) {
            allKnownPaths.clear();
            allKnownPaths = null;
        }
    }

    private boolean shouldProvideResource(String resourcePath) {
        //if the file isn't in our scanned list, we definitely don't have it. False = no True = Yes
        if (allKnownPaths==null||!allKnownPaths.contains(resourcePath)) {
            return false;
        }

        if (!ResourceManagerConfigK.SHOULD_BLACK_LIST_TEXTURES_BE_RENDERED.get()) {
            if (BlackListsConfigs.ITEM_BLACK_LIST != null && BlackListsConfigs.ITEM_BLACK_LIST.contains(resourcePath)) {
                return false;
            }
            if (BlackListsConfigs.BLOCK_BLACK_LIST != null && BlackListsConfigs.BLOCK_BLACK_LIST.contains(resourcePath)) {
                return false;
            }
            if (BlackListsConfigs.PARTICLE_BLACK_LIST != null && BlackListsConfigs.PARTICLE_BLACK_LIST.contains(resourcePath)) {
                return false;
            }
        }

        if (resourcePath.contains("/textures/block/")) {
//            PackManager.LOGGER.info("Resource Overridden: Type:-"+ resourcePath);
            if(!ResourceManagerConfigK.ENABLE_BLOCK_TEXTURE_OVERRIDE.get()) return false;
        }
        if (resourcePath.contains("/textures/item/")) {
//            PackManager.LOGGER.info("Resource Overridden: Type:-"+ resourcePath);
            if(!ResourceManagerConfigK.ENABLE_ITEM_TEXTURE_OVERRIDE.get())return false;
        }
        if (resourcePath.contains("/textures/particle/")) {
//            PackManager.LOGGER.info("Resource Overridden: Type:-"+ resourcePath);
            if(!ResourceManagerConfigK.ENABLE_PARTICLE_TEXTURE_OVERRIDE.get())return false;
        }
        if (resourcePath.contains("/textures/painting/")) {
//            PackManager.LOGGER.info("Resource Overridden: Type:-"+ resourcePath);
            if(!ResourceManagerConfigK.ENABLE_PAINTING_TEXTURE_OVERRIDE.get())return false;
        }

        return ModOverrideConfigManager.shouldProvideModResource(resourcePath);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String @NotNull ... p_252049_) {
        return null;
    }

    @Override
    public @NotNull String packId() {
        return "32x_Stylized_Mods_Resources_Configurable";
    }

    @Override
    public boolean isBuiltin() {
        return PackResources.super.isBuiltin();
    }

    @Override
    public boolean isHidden() {
        return PackResources.super.isHidden();
    }

    @Override
    public @Nullable Collection<PackResources> getChildren() {
        return PackResources.super.getChildren();
    }
}
