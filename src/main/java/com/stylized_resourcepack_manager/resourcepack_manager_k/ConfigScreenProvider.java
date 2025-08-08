package com.stylized_resourcepack_manager.resourcepack_manager_k;
import com.stylized_resourcepack_manager.resourcepack_manager_k.cache_tables.PackManagerCache;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.BlackListsConfigs;
import com.stylized_resourcepack_manager.resourcepack_manager_k.cache_tables.ModOverrideSettings;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.ModOverrideConfigManager;
import com.stylized_resourcepack_manager.resourcepack_manager_k.configs.ResourceManagerConfigK;
import com.stylized_resourcepack_manager.resourcepack_manager_k.events.KeyBinds;
import me.shedaniel.clothconfig2.api.*;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.settings.KeyModifier;

import java.util.*;
import java.util.function.Consumer;

public class ConfigScreenProvider {

    private static boolean isEnableAll = false;
    private static boolean isDisableAll = false;
    private static boolean LoopBreak = false;

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Resource Manager Settings"));
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();


        general.addEntry(entryBuilder.startTextDescription(Component.literal("1. Always delete the cache folder inside the mod's config folder if you downloaded a new version of the resourcepack or applied an update,\nalso the downloaded resource pack needs to be in the available list not the active.\n\n2. The main resource pack name should be \"32X_Stylized_Mods_Resources\" this is what the mod will look for.\n\n3. Disabling the resource pack here will disable everything, but keeps all the setting and the configs, it might require a restart since it removes the Virtual Pack.\n\n4. Enable reload resources config if you want to apply the override configs after save.")).build());

