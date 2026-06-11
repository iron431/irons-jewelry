package io.redspace.ironsjewelry.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.parameters.IBonusParameterType;
import io.redspace.ironsjewelry.registry.AssetHandlerRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.ItemRegistry;
import io.redspace.ironsjewelry.utils.JewelryModTags;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

public class GenerateSiteData {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.generate_recipe_data.failed"));

    private static final String RECIPE_DATA_TEMPLATE = """
            - id: "%s"
              name: "%s"
              path: "%s"
              group: "%s"
              craftingType: "%s"
              item0Id: "%s"
              item0: "%s"
              item0Path: "%s"
              item1Id: "%s"
              item1: "%s"
              item1Path: "%s"
              item2Id: "%s"
              item2: "%s"
              item2Path: "%s"
              item3Id: "%s"
              item3: "%s"
              item3Path: "%s"
              item4Id: "%s"
              item4: "%s"
              item4Path: "%s"
              item5Id: "%s"
              item5: "%s"
              item5Path: "%s"
              item6Id: "%s"
              item6: "%s"
              item6Path: "%s"
              item7Id: "%s"
              item7: "%s"
              item7Path: "%s"
              item8Id: "%s"
              item8: "%s"
              item8Path: "%s"
              tooltip: "%s"
              description: ""
            
            """;

    private static final String PATTERN_DATA_TEMPLATE = """
            - name: "%s"
              icon: "/img/patterns/%s.png"
              locked: "%s"
              part_for_quality: "%s"
              quality: %s
              part1: "%s"
              part2: "%s"
              part3: "%s"
              part4: "%s"
              bonus1: "%s"
              bonus2: "%s"
              bonus3: "%s"
              bonus4: "%s"
            
            """;
    private static final String MATERIAL_DATA_TEMPLATE = """
            - name: "%s"
              icon: "/img/materials/%s.png"
              source: "%s"
              group: "%s"
              quality: %s
              bonus_types: "%s"
              bonus_values: "%s"
              sort: "%s"
            
            """;

    protected static int generateSiteData(CommandSourceStack source) {
        generateRecipeData(source);

        generatePatternData(source);

        generateMaterialData(source);

        return 1;
    }

    static ServerLevel level;

    public static List<Item> getVisibleItems() {
        return BuiltInRegistries.ITEM.stream().filter(item -> CreativeModeTabs.allTabs().stream().anyMatch(tab -> tab.contains(new ItemStack(item)))).toList();
    }

    private static void generateRecipeData(CommandSourceStack source) {
        try {
            var itemBuilder = new StringBuilder();
            var blockBuilder = new StringBuilder();
            level = source.getLevel();

            Set<Item> itemsTracked = new HashSet<>();
            handleArtisanScrollEntry(itemBuilder, itemsTracked, source);
            getVisibleItems()
                    .stream()
                    .sorted(Comparator.comparing(Item::getDescriptionId))
                    .forEach(item -> {
                        var itemResource = BuiltInRegistries.ITEM.getKey(item);
                        var tooltip = getTooltip(source.getPlayer(), new ItemStack(item));

                        if (itemResource.getNamespace().equals(IronsJewelry.MODID) && !itemsTracked.contains(item)) {
                            var recipe = getRecipeFor(source, item);
                            var name = item.getName(ItemStack.EMPTY).getString();
                            if (false && item instanceof BlockItem) {
                                if (recipe != null) {
                                    appendToBuilder(blockBuilder, recipe, getRecipeData(recipe), "", tooltip);
                                } else {
                                    appendToBuilder2(blockBuilder, name, itemResource, tooltip);
                                }
                            } else {
                                if (recipe != null) {
                                    appendToBuilder(itemBuilder, recipe, getRecipeData(recipe), handleGenericItemGrouping(item), tooltip);
                                } else {
                                    appendToBuilder3(itemBuilder, name, itemResource, handleGenericItemGrouping(item), tooltip);
                                }
                            }
                            itemsTracked.add(item);

                        }
                    });
            var file = new BufferedWriter(new FileWriter("site_data/item_data.yml"));
            file.write(postProcess(itemBuilder));
            file.close();

//            file = new BufferedWriter(new FileWriter("site_data/block_data.yml"));
//            file.write(postProcess(blockBuilder));
//            file.close();
        } catch (Exception e) {
            IronsJewelry.LOGGER.debug(e.getMessage());
        }
    }

