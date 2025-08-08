package com.stylized_resourcepack_manager.resourcepack_manager_k;


import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.BlackListsConfigs;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.ModOverrideConfigManager;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.ResourceManagerConfigK;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

public class DynamicResourcePack implements PackResources{

    // This is a direct handle to the real ZIP file. It will do the actual reading.
    private final PackResources underlyingPack;
    private static Set<String> allKnownPaths;

    public DynamicResourcePack(File targetPackFile, Set<String> allKnownPaths1 ) {
        // We initialize a standard FilePackResources which knows how to read from a ZIP.
        // This is our connection to the actual assets on disk.
        this.underlyingPack = new FilePackResources(targetPackFile.getName(), targetPackFile, true);
        allKnownPaths = allKnownPaths1; // Assuming you add a getter for this
    }

    public static void clearCachedPathData(){
        if (allKnownPaths!=null) {
            allKnownPaths.clear();
            allKnownPaths = null;
        }
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String @NotNull ... p_252049_) {
        return null;
    }

    /**
     * PROVIDE THE ASSET: If hasResource() returned true, Minecraft calls this to get the data.
     * We don't implement any logic here; we just delegate the request to the underlying
     * pack that is connected to the real ZIP file.
     */
    @Override
    public IoSupplier<InputStream> getResource(@NotNull PackType type, @NotNull ResourceLocation location) {
//        if (mc.player!=null)mc.player.sendSystemMessage(Component.literal(location.getPath() + " getResource am getResource.").withStyle(ChatFormatting.YELLOW));
        return this.underlyingPack.getResource(type, location);
    }

    /**
     * THE GATEKEEPER: This is where you provide the assets to the game by either
     * saying "yes, I have it" or "no, I don't."
     */
    @Override
    public void listResources(@NotNull PackType type, @NotNull String namespace, @NotNull String path, @NotNull ResourceOutput consumer) {
        // When Minecraft asks for a list of all resources, we must filter it.
        // We ask the underlying pack for its complete list...
//        if (mc.player!=null)mc.player.sendSystemMessage(Component.literal(path + " listed am listed.").withStyle(ChatFormatting.GREEN));
//        PackManager.LOGGER.warn(path+" listed am listed logger");
        this.underlyingPack.listResources(type, namespace, path, (loc, streamSupplier) -> {
            // filter function
            if (this.shouldProvideResource("assets/" + loc.getNamespace() + "/" + loc.getPath())) {
                consumer.accept(loc, streamSupplier);
            }
        });
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

//        // Now, check against your categorized lists and the config.
//        if (PackManager.BLOCK_TEXTURE_PATHS!=null&&PackManager.BLOCK_TEXTURE_PATHS.contains(resourcePath)) {
////            PackManager.LOGGER.info("Resource Overridden: Type:-"+ resourcePath);
//             if(!ResourceManagerConfigK.ENABLE_BLOCK_TEXTURE_OVERRIDE.get()) return false;
//        }
//        if (PackManager.ITEM_TEXTURE_PATHS!=null&&PackManager.ITEM_TEXTURE_PATHS.contains(resourcePath)) {
////            PackManager.LOGGER.info("Resource Overridden: Type:-"+ resourcePath);
//            if(!ResourceManagerConfigK.ENABLE_ITEM_TEXTURE_OVERRIDE.get())return false;
//        }
//        if (PackManager.PARTICLE_TEXTURE_PATHS!=null&&PackManager.PARTICLE_TEXTURE_PATHS.contains(resourcePath)) {
////            PackManager.LOGGER.info("Resource Overridden: Type:-"+ resourcePath);
//            if(!ResourceManagerConfigK.ENABLE_PARTICLE_TEXTURE_OVERRIDE.get())return false;
//        }
//        if (PackManager.PAINTING_TEXTURE_PATHS!=null&&PackManager.PAINTING_TEXTURE_PATHS.contains(resourcePath)) {
////            PackManager.LOGGER.info("Resource Overridden: Type:-"+ resourcePath);
//            if(!ResourceManagerConfigK.ENABLE_PAINTING_TEXTURE_OVERRIDE.get())return false;
//        }

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

    @Override
    public @NotNull Set<String> getNamespaces(@NotNull PackType type) {
        return this.underlyingPack.getNamespaces(type);
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(@NotNull MetadataSectionSerializer<T> deserializer) throws IOException {
        return this.underlyingPack.getMetadataSection(deserializer);
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
    public void close() {
        this.underlyingPack.close();
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