        general.addEntry(entryBuilder.startTextDescription(Component.literal("\n +++ General Settings +++")).build());
        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Resource Pack"), ResourceManagerConfigK.ENABLE_RESOURCEPACK.get())
                .setTooltip(Component.literal("This disables the mod and the resource pack, while keeping all the settings."))
                .setDefaultValue(true)
                .setSaveConsumer(ResourceManagerConfigK.ENABLE_RESOURCEPACK::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Should Black List Textures Be Rendered"), ResourceManagerConfigK.SHOULD_BLACK_LIST_TEXTURES_BE_RENDERED.get())
                .setTooltip(Component.literal("You can use this for debugging or when the pack gets an update and you don't want clear your black Lists and want them to still be rendered, in that case set this to true."))
                .setDefaultValue(false)
                .setSaveConsumer(ResourceManagerConfigK.SHOULD_BLACK_LIST_TEXTURES_BE_RENDERED::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Load Resources Upon Save"), ResourceManagerConfigK.ENABLE_LOAD_RESOURCE_UPON_SAVE.get())
                .setTooltip(Component.literal("Set this to true if you want to reload resources upon config save."))
                .setDefaultValue(false)
                .setSaveConsumer(ResourceManagerConfigK.ENABLE_LOAD_RESOURCE_UPON_SAVE::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Title Screen Button"), ResourceManagerConfigK.ENABLE_TITLE_SCREEN_BUTTON.get())
                .setTooltip(Component.literal("A button in the title screen opens the mod configs menu, If you set this to false, it will remove it."))
                .setDefaultValue(true)
                .setSaveConsumer(ResourceManagerConfigK.ENABLE_TITLE_SCREEN_BUTTON::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Key Events"), ResourceManagerConfigK.ENABLE_KEY_EVENTS.get())
                .setTooltip(Component.literal("If you want to remove this mods key events from the KeyRegistry and KeyBinds, set this to false, and you will lose the functions as well, requires restart."))
                .setDefaultValue(true)
                .setSaveConsumer(ResourceManagerConfigK.ENABLE_KEY_EVENTS::set)
                .build());

        general.addEntry(entryBuilder.startStrField(Component.literal("API Report URL"), ResourceManagerConfigK.API_REPORT_URL.get())
                .setTooltip(Component.literal("don't change it, I added this in case if the URL change for reporting so you don't have to update the mod for a small change."))
                .setDefaultValue("https://32x-texture-reporter-api.vercel.app/api/log")
                .setSaveConsumer(ResourceManagerConfigK.API_REPORT_URL::set)
                .build());


        // Add the "Enable Minecraft Overrides" option ===================================================
        general.addEntry(entryBuilder.startTextDescription(Component.literal("\n +++ Global Overrides +++")).build());
        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Minecraft Overrides"), ResourceManagerConfigK.ENABLE_MINECRAFT_OVERRIDES.get())
                .setTooltip(Component.literal("I disabled minecraft overrides by default,\nsome mods replace vanilla textures with their own this resource pack also includes these textures if you already have 32x for vanilla,\nlike patrix or any other you can disable this so you get the better textures from the other packs, that's only if it doesnt go back to default of the mods if it did enable this so you get at least the 32x."))
                .setDefaultValue(false)
                .setSaveConsumer(ResourceManagerConfigK.ENABLE_MINECRAFT_OVERRIDES::set)
                .build());


        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Global Block Texture Override"), ResourceManagerConfigK.ENABLE_BLOCK_TEXTURE_OVERRIDE.get())
                .setTooltip(Component.literal("Enable the global block texture override if you want to disable\nthis resource pack block texture overrides set this to false."))
                .setDefaultValue(true)
                .setSaveConsumer(ResourceManagerConfigK.ENABLE_BLOCK_TEXTURE_OVERRIDE::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Global Item Texture Override"), ResourceManagerConfigK.ENABLE_ITEM_TEXTURE_OVERRIDE.get())
                .setTooltip(Component.literal("Enable the global item texture override if you want to disable\nthis resource pack item texture overrides set this to false."))
                .setDefaultValue(true)
                .setSaveConsumer(ResourceManagerConfigK.ENABLE_ITEM_TEXTURE_OVERRIDE::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Global Painting Texture Override"), ResourceManagerConfigK.ENABLE_PAINTING_TEXTURE_OVERRIDE.get())
                .setTooltip(Component.literal("Enable the global painting texture override if you want to disable\nthis resource pack painting texture overrides set this to false."))
                .setDefaultValue(true)
                .setSaveConsumer(ResourceManagerConfigK.ENABLE_PAINTING_TEXTURE_OVERRIDE::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Global Particle Texture Override"), ResourceManagerConfigK.ENABLE_PARTICLE_TEXTURE_OVERRIDE.get())
                .setTooltip(Component.literal("Enable the global particle texture override if you want to disable\nthis resource pack particle texture overrides set this to false."))
                .setDefaultValue(true)
                .setSaveConsumer(ResourceManagerConfigK.ENABLE_PARTICLE_TEXTURE_OVERRIDE::set)
                .build());


        general.addEntry(entryBuilder.startTextDescription(Component.literal("\n +++ Virtual Resource Pack Position +++")).build());
        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Reorderable Pack"), ResourceManagerConfigK.ENABLE_REORDERABLE_PACK.get())
                .setTooltip(Component.literal("If you want to position the pack set this to true then use the pack position number."))
                .setDefaultValue(false)
                .setSaveConsumer(ResourceManagerConfigK.ENABLE_REORDERABLE_PACK::set)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Top and Bottom"), ResourceManagerConfigK.ENABLE_TOP_OR_BOTTOM.get())
                .setTooltip(Component.literal("position always at top true or always at bottom false, still works with reorder as false, for always enable."))
                .setDefaultValue(true).setYesNoTextSupplier((state) -> state? Component.literal("Top"):Component.literal("Bottom"))
                .setSaveConsumer(ResourceManagerConfigK.ENABLE_TOP_OR_BOTTOM::set)
                .build());
        general.addEntry(entryBuilder.startIntField(Component.literal("Pack Position"), ResourceManagerConfigK.PACK_POSITION.get())
                .setTooltip(Component.literal("position of the pack from top, calculated TOP pos - PACK_POSITION.\nthis will be used as the pack position once you set reorderable to true, an example of 1 will put the pack before top resource pack.\n!!WARNING!! restart required for the pack position to change so set the right position you want, so you only do this once"))
                .setDefaultValue(0)
                .setSaveConsumer(ResourceManagerConfigK.PACK_POSITION::set)
                .build());

        general.addEntry(entryBuilder.startTextDescription(Component.literal("\n +++ Notifications +++")).build());
        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Chat Messages"), ResourceManagerConfigK.ENABLE_CHAT_MESSAGES.get())
                .setTooltip(Component.literal("These messages are useful, they tell you if the mod done something right, or wrong in case of reporting, overall info."))
                .setDefaultValue(true)
                .setSaveConsumer(ResourceManagerConfigK.ENABLE_CHAT_MESSAGES::set)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Debug Messages"), ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES.get())
                .setTooltip(Component.literal("This is useless only for development or dubbing, false by default."))
                .setDefaultValue(false)
                .setSaveConsumer(ResourceManagerConfigK.ENABLE_DEBUG_MESSAGES::set)
                .build());



        //------------------------------------------------ Mod Overrides -------------------------------------------------------
        isEnableAll = false;
        isDisableAll = false;
        LoopBreak = false;
        ModOverrideConfigManager.loadConfig();
        ConfigCategory modsCategory = builder.getOrCreateCategory(Component.literal("Mod Overrides"));
        modsCategory.addEntry(entryBuilder.startTextDescription(Component.literal("Use this setting to disable specific mods in the resource pack, it's not really necessary if you are here for performance and that's only if you use most of it otherwise it will decrease loading time and used memory while loading.\nWhile this can be confusing, a bit by default I left all the mods off, in case most of people use this resource-pack for couple of mods not 100s, but I left this toggle setting.\nhow it works once you pressed it, it turns everything on.\nthen you can disable those you don't need after save.\nI know most people might leave all on because it's ridicules, and I couldn't make it work there are so many namespaces.")).build());

        modsCategory.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable All"), false)
                .setSaveConsumer(value -> isEnableAll = value).setYesNoTextSupplier((state) -> state? Component.literal("Enable All Set"):Component.literal("Enable All Unset"))
                .setDefaultValue(false).setTooltip(Component.literal("Once you set the settings below this won't have any effects.\nUntil you save Config and exist this screen."))
                .build());
        modsCategory.addEntry(entryBuilder.startBooleanToggle(Component.literal("Disable All"), false)
                .setSaveConsumer(value -> isDisableAll = value).setYesNoTextSupplier((state) -> state? Component.literal("Disable All Set"):Component.literal("Disable All Unset"))
                .setDefaultValue(false).setTooltip(Component.literal("Once you set the settings below this won't have any effects.\nUntil you save Config and exist this screen."))
                .build());
        // Create the list of mod entries now
        final List<SubCategoryListEntry> modEntries = buildModEntries(entryBuilder);

        // Add all the generated mod entries to the category
        modEntries.forEach(modsCategory::addEntry);
        //------------------------------------------------ BLACK LISTS -------------------------------------------------------

        BlackListsConfigs.loadConfig();
        List<String> item_black = BlackListsConfigs.ITEM_BLACK_LIST.stream().toList();
        List<String> block_black = BlackListsConfigs.BLOCK_BLACK_LIST.stream().toList();
        List<String> particle_black = BlackListsConfigs.PARTICLE_BLACK_LIST.stream().toList();
        ConfigCategory listCategory = builder.getOrCreateCategory(Component.literal("Black Lists"));

        listCategory.addEntry(entryBuilder.startTextDescription(Component.literal("These are the black listed textures for both blocks and items, that also include paintings inside the block list.\nTo clear the lists use the Reset button, and also if you wanted to black list particles you need to provide the resource path like the others but for particles, to see an example, black list something using the KeyEvents.\nIn the keybinding you can set the key to help you black list a lot better.\nBecause you can't target particles in the game, there isn't a key feature to let you black list particles like the others, so if you must black list a specific particle texture, you have to add it manually or disable the mod's particle override in Mods Override Tab.")).build());
        listCategory.addEntry(entryBuilder.startStrList(Component.literal("Item Textures Black List"),item_black)
                .setTooltip(Component.literal("You can remove , add , view the items black listed textures here."))
                        .setSaveConsumer(list-> {
                            BlackListsConfigs.ITEM_BLACK_LIST.clear();
                            BlackListsConfigs.ITEM_BLACK_LIST.addAll(list);
                        })
                .setDefaultValue(new ArrayList<>()).build());
        listCategory.addEntry(entryBuilder.startStrList(Component.literal("Block Textures Black List"),block_black)
                .setTooltip(Component.literal("You can remove , add , view the blocks black listed textures here."))
                .setSaveConsumer(list-> {
                    BlackListsConfigs.BLOCK_BLACK_LIST.clear();
                    BlackListsConfigs.BLOCK_BLACK_LIST.addAll(list);
                })
                .setDefaultValue(new ArrayList<>()).build());
        listCategory.addEntry(entryBuilder.startStrList(Component.literal("Particle Textures Black List"),particle_black)
                .setTooltip(Component.literal("You can remove , add , view the particles black listed textures here."))
                .setSaveConsumer(list-> {
                    BlackListsConfigs.PARTICLE_BLACK_LIST.clear();
                    BlackListsConfigs.PARTICLE_BLACK_LIST.addAll(list);
                })
                .setDefaultValue(new ArrayList<>()).build());

        // Populate and add clear button for list 'item_black'
//        SubCategoryBuilder listEntry = entryBuilder.startSubCategory(Component.literal())
//                .setExpanded(false);
//        listEntry.add();
//        addClearButton(entryBuilder, listCategory, item_black, "Item Texture Black List");
//        buildStringListEntries(entryBuilder, listCategory, item_black, "Item Texture Black List");
//        listCategory.addEntry(listEntry.build());
        //------------------------------------------------ Black Lists -------------------------------------------------------
        //------------------------------------------------ KEY EVENTS -------------------------------------------------------

        ConfigCategory keysCategory = builder.getOrCreateCategory(Component.literal("Key Binds"));
        keysCategory.addEntry(entryBuilder.startTextDescription(Component.literal("This is just a description of the KeyEvents what keys are they bind to, and their default keys, and also some information on what they do.")).build());
        addKeyDesc(entryBuilder,keysCategory,"Open Config Screen","This key opens this config screen you currently on, in game.", KeyBinds.OPEN_CONFIG_KEY);
        addKeyDesc(entryBuilder,keysCategory,"Report Broken Block","This key sends the texture names of the targeted block to the mod author for fixing.", KeyBinds.REPORT_BROKEN_BLOCK_TEXTURE_KEY);
        addKeyDesc(entryBuilder,keysCategory,"Report Broken Item","This key sends the item texture names in the main hand to the mod author for fixing.", KeyBinds.REPORT_BROKEN_ITEM_TEXTURE_KEY);
        addKeyDesc(entryBuilder,keysCategory,"Black List Block","This key black list the textures of the targeted block.", KeyBinds.BLACK_LIST_BLOCK_TEXTURE_KEY);
        addKeyDesc(entryBuilder,keysCategory,"Black List Item","This key black list the item textures in the main hand.", KeyBinds.BLACK_LIST_ITEM_TEXTURE_KEY);
        addKeyDesc(entryBuilder,keysCategory,"Un Black List Block","This key un-black list the textures of the targeted block.", KeyBinds.UN_BLACK_LIST_BLOCK_TEXTURE_KEY);
        addKeyDesc(entryBuilder,keysCategory,"Un Black List Item","This key un-black list the item textures in the main hand.", KeyBinds.UN_BLACK_LIST_ITEM_TEXTURE_KEY);


        builder.setSavingRunnable(() -> {
            ResourceManagerConfigK.SPEC.save();
            BlackListsConfigs.saveConfig();
            ModOverrideConfigManager.saveConfig();
            if (ResourceManagerConfigK.ENABLE_LOAD_RESOURCE_UPON_SAVE.get())Minecraft.getInstance().reloadResourcePacks();
            isEnableAll = false;
            isDisableAll = false;
            LoopBreak = false;
        });
        return builder.build();
    }