    private static String handleGenericItemGrouping(Item item) {
        if (item instanceof BlockItem) {
            return "Blocks";
        } else if (new ItemStack(item).is(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems")))) {
            return "Gems";
        } else {
            return "All";
        }
    }

    @NotNull
    private static ArrayList<RecipeIngredientData> getRecipeData(Recipe<?> recipe) {
        var resultItemResourceLocation = BuiltInRegistries.ITEM.getKey(recipe.getResultItem(level.registryAccess()).getItem());
        var recipeData = new ArrayList<RecipeIngredientData>(10);
        recipeData.add(new RecipeIngredientData(
                resultItemResourceLocation.toString(),
                recipe.getResultItem(level.registryAccess()).getItem().getName(ItemStack.EMPTY).getString(),
                String.format("/img/items/%s.png", resultItemResourceLocation.getPath()),
                recipe.getResultItem(level.registryAccess()).getItem())
        );
        if (recipe instanceof ShapedRecipe shapedRecipe && shapedRecipe.pattern.width() < 3) {
            var ingredients = recipe.getIngredients();
            for (int i = 0; i < ingredients.size(); i++) {
                handleIngredient(ingredients.get(i), recipeData, recipe);
                //assume spaces are only on the right, never left or middle
                if ((i + 1) % shapedRecipe.pattern.width() == 0) {
                    recipeData.add(RecipeIngredientData.EMPTY);
                }

            }
        } else {
            recipe.getIngredients().forEach(ingredient -> {
                handleIngredient(ingredient, recipeData, recipe);
            });
        }
        return recipeData;
    }

    private static @Nullable Recipe getRecipeFor(CommandSourceStack sourceStack, Item item) {
        for (RecipeHolder<?> recipe : sourceStack.getRecipeManager().getRecipes()) {
            if (recipe.value().getResultItem(level.registryAccess()).is(item)) {
                return recipe.value();
            }
        }
        return null;
    }

    private static void handleArtisanScrollEntry(StringBuilder curioBuilder, Set<Item> itemsTracked, CommandSourceStack source) {
        var item = ItemRegistry.RECIPE.get();
        itemsTracked.add(item);
        var itemResource = BuiltInRegistries.ITEM.getKey(item);
        var name = item.getName(ItemStack.EMPTY).getString();
        appendToBuilder3(curioBuilder, name, itemResource, "All",
                "Artisan Scrolls can be found, looted, or traded for, and can be consumed to learn a new jewelry pattern."
        );

    }

    private static String postProcess(StringBuilder sb) {
        return sb.toString()/*
                .replace("netherite_spell_book.png", "netherite_spell_book.gif")*/;
    }

    private static String getTooltip(ServerPlayer player, ItemStack itemStack) {
        return Arrays.stream(itemStack.getTooltipLines(Item.TooltipContext.EMPTY, player, TooltipFlag.Default.NORMAL)
                        .stream()
                        .skip(1) //First component is always the name. Ignore it
                        .map(Component::getString)
                        .filter(x -> x.trim().length() > 0)
                        .collect(Collectors.joining(", "))
                        .replace(":,", ": ")
                        .replace("  ", " ")
                        .split(","))
                .filter(item -> !item.contains("Slot"))
                .collect(Collectors.joining(","))
                .trim()
                .replace(":", ":<br>");
    }

    private static void appendToBuilder(StringBuilder sb, Recipe recipe, List<RecipeIngredientData> recipeIngredientData, String group, String tooltip) {
        sb.append(String.format(RECIPE_DATA_TEMPLATE,
                getRecipeDataAtIndex(recipeIngredientData, 0).id,
                getRecipeDataAtIndex(recipeIngredientData, 0).name,
                getRecipeDataAtIndex(recipeIngredientData, 0).path,
                group,
                recipe.getType(),
                getRecipeDataAtIndex(recipeIngredientData, 1).id,
                getRecipeDataAtIndex(recipeIngredientData, 1).name,
                getRecipeDataAtIndex(recipeIngredientData, 1).path,
                getRecipeDataAtIndex(recipeIngredientData, 2).id,
                getRecipeDataAtIndex(recipeIngredientData, 2).name,
                getRecipeDataAtIndex(recipeIngredientData, 2).path,
                getRecipeDataAtIndex(recipeIngredientData, 3).id,
                getRecipeDataAtIndex(recipeIngredientData, 3).name,
                getRecipeDataAtIndex(recipeIngredientData, 3).path,
                getRecipeDataAtIndex(recipeIngredientData, 4).id,
                getRecipeDataAtIndex(recipeIngredientData, 4).name,
                getRecipeDataAtIndex(recipeIngredientData, 4).path,
                getRecipeDataAtIndex(recipeIngredientData, 5).id,
                getRecipeDataAtIndex(recipeIngredientData, 5).name,
                getRecipeDataAtIndex(recipeIngredientData, 5).path,
                getRecipeDataAtIndex(recipeIngredientData, 6).id,
                getRecipeDataAtIndex(recipeIngredientData, 6).name,
                getRecipeDataAtIndex(recipeIngredientData, 6).path,
                getRecipeDataAtIndex(recipeIngredientData, 7).id,
                getRecipeDataAtIndex(recipeIngredientData, 7).name,
                getRecipeDataAtIndex(recipeIngredientData, 7).path,
                getRecipeDataAtIndex(recipeIngredientData, 8).id,
                getRecipeDataAtIndex(recipeIngredientData, 8).name,
                getRecipeDataAtIndex(recipeIngredientData, 8).path,
                getRecipeDataAtIndex(recipeIngredientData, 9).id,
                getRecipeDataAtIndex(recipeIngredientData, 9).name,
                getRecipeDataAtIndex(recipeIngredientData, 9).path,
                tooltip
        ));
    }

    private static void appendToBuilder2(StringBuilder sb, String name, ResourceLocation itemResource, String tooltip) {
        sb.append(String.format(RECIPE_DATA_TEMPLATE,
                itemResource.toString(),
                name,
                String.format("/img/items/%s.png", itemResource.getPath()),
                "",
                "none",
                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", tooltip
        ));
    }

    private static void appendToBuilder3(StringBuilder sb, String name, ResourceLocation itemResource, String group, String tooltip) {
        sb.append(String.format(RECIPE_DATA_TEMPLATE,
                itemResource.toString(),
                name,
                String.format("/img/items/%s.png", itemResource.getPath()),
                group,
                "none",
                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", tooltip
        ));
    }

    private static void handleIngredient(Ingredient ingredient, ArrayList<RecipeIngredientData> recipeData, Recipe recipe) {

        Arrays.stream(ingredient.getItems())
                .findFirst()
                .ifPresentOrElse(itemStack -> {
                    var itemResource = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
                    var path = "";

                    if (itemResource.toString().contains(IronsJewelry.MODID)) {
                        path = String.format("/img/items/%s.png", itemResource.getPath());
                    } else {
                        path = String.format("/img/items/minecraft/%s.png", itemResource.getPath());
                    }
                    recipeData.add(new RecipeIngredientData(
                            itemResource.toString(),
                            itemStack.getItem().getName(ItemStack.EMPTY).getString(),
                            path,
                            recipe.getResultItem(level.registryAccess()).getItem()));

                }, () -> {
                    recipeData.add(RecipeIngredientData.EMPTY);
                });
    }

    private static RecipeIngredientData getRecipeDataAtIndex(List<RecipeIngredientData> recipeIngredientData, int index) {
        if (index < recipeIngredientData.size()) {
            return recipeIngredientData.get(index);
        } else {
            return RecipeIngredientData.EMPTY;
        }
    }

    private record RecipeIngredientData(String id, String name, String path, Item item) {
        public static RecipeIngredientData EMPTY = new RecipeIngredientData("", "", "", null);
    }

    private static String rasterizeTranslation(String descriptionId) {
        return handleCapitalization(Component.translatable(descriptionId).getString());
    }

    private static String parseAllowedMaterialsLabel(ResourceLocation partId) {
        var rootOpt = loadRegistryDataJson(partId, "part");
        if (rootOpt.isEmpty()) {
            return "Any";
        }
        try {
            JsonObject root = rootOpt.get();
            if (!root.has("allowedMaterials")) {
                return "Any";
            }
            LinkedHashSet<String> labels = new LinkedHashSet<>();
            parseHolderSetValues(root.get("allowedMaterials"), labels);
            return labels.isEmpty() ? "Any" : String.join(", ", labels);
        } catch (Exception e) {
            IronsJewelry.LOGGER.debug("Failed to parse part registry json {}: {}", partId, e.getMessage());
            return "Any";
        }
    }

    private static Optional<JsonObject> loadRegistryDataJson(ResourceLocation id, String registryPath) {
        if (level == null || level.getServer() == null) {
            return Optional.empty();
        }
        ResourceLocation dataPath = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "irons_jewelry/" + registryPath + "/" + id.getPath() + ".json");
        var resourceOpt = level.getServer().getResourceManager().getResource(dataPath);
        if (resourceOpt.isEmpty()) {
            return Optional.empty();
        }
        try (var reader = resourceOpt.get().openAsReader()) {
            return Optional.of(JsonParser.parseReader(reader).getAsJsonObject());
        } catch (Exception e) {
            IronsJewelry.LOGGER.debug("Failed to load data json {}: {}", dataPath, e.getMessage());
            return Optional.empty();
        }
    }

