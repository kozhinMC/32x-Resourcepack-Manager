package com.stylized_resourcepack_manager.resourcepack_manager_k;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stylized_resourcepack_manager.resourcepack_manager_k.cache_tables.PackManagerCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.InputStreamReader;
import java.util.*;

public class TextureFinder {


    // GETTERS
    /**
     * Public entry point for getting textures of a BLOCK from its ResourceLocation.
     * Use this when you are looking at a block in the world.
     * @param blockId The ResourceLocation of the block (e.g., "minecraft:grass_block").
     * @return A List of all texture paths associated with the block's models.
     */
    public static List<String> getAllTexturesForBlock(ResourceLocation blockId) {
        if (!ForgeRegistries.BLOCKS.containsKey(blockId)) {
            ResourceManagerK.SendToLoggerDebug("Not a valid block ID: " + blockId,ChatFormatting.YELLOW);
            return Collections.emptyList();
        }
        net.minecraft.server.packs.resources.ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

        ArrayList<String> paths =  new ArrayList<>(getBlockTextures(blockId, resourceManager));
        if (paths.isEmpty()) {
            PackManagerCache all_assets = PackManager.load_cache(PackManager.all_assets_cache_file);
            if (all_assets!=null) {
                ArrayList<String> paths1 = new ArrayList<>(getSmarterForcedPathsForBlock(blockId, all_assets.ResourcePackDataCache));
                if (paths1.isEmpty()) return new ArrayList<>(getForcedTexturePathForBlock(blockId));
                else return paths1;
            }else return new ArrayList<>(getForcedTexturePathForBlock(blockId));
        }
        return paths;
    }

    /**
     * Public entry point for getting textures of an ITEM from its ItemStack.
     * Use this when you are holding an item in your hand.
     * @param itemid The item id from the player's hand.
     * @return A List of all texture paths associated with the item's model.
     */
    public static List<String> getAllTexturesForItem(ResourceLocation itemid) {
        net.minecraft.server.packs.resources.ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        return new ArrayList<>(getItemTextures(itemid, resourceManager, new HashSet<>()));
    }