    private static void addClearButton(ConfigEntryBuilder entryBuilder, SubCategoryBuilder category, List<String> list, String listName) {
        category.add(entryBuilder.startBooleanToggle(Component.literal("Clear "+listName), false)
                        .setSaveConsumer(value -> list.clear()).setYesNoTextSupplier((state) -> Component.literal("Clear"))

                .build());
    }
    private static void addKeyDesc(ConfigEntryBuilder entryBuilder, ConfigCategory category, String name, String desc, KeyMapping keyMapping){
        SubCategoryBuilder subCategoryBuilder = entryBuilder.startSubCategory(Component.literal(name)).setExpanded(true);
        subCategoryBuilder.add(entryBuilder.startTextDescription(Component.literal(desc)).build());

        MutableComponent description = Component.literal("Default Key:   ").withStyle(ChatFormatting.WHITE);
        Component keyComponent = keyMapping.getDefaultKey().getDisplayName().copy().withStyle(ChatFormatting.GOLD);
        description.append(keyComponent);
        KeyModifier modifier = keyMapping.getDefaultKeyModifier();
        if (modifier != KeyModifier.NONE) {
            description.append(Component.literal(" + ").withStyle(ChatFormatting.WHITE));
            description.append(Component.literal(modifier.name()).withStyle(ChatFormatting.GOLD));
        }
        subCategoryBuilder.add(entryBuilder.startTextDescription(description).build());

        MutableComponent description1 = Component.literal("Current Key:   ").withStyle(ChatFormatting.WHITE);
        Component keyComponent1 = keyMapping.getKey().getDisplayName().copy().withStyle(ChatFormatting.GOLD);
        description1.append(keyComponent1);
        KeyModifier modifier1 = keyMapping.getKeyModifier();
        if (modifier1 != KeyModifier.NONE) {
            description1.append(Component.literal(" + ").withStyle(ChatFormatting.WHITE));
            description1.append(Component.literal(modifier1.name()).withStyle(ChatFormatting.GOLD));
        }
        subCategoryBuilder.add(entryBuilder.startTextDescription(description1).build());
        category.addEntry(subCategoryBuilder.build());
    }

