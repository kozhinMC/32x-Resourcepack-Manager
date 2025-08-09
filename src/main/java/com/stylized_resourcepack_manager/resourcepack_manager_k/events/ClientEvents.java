package com.stylized_resourcepack_manager.resourcepack_manager_k.events;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.stylized_resourcepack_manager.resourcepack_manager_k.ConfigScreenProvider;
import com.stylized_resourcepack_manager.resourcepack_manager_k.PackManager;
import com.stylized_resourcepack_manager.resourcepack_manager_k.ResourceManagerK;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.BlackListsConfigs;
import com.stylized_resourcepack_manager.resourcepack_manager_k.TextureFinder;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.ResourceManagerConfigK;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ResourceManagerK.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        if (!ResourceManagerConfigK.ENABLE_KEY_EVENTS.get() || !ResourceManagerConfigK.ENABLE_RESOURCEPACK.get())return;
        event.register(KeyBinds.OPEN_CONFIG_KEY);
        event.register(KeyBinds.REPORT_BROKEN_BLOCK_TEXTURE_KEY);
        event.register(KeyBinds.REPORT_BROKEN_ITEM_TEXTURE_KEY);
        event.register(KeyBinds.BLACK_LIST_BLOCK_TEXTURE_KEY);
        event.register(KeyBinds.UN_BLACK_LIST_BLOCK_TEXTURE_KEY);
        event.register(KeyBinds.BLACK_LIST_ITEM_TEXTURE_KEY);
        event.register(KeyBinds.UN_BLACK_LIST_ITEM_TEXTURE_KEY);
    }

    @Mod.EventBusSubscriber(modid = ResourceManagerK.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (!ResourceManagerConfigK.ENABLE_KEY_EVENTS.get() || !ResourceManagerConfigK.ENABLE_RESOURCEPACK.get())return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player==null)return;
            if (KeyBinds.OPEN_CONFIG_KEY.consumeClick()) {
                Minecraft.getInstance().setScreen(
                        ConfigScreenProvider.createConfigScreen(Minecraft.getInstance().screen)
                );
            }

            if (KeyBinds.REPORT_BROKEN_BLOCK_TEXTURE_KEY.consumeClick()) {
                ResourceLocation id = getTargetBlockID();
                if (id!=null) {
                    ResourceManagerK.SendToChatDebug(mc,"REPORT_BROKEN_BLOCK_TEXTURE_KEY " + id,ChatFormatting.DARK_GREEN);
                    List<String> textures = TextureFinder.getAllTexturesForBlock(id);
                    if (textures.isEmpty())textures = TextureFinder.getAllTexturesForPainting(id);
                    if (!textures.isEmpty()) {
                        if (ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES.get()) textures.forEach(System.out::println);
                        if (ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES.get()) textures.forEach(s -> mc.player.sendSystemMessage(Component.literal(s)));
                        if (textures.size()==1){
                            ResourceManagerK.SendToChatDebug(mc,textures.get(0),ChatFormatting.DARK_GRAY);
                            send_respond(textures.get(0));
                        }
                        else {
                            StringBuilder report = new StringBuilder();
                            for (String texture : textures) report.append(texture).append(",");
                            report.replace(report.length()-1,report.length(),"");
                            ResourceManagerK.SendToChatDebug(mc,report.toString(),ChatFormatting.DARK_GRAY);
                            send_respond(report.toString());
                        }
                    }
                }
            }
            if (KeyBinds.REPORT_BROKEN_ITEM_TEXTURE_KEY.consumeClick()) {
                ResourceLocation id = getHeldItemID();
                if (id != null){
                    ResourceManagerK.SendToChatDebug(mc,"REPORT_BROKEN_ITEM_TEXTURE_KEY " + id,ChatFormatting.DARK_GREEN);
                    List<String> textures = TextureFinder.getAllTexturesForItem(id);
                    if (ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES.get())textures.forEach(System.out::println);
                    if (ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES.get())textures.forEach(s->mc.player.sendSystemMessage(Component.literal(s)));
                    if (!textures.isEmpty()) {
                        if (textures.size() == 1) {
                            ResourceManagerK.SendToChatDebug(mc, textures.get(0), ChatFormatting.DARK_GRAY);
                            send_respond(textures.get(0));
                        } else {
                            StringBuilder report = new StringBuilder();
                            for (String texture : textures) report.append(texture).append(",");
                            report.replace(report.length() - 1, report.length(), "");
                            ResourceManagerK.SendToChatDebug(mc, report.toString(), ChatFormatting.DARK_GRAY);
                            send_respond(report.toString());
                        }
                    }
                }
            }

            if (KeyBinds.BLACK_LIST_BLOCK_TEXTURE_KEY.consumeClick()) {
                ResourceLocation id = getTargetBlockID();
                if (id!=null) {
                    ResourceManagerK.SendToChatDebug(mc,"BLACK_LIST_BLOCK_TEXTURE_KEY " + id,ChatFormatting.DARK_RED);
                    ResourceManagerK.SendToChatInfo(mc,id.getPath()+" Added To Black List.",ChatFormatting.DARK_RED);
                    List<String> textures = TextureFinder.getAllTexturesForBlock(id);
                    if (textures.isEmpty())textures = TextureFinder.getAllTexturesForPainting(id);
                    if (!textures.isEmpty()) {
                        if (ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES.get()) textures.forEach(System.out::println);
                        if (ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES.get()) textures.forEach(s -> mc.player.sendSystemMessage(Component.literal(s)));
                        BlackListsConfigs.loadConfig();
                        BlackListsConfigs.BLOCK_BLACK_LIST.addAll(textures);
                        BlackListsConfigs.saveConfig();
                    }
                }
            }
            if (KeyBinds.UN_BLACK_LIST_BLOCK_TEXTURE_KEY.consumeClick()) {
                ResourceLocation id = getTargetBlockID();
                if (id != null){
                    ResourceManagerK.SendToChatDebug(mc,"UN_BLACK_LIST_BLOCK_TEXTURE_KEY " + id,ChatFormatting.DARK_GREEN);
                    ResourceManagerK.SendToChatInfo(mc,id.getPath()+" Removed From Black List.",ChatFormatting.DARK_GREEN);

                    List<String> textures = TextureFinder.getAllTexturesForBlock(id);
                    if (textures.isEmpty())textures = TextureFinder.getAllTexturesForPainting(id);
                    if (!textures.isEmpty()) {
                        if (ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES.get()) textures.forEach(System.out::println);
                        if (ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES.get()) textures.forEach(s -> mc.player.sendSystemMessage(Component.literal(s)));
                        BlackListsConfigs.loadConfig();
                        textures.forEach(BlackListsConfigs.BLOCK_BLACK_LIST::remove);
                        BlackListsConfigs.saveConfig();
                    }
                }
            }
            if (KeyBinds.BLACK_LIST_ITEM_TEXTURE_KEY.consumeClick()) {
                ResourceLocation id = getHeldItemID();
                if (id != null){
                    ResourceManagerK.SendToChatInfo(mc,id.getPath()+" Added To Black List.",ChatFormatting.DARK_RED);
                    ResourceManagerK.SendToChatDebug(mc,"BLACK_LIST_ITEM_TEXTURE_KEY " + id,ChatFormatting.DARK_RED);
                    List<String> textures = TextureFinder.getAllTexturesForItem(id);
                    if (ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES.get())textures.forEach(System.out::println);
                    if (ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES.get())textures.forEach(s->mc.player.sendSystemMessage(Component.literal(s)));
                    if (!textures.isEmpty()) {
                        BlackListsConfigs.loadConfig();
                        BlackListsConfigs.ITEM_BLACK_LIST.addAll(textures);
                        BlackListsConfigs.saveConfig();
                    }
                }
            }
            if (KeyBinds.UN_BLACK_LIST_ITEM_TEXTURE_KEY.consumeClick()) {
                ResourceLocation id = getHeldItemID();
                if (id!=null) {
                    ResourceManagerK.SendToChatDebug(mc,"UN_BLACK_LIST_ITEM_TEXTURE_KEY " + id,ChatFormatting.DARK_GREEN);
                    ResourceManagerK.SendToChatInfo(mc,id.getPath()+" Removed From Black List.",ChatFormatting.DARK_GREEN);
                    List<String> textures = TextureFinder.getAllTexturesForItem(id);
                    if (ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES.get())textures.forEach(System.out::println);
                    if (ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES.get())textures.forEach(s->mc.player.sendSystemMessage(Component.literal(s)));
                    BlackListsConfigs.loadConfig();
                    textures.forEach(BlackListsConfigs.ITEM_BLACK_LIST::remove);
                    BlackListsConfigs.saveConfig();
                }
            }
        }
    }

    private static ResourceLocation getTargetBlockID() {
        Minecraft mc = Minecraft.getInstance();
        HitResult hitResult = mc.hitResult;

        // Make sure we are looking at something and the player exists
        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS || mc.player == null) {
            if (mc.player!=null)
            mc.player.sendSystemMessage(Component.literal("§cNo target found.").withStyle(ChatFormatting.RED));
            return null;
        }

        // Case 1: The player is looking at a BLOCK
        if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockPos pos = blockHitResult.getBlockPos();
            BlockState state = mc.player.level().getBlockState(pos);
            //            mc.player.sendSystemMessage(Component.literal("Block: " + id));
            return ForgeRegistries.BLOCKS.getKey(state.getBlock());
        }

        // Case 2: The player is looking at an ENTITY
        if (hitResult instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();

            // Check if that entity is a Painting
            if (entity instanceof Painting painting) {
                //                mc.player.sendSystemMessage(Component.literal("Painting: " + id));
                return ForgeRegistries.PAINTING_VARIANTS.getKey(painting.getVariant().value());
            } else {
//                mc.player.sendSystemMessage(Component.literal("§cTarget is not a block or painting."));
                return null;
            }
        }
        return null;
    }

    /**
     * Gets the ResourceLocation of the item held in the player's main hand.
     *
     * @return The ResourceLocation of the held item, or null if no item is held.
     */
    private static ResourceLocation getHeldItemID() {
        Minecraft mc = Minecraft.getInstance();

        // Ensure the player exists
        if (mc.player == null) {
            return null;
        }

        // Get the ItemStack in the player's main hand
        ItemStack heldItemStack = mc.player.getMainHandItem();

        // Check if the player is holding an item
        if (heldItemStack.isEmpty()) {
            mc.player.sendSystemMessage(Component.literal("§cNo item in hand.").withStyle(ChatFormatting.RED));
            return null;
        }

        // Get the ResourceLocation from the item

        // Send the item's ID to the player
//        if (id != null) {
//            mc.player.sendSystemMessage(Component.literal("Item: " + id));
//        }

        return ForgeRegistries.ITEMS.getKey(heldItemStack.getItem());
    }

    private static void send_respond(String path) {
        new Thread(() -> {
            try {
                // Vercel API endpoint URL
                URL url = new URL(ResourceManagerConfigK.API_REPORT_URL.get());
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("POST");
                http.setDoOutput(true);
                http.setRequestProperty("Content-Type", "application/json");
                // connection and read timeouts to prevent the thread from hanging indefinitely
                http.setConnectTimeout(5000); // 5 seconds
                http.setReadTimeout(5000); // 5 seconds

                String jsonPayload = "{\"blockId\": \"" + path + "\"}";
                byte[] out = jsonPayload.getBytes(StandardCharsets.UTF_8);
                try (OutputStream stream = http.getOutputStream()) {
                    stream.write(out);
                }

                Minecraft mc = Minecraft.getInstance();
                int statusCode = http.getResponseCode();
                if (statusCode == 200) {
                    ResourceManagerK.SendToChatInfo(mc,path + " broken texture(s) has been reported.",ChatFormatting.GREEN);
                } else if (statusCode == 429) {
                    try {
                        java.io.InputStream errorStream = http.getErrorStream();
                        java.util.Scanner s = new java.util.Scanner(errorStream).useDelimiter("\\A");
                        String responseBody = s.hasNext() ? s.next() : "";
                        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                        String messageFromServer = jsonObject.get("message").getAsString();
                        ResourceManagerK.SendToChatInfo(mc,messageFromServer,ChatFormatting.YELLOW);
                    } catch (JsonSyntaxException e) {
                        ResourceManagerK.SendToChatInfo(mc,"Error Code: "+statusCode+" Too Many Requests , Time Left: unknown [server sent a malformed response]",ChatFormatting.RED);
                    }
                } else {
                    // OTHER ERROR (500, 400, etc.)
                    ResourceManagerK.SendToChatInfo(mc,"Error Code: "+statusCode + " Connection or Post Error.",ChatFormatting.DARK_RED);
                }
//                    mc.player.sendSystemMessage(Component.literal("Vercel Response: " + http.getResponseCode() + " " + http.getResponseMessage()));
                http.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * This event fires when the game is discovering all possible resource packs.
     * It's the perfect time to scan our target pack and inject our virtual one.
     * @param event The event object containing the pack repository.
     */
    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (!ResourceManagerConfigK.ENABLE_RESOURCEPACK.get())return;
        // am only interested in the client-side resource packs.
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }

        event.addRepositorySource((packConsumer) -> {
            PackManager.scanAndInitialize();

            if (PackManager.VIRTUAL_PACK == null) {
                PackManager.LOGGER.error("Virtual pack was not created. Cannot register it.");
                return; // Do nothing if the virtual pack is null.
            }

//            final Pack virtualPackContainer = Pack.create(
//                    PackManager.VIRTUAL_PACK.packId(), // 1. ID
//                    Component.literal(PackManager.VIRTUAL_PACK.packId()), // 2. Display Name
//                    !ResourceManagerConfigK.ENABLE_REORDERABLE_PACK.get(), // 3. isAlwaysEnabled
//                    (packId) -> PackManager.VIRTUAL_PACK, // 4. ResourcesSupplier
//                    // 5. Pack.Info (using the correct constructor you provided)
//                    new Pack.Info(
//                            Component.literal("Dynamically manages resources via the Resource Pack Manager mod."),
//                            15, // dataFormat
//                            15, // resourceFormat
//                            FeatureFlagSet.of(), // requestedFeatures
//                            false // hidden
//                    ),
//
//                    PackType.CLIENT_RESOURCES, // 6. The type of pack
//                    ResourceManagerConfigK.ENABLE_TOP_OR_BOTTOM.get()?Pack.Position.TOP:Pack.Position.BOTTOM,// 7. The position in the list (TOP or BOTTOM)
//                    true,
//                    PackSource.BUILT_IN // 8. The source of the pack
//            );

            final Pack virtualPackContainer = Pack.create(
                    PackManager.VIRTUAL_PACK.packId(), // 1. ID
                    Component.literal(PackManager.VIRTUAL_PACK.packId()), // 2. Display Name
                    !ResourceManagerConfigK.ENABLE_REORDERABLE_PACK.get(), // 3. isAlwaysEnabled
                    new Pack.ResourcesSupplier() {
                        @Override
                        public @NotNull PackResources openPrimary(@NotNull String p_298664_) {
                            return PackManager.VIRTUAL_PACK;
                        }

                        @Override
                        public @NotNull PackResources openFull(@NotNull String p_251717_, Pack.@NotNull Info p_298253_) {
                            return PackManager.VIRTUAL_PACK;
                        }
                    }, // 4. ResourcesSupplier
                    // 5. Pack.Info (using the correct constructor you provided)
                    new Pack.Info(
                            Component.literal("Dynamically manages resources via the Resource Pack Manager mod."),
                            PackCompatibility.COMPATIBLE, // dataFormat
                            FeatureFlagSet.of(), // requestedFeatures
                            List.of("") // hidden
                    ),
                    ResourceManagerConfigK.ENABLE_TOP_OR_BOTTOM.get() ? Pack.Position.TOP : Pack.Position.BOTTOM,// 7. The position in the list (TOP or BOTTOM)
                    true,
                    PackSource.BUILT_IN // 8. The source of the pack
            );
            PackManager.LOGGER.info("Virtual Pack is generated, am from 32x resource manager mod.");
            // Finally, we "consume" or "accept" the pack, which officially adds it to the game's list.
            packConsumer.accept(virtualPackContainer);
        });
    }

    @SubscribeEvent
    public static void onResourceReload(net.minecraftforge.client.event.RegisterClientReloadListenersEvent event) {
        // Any resource reload means we need to check our pack's position again.
        int pack_pos_config = ResourceManagerConfigK.PACK_POSITION.get();
        if (pack_pos_config < 0) return;

        Minecraft mc_k = Minecraft.getInstance();
        PackRepository repository_k73624 = mc_k.getResourcePackRepository();
        List<String> selectedPacks_k = new ArrayList<>(repository_k73624.getSelectedIds());

        String CVPackId =  PackManager.VIRTUAL_PACK.packId();
        selectedPacks_k.remove(CVPackId);

        int targetIndex_k = Math.max(0, selectedPacks_k.size() - pack_pos_config);
        targetIndex_k = Math.min(selectedPacks_k.size(), targetIndex_k);

        selectedPacks_k.remove(CVPackId);
        selectedPacks_k.add(targetIndex_k, CVPackId);
        repository_k73624.setSelected(selectedPacks_k);
    }

    @SubscribeEvent
    public static void onModelBakingCompleted(ModelEvent.BakingCompleted event) {
        // This event is a reliable signal that resource loading is finished.
        // It's now safe to clear the cached Sets.
        if (!ResourceManagerConfigK.ENABLE_RESOURCEPACK.get())return;
        ResourceManagerK.SendToLoggerDebug("Model baking complete. Clearing dynamic resource pack caches.",ChatFormatting.WHITE);
        PackManager.clearCachedPathData();
    }
}