    // FORCED PATHS
    /**
     * Searches a pre-filled cache of assets to find all block textures matching a naming convention for the given block ID.
     * This is highly effective for blocks that don't have explicit model files.
     *
     * @param blockId The ResourceLocation of the block.
     * @param resourcePackDataCache A map where the key is a namespace and the value is a list of all asset paths in that namespace.
     * @return A list of all matching texture paths found in the cache.
     */
    public static List<String> getSmarterForcedPathsForBlock(ResourceLocation blockId, Map<String, ArrayList<String>> resourcePackDataCache) {
        String namespace = blockId.getNamespace();
        String blockPath = blockId.getPath();

        // 1. Get all assets for the relevant namespace from your cache
        List<String> namespaceAssets = resourcePackDataCache.get(namespace);
        if (namespaceAssets == null || namespaceAssets.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> foundPaths = new HashSet<>();
        // 2. Define the search prefix we expect for this block's textures
        String searchPrefix = "assets/" + namespace + "/textures/block/" + blockPath;

        // 3. Iterate through all assets and find matches
        for (String assetPath : namespaceAssets) {
            // We are looking for files that start with the prefix and end with .png
            // This will find "my_block.png", "my_block_top.png", "my_block_side.png", etc.
            if (assetPath.startsWith(searchPrefix) && assetPath.endsWith(".png")) {
                foundPaths.add(assetPath);
            }
        }

        return new ArrayList<>(foundPaths);
    }

    /**
     * Searches a pre-filled cache of assets to find all item textures matching a naming convention for the given item ID.
     *
     * @param itemId The ResourceLocation of the item.
     * @param resourcePackDataCache A map where the key is a namespace and the value is a list of all asset paths in that namespace.
     * @return A list of all matching texture paths found in the cache.
     */
    public static List<String> getSmarterForcedPathsForItem(ResourceLocation itemId, Map<String, ArrayList<String>> resourcePackDataCache) {
        String namespace = itemId.getNamespace();
        String itemPath = itemId.getPath();

        List<String> namespaceAssets = resourcePackDataCache.get(namespace);
        if (namespaceAssets == null || namespaceAssets.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> foundPaths = new HashSet<>();
        // The only change is searching in the "item" texture directory
        String searchPrefix = "assets/" + namespace + "/textures/item/" + itemPath;

        for (String assetPath : namespaceAssets) {
            if (assetPath.startsWith(searchPrefix) && assetPath.endsWith(".png")) {
                foundPaths.add(assetPath);
            }
        }

        return new ArrayList<>(foundPaths);
    }

    /**
     * Public entry point for getting the textures of a Painting from its variant ID.
     * @param paintingId The ResourceLocation of the painting variant (e.g., "minecraft:aztec").
     * @return A List containing the path to the painting's main texture and its back texture.
     */
    public static List<String> getAllTexturesForPainting(ResourceLocation paintingId) {
        // 1. Validate that this is a real painting variant
        if (!ForgeRegistries.PAINTING_VARIANTS.containsKey(paintingId)) {
            ResourceManagerK.SendToLoggerDebug("Not a valid painting variant ID: " + paintingId,ChatFormatting.YELLOW);
            return Collections.emptyList();
        }

        Set<String> texturePaths = new HashSet<>();

        // 2. Construct the path to the main painting texture.
        // The convention is that a painting with ID "mod:name" has its texture at "assets/mod/textures/painting/name.png"
        ResourceLocation frontTextureLocation = ResourceLocation.fromNamespaceAndPath(paintingId.getNamespace(), "textures/painting/" + paintingId.getPath() + ".png");
        texturePaths.add("assets/" + frontTextureLocation.getNamespace() + "/" + frontTextureLocation.getPath());


        // 3. Add the texture for the back of the painting, which is universal.
//        texturePaths.add("assets/minecraft/textures/painting/back.png");

        return new ArrayList<>(texturePaths);
    }

    /**
     * Constructs a direct, assumed texture path for a BLOCK based on its ID.
     * This does NOT parse any model files and assumes a standard naming convention.
     * @param blockId The ResourceLocation of the block (e.g., "minecraft:stone").
     * @return A List containing a single, guessed texture path.
     */
    public static List<String> getForcedTexturePathForBlock(ResourceLocation blockId) {
        // Convention: A block with ID "mod:name" likely has a texture at "assets/mod/textures/block/name.png"
        ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(blockId.getNamespace(), "textures/block/" + blockId.getPath() + ".png");
        String fullPath = "assets/" + textureLocation.getNamespace() + "/" + textureLocation.getPath();
        return Collections.singletonList(fullPath);
    }

    /**
     * Constructs a direct, assumed texture path for an ITEM based on its ID.
     * This does NOT parse any model files and assumes a standard naming convention.
     * @param itemId The ResourceLocation of the item (e.g., "minecraft:apple").
     * @return A List containing a single, guessed texture path.
     */
    public static List<String> getForcedTexturePathForItem(ResourceLocation itemId) {
        // Convention: An item with ID "mod:name" likely has a texture at "assets/mod/textures/item/name.png"
        ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "textures/item/" + itemId.getPath() + ".png");
        String fullPath = "assets/" + textureLocation.getNamespace() + "/" + textureLocation.getPath();
        return Collections.singletonList(fullPath);
    }

    // PARSERS
    /**
     * Retrieves all textures associated with a block by parsing its blockstate and model files.
     */
    private static Set<String> getBlockTextures(ResourceLocation blockId, net.minecraft.server.packs.resources.ResourceManager resourceManager) {
        Set<String> texturePaths = new HashSet<>();
        // 1. Find the blockstate file
        ResourceLocation blockstateLocation = ResourceLocation.fromNamespaceAndPath(blockId.getNamespace(), "blockstates/" + blockId.getPath() + ".json");

        Optional<Resource> resourceProvider = resourceManager.getResource(blockstateLocation);
        if (resourceProvider.isPresent())
        try (InputStreamReader reader = new InputStreamReader(resourceProvider.get().open())) {
            JsonObject blockstateJson = JsonParser.parseReader(reader).getAsJsonObject();

            // 2. The blockstate can have many "variants" or be a "multipart" block. We need to find all possible models.
            Set<ResourceLocation> modelLocations = new HashSet<>();
            if (blockstateJson.has("variants")) {
                JsonObject variants = blockstateJson.getAsJsonObject("variants");
                for (JsonElement variantElement : variants.asMap().values()) {
                    // Models can be in an array or a single object
                    if (variantElement.isJsonArray()) {
                        variantElement.getAsJsonArray().forEach(elem -> modelLocations.add(ResourceLocation.parse(elem.getAsJsonObject().get("model").getAsString())));
                    } else {
                        modelLocations.add(ResourceLocation.parse(variantElement.getAsJsonObject().get("model").getAsString()));
                    }
                }
            }
            if (blockstateJson.has("multipart")) {
                blockstateJson.getAsJsonArray("multipart").forEach(part -> {
                    JsonObject apply = part.getAsJsonObject().getAsJsonObject("apply");
                    if (apply.isJsonArray()) {
                        apply.getAsJsonArray().forEach(elem -> modelLocations.add(ResourceLocation.parse(elem.getAsJsonObject().get("model").getAsString())));
                    } else {
                        modelLocations.add(ResourceLocation.parse(apply.get("model").getAsString()));
                    }
                });
            }


            // 3. For each unique model, parse it to find its textures
            for (ResourceLocation modelLocation : modelLocations) {
                texturePaths.addAll(getTexturesFromModel(modelLocation, resourceManager, new HashSet<>()));
            }

        } catch (Exception e) {
            ResourceManagerK.SendToLoggerDebug("Error parsing blockstate for " + blockId + ": " + e.getMessage(),ChatFormatting.RED);
        }

        return texturePaths;
    }

    /**
     * Retrieves all textures from a given model JSON. It recursively checks parent models.
     * This is used by both items and blocks.
     */
    private static Set<String> getItemTextures(ResourceLocation itemId, net.minecraft.server.packs.resources.ResourceManager resourceManager, Set<ResourceLocation> visitedModels) {
        // First, check the item's own model file to see if it's just a block redirect.
        ResourceLocation itemModelFile = ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "models/item/" + itemId.getPath() + ".json");

        try {
            Optional<Resource> resourceOptional = resourceManager.getResource(itemModelFile);
            if (resourceOptional.isPresent()) {
                try (InputStreamReader reader = new InputStreamReader(resourceOptional.get().open())) {
                    JsonObject modelJson = JsonParser.parseReader(reader).getAsJsonObject();
                    if (modelJson.has("parent")) {
                        // Use ResourceLocation.parse to correctly handle namespaces
                        ResourceLocation parentLocation = ResourceLocation.parse(modelJson.get("parent").getAsString());

                        // THE CORE LOGIC: Check if the parent model is in the 'block/' directory.
                        if (parentLocation.getPath().startsWith("block/")) {
                            ResourceManagerK.SendToChatInfo(Minecraft.getInstance(),"Item '" + itemId + "' uses a block model directly. No unique item assets found.",ChatFormatting.YELLOW);
                            return Collections.emptySet(); // Return empty set as requested.
                        }
                    }
                }
            }
        } catch (Exception e) {
            // This can happen if the model file is malformed or other IO issues. Fail safely.
            ResourceManagerK.SendToLoggerDebug("Could not pre-check item model for " + itemId + ": " + e.getMessage(),ChatFormatting.YELLOW);
        }

        // If the check above didn't stop us, proceed with the original recursive search.
        ResourceLocation modelIdForRecursion = ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "item/" + itemId.getPath());
        Set<String> paths = getTexturesFromModel(modelIdForRecursion, resourceManager, visitedModels);
        if (paths.isEmpty()){
            PackManagerCache all_assets = PackManager.load_cache(PackManager.all_assets_cache_file);
            if (all_assets!=null) {
                Set<String> paths1 = new HashSet<>(getSmarterForcedPathsForItem(itemId, all_assets.ResourcePackDataCache));
                if (paths1.isEmpty()) return new HashSet<>(getForcedTexturePathForItem(itemId));
                else return paths1;
            }else return new HashSet<>(getForcedTexturePathForItem(itemId));
        }
        else return paths;
    }

    private static Set<String> getTexturesFromModel(ResourceLocation modelId,  net.minecraft.server.packs.resources.ResourceManager resourceManager, Set<ResourceLocation> visitedModels) {
        // Prevent infinite recursion if models reference each other in a loop
        if (!visitedModels.add(modelId)) {
            return new HashSet<>();
        }

        Set<String> texturePaths = new HashSet<>();

        try {
            ResourceLocation properModelLocation = ResourceLocation.fromNamespaceAndPath(modelId.getNamespace(), "models/" + modelId.getPath() + ".json");
            Optional<Resource> modelResourceOptional = resourceManager.getResource(properModelLocation);

            if (modelResourceOptional.isEmpty()) {
                // This is a normal case for things like "builtin/generated" which don't have a physical model file.
                // We no longer print an error here, as it's not always an error.
                return texturePaths;
            }

            Resource modelResource = modelResourceOptional.get();

            try (InputStreamReader reader = new InputStreamReader(modelResource.open())) {
                JsonObject modelJson = JsonParser.parseReader(reader).getAsJsonObject();

                // 1. Recursively get textures from the parent model, if it exists
                if (modelJson.has("parent")) {
                    ResourceLocation parentModelId = ResourceLocation.parse(modelJson.get("parent").getAsString());
                    texturePaths.addAll(getTexturesFromModel(parentModelId, resourceManager, visitedModels));
                }

                // 2. Get all textures defined in *this* model
                if (modelJson.has("textures")) {
                    JsonObject texturesObject = modelJson.getAsJsonObject("textures");
                    for (JsonElement textureValue : texturesObject.asMap().values()) {
                        String path = textureValue.getAsString();
                        // Sometimes the value is a reference to another key in the same file (e.g., "particle": "#side"). We ignore these.
                        if (!path.startsWith("#")) {
                            ResourceLocation textureLocation = ResourceLocation.parse(path);
                            texturePaths.add("assets/" + textureLocation.getNamespace() + "/textures/" + textureLocation.getPath() + ".png");
                        }
                    }
                }
            }
        } catch (Exception e) {
            ResourceManagerK.SendToLoggerDebug("Error parsing model " + modelId + ": " + e.getMessage(), ChatFormatting.YELLOW);
        }
        return texturePaths;
    }
}
