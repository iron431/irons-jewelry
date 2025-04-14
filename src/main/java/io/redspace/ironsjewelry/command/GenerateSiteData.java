package io.redspace.ironsjewelry.command;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.redspace.atlasapi.AtlasApi;
import io.redspace.atlasapi.api.AtlasApiHelper;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import io.redspace.ironsjewelry.registry.AssetHandlerRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.ItemRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Native;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private static final String SPELL_DATA_TEMPLATE = """
            - name: "%s"
              school: "%s"
              icon: "%s"
              level: "%d to %d"
              mana: "%d to %d"
              cooldown: "%ds"
              cast_type: "%s"
              rarity: "%s to %s"
              description: "%s"
              u1: "%s"
              u2: "%s"
              u3: "%s"
              u4: "%s"
              
                    """;

    private static final String PATTERN_DATA_TEMPLATE = """
            - name: "%s"
              icon: "/img/patterns/%s.png"
              locked: "%s by Default"
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

    protected static int generateSiteData(CommandSourceStack source) {
        generateRecipeData(source);

        generatePatternData(source);

        return 1;
    }

    static ServerLevel level;

    public static List<Item> getVisibleItems() {
        return BuiltInRegistries.ITEM.stream().filter(item -> CreativeModeTabs.allTabs().stream().anyMatch(tab -> tab.contains(new ItemStack(item)))).toList();
    }

    private static void generateRecipeData(CommandSourceStack source) {
        try {
            var itemBuilder = new StringBuilder();
            var armorBuilder = new StringBuilder();
            var spellbookBuilder = new StringBuilder();
            var curioBuilder = new StringBuilder();
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

    /**
     * ["A", "B", "C"] -> "A, B, C"
     */
    private static String listListElements(List<?> list) {
        StringBuilder builder = new StringBuilder();
        list.forEach(obj -> builder.append(obj.toString()).append(", "));
        return builder.substring(0, builder.length() - 2);
    }

    private static void generatePatternData(CommandSourceStack source) {
        try {
            var registry = IronsJewelryRegistries.patternRegistry(source.registryAccess());
            var materialRegistry = IronsJewelryRegistries.materialRegistry(source.registryAccess());

            var sb = new StringBuilder();

            registry.stream()
//                    .filter(st -> (st.isEnabled() && st != SpellRegistry.none()))
                    .forEach(pattern -> {
                        var name = rasterizeTranslation(pattern.descriptionId());
                        var imgid = registry.wrapAsHolder(pattern).getKey().location().getPath();
                        var locked = pattern.unlockedByDefault() ? "Unlocked" : "Locked";
                        var partForQuality = pattern.partForQuality().map(part -> rasterizeTranslation(part.value().descriptionId())).orElse("None");
                        var quality = pattern.qualityMultiplier();
                        var parts = pattern.partTemplate().stream().map(part -> String.format("%s (%s - %s)",
                                rasterizeTranslation(part.part().value().descriptionId()),
                                part.materialCost(),
                                handleCapitalization(listListElements(part.part().value().allowedMaterials())))).toList();
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
                        try {
                            NativeImage image = new NativeImage(16, 16, false);
                            pattern.partTemplate().stream().map(PartIngredient::part).forEach(part -> {
                                var metal = materialRegistry.getHolder(IronsJewelry.id("gold")).get();
                                var gem = materialRegistry.getHolder(IronsJewelry.id("ruby")).get();
                                Holder<MaterialDefinition> renderMaterial = null;
                                if (part.value().canUseMaterial("gem")) {
                                    renderMaterial = gem;
                                } else if (part.value().canUseMaterial("metal")) {
                                    renderMaterial = metal;
                                } else {
                                    for (MaterialDefinition materialDefinition : IronsJewelryRegistries.materialRegistry(source.registryAccess())) {
                                        if (part.value().canUseMaterial(materialDefinition.materialType())) {
                                            renderMaterial = IronsJewelryRegistries.materialRegistry(source.registryAccess()).wrapAsHolder(materialDefinition);
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
                    });

            var file = new BufferedWriter(new FileWriter("site_data/pattern_data.yml"));
            file.write(sb.toString());
            file.close();

        } catch (Exception e) {
            IronsJewelry.LOGGER.debug(e.getMessage());
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
        return Arrays.stream(input.toLowerCase().split("[ |_]"))
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

    private enum CraftingType {
        CRAFTING_TABLE,
        SMITHING_TABLE,
        NOT_CRAFTABLE
    }
}