    // Mod Overrides
    private static List<SubCategoryListEntry> buildModEntries(ConfigEntryBuilder entryBuilder) {
        List<SubCategoryListEntry> entries = new ArrayList<>();

        // Get a sorted list of namespaces for a clean UI
        List<String> sortedNamespaces = new ArrayList<>(PackManager.SCANNED_NAMESPACES);
        Collections.sort(sortedNamespaces);

        for (String namespace : sortedNamespaces) {
//            PackManager.LOGGER.info("KEY_TO_FIND : "+ namespace);
            // Skip "minecraft" as it's handled in the General tab
            if (namespace.equals("minecraft")) continue;

            ModOverrideSettings settings = ModOverrideConfigManager.getSettings(namespace);

            // Create a collapsible sub-category for each mod
            List<BooleanListEntry> childEntries = new ArrayList<>();
            childEntries.add(makeBooleanEntry(entryBuilder,namespace,
                    value ->{
                    if (LoopBreak)return;
                    if (isEnableAll){
                        LoopBreak = true;
                        ModOverrideConfigManager.modSettings.forEach((key,valuee)->valuee.enabled = true);
                        update_all(true);
                    }else if (isDisableAll){
                        LoopBreak = true;
                        ModOverrideConfigManager.modSettings.forEach((key,valuee)->valuee.enabled = false);
                        update_all(false);
                    }else{
                        settings.enabled = value;
                        PackManagerCache cache = PackManager.load_cache(PackManager.light_assets_cache_file);
                        if (cache!=null) {
                            cache.UpdateFlags.remove(namespace);
                            cache.UpdateFlags.put(namespace, true);
                            PackManager.save_cache(PackManager.light_assets_cache_file,null,null,cache.scannedNameSpaces,cache.UpdateFlags);
                        }else{
                            ResourceManagerK.SendToLoggerDebug("Updating "+namespace+" Failed",ChatFormatting.RED);
                        }
                        ResourceManagerK.SendToLoggerDebug("Updating "+namespace+" Succeeded",ChatFormatting.WHITE);
                    }
                },settings.enabled,"Enable All Overrides for this Mod",false));

            childEntries.add(makeBooleanEntry(entryBuilder,namespace,value -> settings.overrideBlocks = value,settings.overrideBlocks,"Block Textures",true));
            childEntries.add(makeBooleanEntry(entryBuilder,namespace,value -> settings.overrideItems = value,settings.overrideItems,"Item Textures",true));
            childEntries.add(makeBooleanEntry(entryBuilder,namespace,value -> settings.overrideParticles = value,settings.overrideParticles,"Particle Textures",true));
            childEntries.add(makeBooleanEntry(entryBuilder,namespace,value -> settings.overridePaintings = value,settings.overridePaintings,"Painting Textures",true));

            SubCategoryBuilder subCategory = entryBuilder.startSubCategory(Component.literal(namespace))
                    .setTooltip(Component.literal("Configure overrides for the '").append(namespace).append("' mod."));
            subCategory.addAll(childEntries);
            entries.add(subCategory.build());
        }
        return entries;
    }
    private static BooleanListEntry makeBooleanEntry(ConfigEntryBuilder entryBuilder, String namespace, Consumer<Boolean> save,boolean current,String des,boolean defaultV){
        BooleanListEntry be =entryBuilder.startBooleanToggle(Component.literal(des), current)
                .setDefaultValue(defaultV)
                .setSaveConsumer(save)
                .build();
        be.appendSearchTags(List.of(namespace));
        return be;

    }
    private static void update_all(boolean flag){
        PackManagerCache cache = PackManager.load_cache(PackManager.light_assets_cache_file);
        if (cache!=null) {
            cache.UpdateFlags.clear();
            cache.scannedNameSpaces.forEach(name->cache.UpdateFlags.put(name,true));
            PackManager.save_cache(PackManager.light_assets_cache_file,null,null,cache.scannedNameSpaces,cache.UpdateFlags);
            ModOverrideConfigManager.saveConfig();
        }else{
            ResourceManagerK.SendToLoggerDebug(flag?"Enable":"Disable"+" All Failed",ChatFormatting.RED);
        }
    }
}