    private static void parseHolderSetValues(JsonElement element, Set<String> outLabels) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonPrimitive()) {
            outLabels.add(holderSetEntryLabel(element.getAsString()));
            return;
        }
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(value -> parseHolderSetValues(value, outLabels));
            return;
        }
        if (!element.isJsonObject()) {
            return;
        }
        JsonObject object = element.getAsJsonObject();
        if (object.has("values")) {
            parseHolderSetValues(object.get("values"), outLabels);
        }
        if (object.has("tags")) {
            parseHolderSetValues(object.get("tags"), outLabels);
        }
    }

    private static String holderSetEntryLabel(String entry) {
        if (entry.startsWith("#")) {
            ResourceLocation tagId = ResourceLocation.parse(entry.substring(1));
            return handleCapitalization(tagId.getPath().replace("_", " "));
        }
        ResourceLocation id = ResourceLocation.parse(entry);
        return handleCapitalization(id.getPath().replace("_", " "));
    }

    private static int sortIngredientStack(ItemStack a, ItemStack b) {
        return prioritizeCompare(BuiltInRegistries.ITEM.getKey(a.getItem()).getNamespace(), BuiltInRegistries.ITEM.getKey(a.getItem()).getNamespace(), "irons_jewelry");
    }

    private static int prioritizeCompare(String a, String b, String priority) {
        if (a.equals(priority)) {
            return -1;
        } else if (b.equals(priority)) {
            return 1;
        } else {
            return a.compareTo(b);
        }
    }

    private static void generateMaterialData(CommandSourceStack source) {
        try {
            var registry = IronsJewelryRegistries.materialRegistry(source.registryAccess());

            var sb = new StringBuilder();

            for (MaterialDefinition material : registry) {
                var name = rasterizeTranslation(material.descriptionId());
                var id = registry.wrapAsHolder(material).getKey().location();
                if (id.equals(IronsJewelry.id("example"))) {
                    continue;
                }
                if (material.isIngredientEmpty()) {
                    IronsJewelry.LOGGER.error("Cannot generate material {}, no valid ingredients present!", id);
                    continue;
                }
                ItemStack representativeStack = material.getIngredientItems().map(ItemStack::new).sorted(GenerateSiteData::sortIngredientStack).findFirst().get();
                var ingrId = BuiltInRegistries.ITEM.getKey(representativeStack.getItem());
                var imgid = ingrId.getPath();
                var sortOrder = (int) name.charAt(0);
                var modsource = "Vanilla";
                if (ingrId.getNamespace().equals("irons_jewelry")) {
                    modsource = "Gems 'n Jewelry";
                    sortOrder += 100;
                } else if (!ingrId.getNamespace().equals("minecraft")) {
                    modsource = "Requires Addon";
                    sortOrder += 1000;
                }
                var group = "Other";
                var materialHolder = registry.wrapAsHolder(material);
                if (materialHolder.is(JewelryModTags.GEM)) {
                    group = "Gem";
                } else if (materialHolder.is(JewelryModTags.METAL)) {
                    group = "Metal";
                }
                var quality = material.quality();
                var bonusTypes = material.bonusParameters().keySet().stream().map(param -> handleCapitalization(IronsJewelryRegistries.PARAMETER_TYPE_REGISTRY.getKey(param).getPath().replace("_", " "))).collect(Collectors.joining(";"));
                var bonusValues = material.bonusParameters().entrySet().stream().map(entry ->
                        ((IBonusParameterType) entry.getKey()).getSimpleDescription(entry.getValue())).filter(Optional::isPresent).map(opt -> ((Component) opt.get()).getString()).collect(Collectors.joining(";"));

                sb.append(String.format(MATERIAL_DATA_TEMPLATE,
                        name,
                        imgid,
                        modsource,
                        group,
                        quality,
                        bonusTypes,
                        bonusValues,
                        sortOrder)
                );
                if (!ingrId.getNamespace().equals("minecraft") && !ingrId.getNamespace().equals("irons_jewelry") && ingrId.getPath().contains("ingot")) {
                    try {
                        Path filePath = Path.of("site_data/wiki_ingot_base.png");
                        if (Files.notExists(filePath, LinkOption.NOFOLLOW_LINKS)) {
                            throw new FileNotFoundException("No base ingot file");
                        }
                        NativeImage baseIngot = NativeImage.read(Files.newInputStream(filePath));
                        NativeImage ingot = baseIngot.mappedCopy(createPaletteMapping(
                                PalettedPermutations.loadPaletteEntryFromImage(Minecraft.getInstance().getResourceManager(), IronsJewelry.id("palettes/gold")),
                                PalettedPermutations.loadPaletteEntryFromImage(Minecraft.getInstance().getResourceManager(), material.paletteLocation())
                        ));
                        exportNativeImage(ingot, "ingredient/" + ingrId.getPath());
                    } catch (Exception exception) {
                        IronsJewelry.LOGGER.debug("Failed to make image file {}: {}", ingrId, exception.getMessage());
                    }
                }
            }

            var file = new BufferedWriter(new FileWriter("site_data/material_data.yml"));
            file.write(sb.toString());
            file.close();

        } catch (Exception e) {
            IronsJewelry.LOGGER.debug(e.getMessage());
        }
    }

    private static void generatePatternData(CommandSourceStack source) {
        try {
            var registry = IronsJewelryRegistries.patternRegistry(source.registryAccess());

            var sb = new StringBuilder();

            registry.stream()
//                    .filter(st -> (st.isEnabled() && st != SpellRegistry.none()))
                    .forEach(pattern -> {
                        var name = rasterizeTranslation(pattern.descriptionId());
                        var imgid = registry.wrapAsHolder(pattern).getKey().location().getPath();
                        var locked = pattern.unlockedByDefault() ? "Yes" : "No";
                        var partForQuality = pattern.partForQuality().map(part -> rasterizeTranslation(part.value().descriptionId())).orElse("None");
                        var quality = pattern.qualityMultiplier();
                        var parts = pattern.partTemplate().stream().map(part -> String.format("%s (%s - %s)",
                                rasterizeTranslation(part.part().value().descriptionId()),
                                part.materialCost(),
                                parseAllowedMaterialsLabel(part.part().getKey().location()))).toList();
                        var part1 = parts.size() >= 1 ? parts.get(0) : "";
                        var part2 = parts.size() >= 2 ? parts.get(1) : "";
                        var part3 = parts.size() >= 3 ? parts.get(2) : "";
                        var part4 = parts.size() >= 4 ? parts.get(3) : "";
                        var bonuses = pattern.getPatternBonusesTooltip();
                        if (!bonuses.isEmpty()) {
                            bonuses.removeFirst(); // remove header
                        }
                        var bonus1 = bonuses.size() >= 1 ? bonuses.get(0).getString() : "";
                        var bonus2 = bonuses.size() >= 2 ? bonuses.get(1).getString() : "";
                        var bonus3 = bonuses.size() >= 3 ? bonuses.get(2).getString() : "";
                        var bonus4 = bonuses.size() >= 4 ? bonuses.get(3).getString() : "";

                        sb.append(String.format(PATTERN_DATA_TEMPLATE,
                                name,
                                imgid,
                                locked,
                                partForQuality,
                                quality,
                                part1,
                                part2,
                                part3,
                                part4,
                                bonus1,
                                bonus2,
                                bonus3,
                                bonus4)
                        );
                        tryGeneratePatternImage(source, pattern, imgid);
                    });

            var file = new BufferedWriter(new FileWriter("site_data/pattern_data.yml"));
            file.write(sb.toString());
            file.close();

        } catch (Exception e) {
            IronsJewelry.LOGGER.debug(e.getMessage());
        }
    }

    private static void tryGeneratePatternImage(CommandSourceStack source, PatternDefinition pattern, String imgid) {
        try {
            var materialRegistry = IronsJewelryRegistries.materialRegistry(source.registryAccess());
            var metal = materialRegistry.getHolder(IronsJewelry.id("gold")).get();
            var gem = materialRegistry.getHolder(IronsJewelry.id("ruby")).get();
            NativeImage image = new NativeImage(16, 16, false);
            pattern.partTemplate().stream().map(PartIngredient::part).forEach(part -> {
                Holder<MaterialDefinition> renderMaterial = null;
                if (part.value().canUseMaterial(gem)) {
                    renderMaterial = gem;
                } else if (part.value().canUseMaterial(metal)) {
                    renderMaterial = metal;
                } else {
                    for (Holder.Reference<MaterialDefinition> materialDefinition : materialRegistry.holders().toList()) {
                        if (part.value().canUseMaterial(materialDefinition)) {
                            renderMaterial = materialDefinition;
                            break;
                        }
                    }
                    Objects.requireNonNull(renderMaterial);
                }
                var sprite = AssetHandlerRegistry.JEWELRY_HANDLER.get().getSprite(AssetHandlerRegistry.JEWELRY_HANDLER.get().getSpriteLocation(part, renderMaterial));
                var layer = sprite.contents().getOriginalImage();
                var pixels = layer.getPixelsRGBA();
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        int i = y * 16 + x;
                        int rgba = pixels[i];
                        int alpha = (rgba >> 24) & 0xFF;
                        if (alpha != 0) {
                            image.setPixelRGBA(x, y, rgba);
                        }
                    }
                }
            });
            exportNativeImage(image, imgid);
        } catch (Exception e) {
            IronsJewelry.LOGGER.debug("Failed to make image file: {} {}", pattern.descriptionId(), e.getMessage());
        }
    }

    public static void exportNativeImage(NativeImage image, String name) throws IOException {
        String fileName = name;
        if (!fileName.endsWith(".png")) //Texture atlas name already ends with .png
        {
            fileName += ".png";
        }

        Path filePath = Path.of("site_data/img").resolve(fileName);
        if (Files.notExists(filePath, LinkOption.NOFOLLOW_LINKS)) {
            Files.createFile(filePath);
        }
        image.writeToFile(filePath);
    }

    private static List<String> processUniqueInfo(AbstractSpell spell) {
        List<String> text = new ArrayList<>();
        var uniqueInfoMin = spell.getUniqueInfo(spell.getMinLevel(), null);
        var uniqueInfoMax = spell.getUniqueInfo(spell.getMaxLevel(), null);
        for (int i = 0; i < uniqueInfoMax.size(); i++) {
            var splitMin = uniqueInfoMin.get(i).getString().split(" ");
            var splitMax = uniqueInfoMax.get(i).getString().split(" ");
            int k = -1;
            for (int j = 0; j < splitMin.length; j++) {
                if (splitMin[j].matches("\\d\\.?\\d*(s|m|%)*")) {
                    k = j;
                    break;
                }
            }
            if (k >= 0 && !splitMin[k].equals(splitMax[k])) {
                text.add(String.format(uniqueInfoMin.get(i).getString().replaceFirst(splitMin[k], "%s"), String.format("%s-%s", splitMin[k], splitMax[k])));
            } else {
                text.add(uniqueInfoMin.get(i).getString());
            }
        }
        return text;
    }

    public static String handleCapitalization(String input) {
        return Arrays.stream(input.split("[ |_]"))
                .map(word -> {
                    if (word.equals("spell")) {
                        return "";
                    } else {
                        var first = word.substring(0, 1);
                        var rest = word.substring(1);
                        return first.toUpperCase() + rest;
                    }
                })
                .collect(Collectors.joining(" "))
                .trim();
    }

    private static IntUnaryOperator createPaletteMapping(int[] p_266839_, int[] p_266776_) {
        if (p_266776_.length != p_266839_.length) {
            throw new IllegalArgumentException();
        } else {
            Int2IntMap int2intmap = new Int2IntOpenHashMap(p_266776_.length);

            for (int i = 0; i < p_266839_.length; i++) {
                int j = p_266839_[i];
                if (FastColor.ABGR32.alpha(j) != 0) {
                    int2intmap.put(FastColor.ABGR32.transparent(j), p_266776_[i]);
                }
            }

            return p_267899_ -> {
                int k = FastColor.ABGR32.alpha(p_267899_);
                if (k == 0) {
                    return p_267899_;
                } else {
                    int l = FastColor.ABGR32.transparent(p_267899_);
                    int i1 = int2intmap.getOrDefault(l, FastColor.ABGR32.opaque(l));
                    int j1 = FastColor.ABGR32.alpha(i1);
                    return FastColor.ABGR32.color(k * j1 / 255, i1);
                }
            };
        }
    }

    private enum CraftingType {
        CRAFTING_TABLE,
        SMITHING_TABLE,
        NOT_CRAFTABLE
    }
}