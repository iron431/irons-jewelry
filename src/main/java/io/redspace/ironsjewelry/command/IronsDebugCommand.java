package io.redspace.ironsjewelry.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.loot.LootInjectionHandler;
import io.redspace.ironsjewelry.registry.*;
import joptsimple.internal.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class IronsDebugCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_imbued_sword.failed"));
    private static final SuggestionProvider<CommandSourceStack> PATTERN_SUGGESTIONS = (context, builder) -> {
        var registry = IronsJewelryRegistries.patternRegistry(context.getSource().registryAccess());
        var resources = registry.stream()
                .map(registry::getKey)
                .collect(Collectors.toSet());
        return SharedSuggestionProvider.suggestResource(resources, builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {

        var command = Commands.literal("ironsJewelry").requires((p_138819_) -> p_138819_.hasPermission(2)).
                then(Commands.literal("learnPattern")
                        .then(Commands.argument("pattern", PatternCommandArgument.patternArgument()).suggests(PATTERN_SUGGESTIONS).executes((commandContext) -> learnPattern(commandContext.getSource(), commandContext.getArgument("pattern", String.class)))).then(Commands.literal("all").executes(context -> learnAllPatterns(context.getSource())))
                        .then(Commands.literal("unlearnAll").executes(context -> unlearnAllPatterns(context.getSource()))))
                .then(Commands.literal("createPatternItem").then(Commands.argument("pattern", PatternCommandArgument.patternArgument()).suggests(PATTERN_SUGGESTIONS).executes((commandContext) -> {
                            return createPatternItem(commandContext.getSource(), commandContext.getArgument("pattern", String.class));
                        }))
                );

        if (!FMLLoader.isProduction()) {
            command.then(Commands.literal("debug")
                    .then(Commands.literal("countCombos").executes((commandContext) -> enumerateCombos(commandContext.getSource())))
                    .then(Commands.literal("generateSiteData").executes((commandContext) -> GenerateSiteData.generateSiteData(commandContext.getSource())))
                    .then(Commands.literal("exportHeldItem").executes((commandContext) -> exportHeldItem(commandContext.getSource())))
                    .then(Commands.literal("lootTracker").executes((commandContext) -> dumpLootInfo(commandContext.getSource())))
                    .then(Commands.literal("compareMaterials").executes((commandContext) -> compareMaterialData(commandContext.getSource())))
            );
        }
        pDispatcher.register(
                command
        );
    }

    private static int enumerateCombos(CommandSourceStack source) {
        var registry = source.registryAccess();
        int total = 0;
        var materials = IronsJewelryRegistries.materialRegistry(registry);
        for (PatternDefinition patternDefinition : IronsJewelryRegistries.patternRegistry(registry)) {
            List<Integer> lengths = new ArrayList<>();
            for (PartIngredient part : patternDefinition.partTemplate()) {
                int option = 0;
                for (MaterialDefinition materialDefinition : materials) {
                    if (part.part().value().canUseMaterial(materialDefinition.materialType())) {
                        option++;
                    }
                }
                lengths.add(option);
            }
            //take cartesian product of the possible options for each part entry
            int subtotal = lengths.getFirst();
            for (int i = 1; i < lengths.size(); i++) {
                subtotal *= lengths.get(i);
            }
            total += subtotal;
        }
        source.sendSystemMessage(Component.literal(String.valueOf(total)));
        return total;
    }

    private static int createPatternItem(CommandSourceStack source, String patternId) throws CommandSyntaxException {
        if (!patternId.contains(":")) {
            patternId = IronsJewelry.MODID + ":" + patternId;
        }

        var registry = IronsJewelryRegistries.patternRegistry(source.registryAccess());
        var pattern = registry.getHolder(ResourceLocation.parse(patternId));
        if (pattern.isPresent()) {
            var serverPlayer = source.getPlayer();
            ItemStack stack = new ItemStack(ItemRegistry.RECIPE.get());
            stack.set(ComponentRegistry.STORED_PATTERN, pattern.get());
            serverPlayer.getInventory().add(stack);
            return 1;
        }

        throw ERROR_FAILED.create();
    }

    private static int learnPattern(CommandSourceStack source, String patternId) throws CommandSyntaxException {
        if (!patternId.contains(":")) {
            patternId = IronsJewelry.MODID + ":" + patternId;
        }

        var registry = IronsJewelryRegistries.patternRegistry(source.registryAccess());
        var pattern = registry.get(ResourceLocation.parse(patternId));
        if (pattern != null) {
            var serverPlayer = source.getPlayer();
            if (serverPlayer != null) {
                return serverPlayer.getData(DataAttachmentRegistry.PLAYER_DATA).learnAndSync(serverPlayer, registry.wrapAsHolder(pattern)) ? 1 : 0;
            }

        }

        throw ERROR_FAILED.create();
    }

    private static int learnAllPatterns(CommandSourceStack source) throws CommandSyntaxException {
        var serverPlayer = source.getPlayer();
        if (serverPlayer != null) {
            var data = serverPlayer.getData(DataAttachmentRegistry.PLAYER_DATA);
            var registry = IronsJewelryRegistries.patternRegistry(source.registryAccess());
            for (Map.Entry<ResourceKey<PatternDefinition>, PatternDefinition> entry : registry.entrySet()) {
                data.learn(registry.wrapAsHolder(entry.getValue()));
            }
            data.sync(serverPlayer);
            return 1;
        }

        throw ERROR_FAILED.create();
    }

    private static int unlearnAllPatterns(CommandSourceStack source) throws CommandSyntaxException {
        var serverPlayer = source.getPlayer();
        if (serverPlayer != null) {
            var data = serverPlayer.getData(DataAttachmentRegistry.PLAYER_DATA);
            data.getLearnedPatterns().clear();
            data.sync(serverPlayer);
            return 1;
        }

        throw ERROR_FAILED.create();
    }

    private static int dumpLootInfo(CommandSourceStack source) throws CommandSyntaxException {
        var serverPlayer = source.getPlayer();
        if (serverPlayer != null) {
            int maxLength = 0;
            var entries = LootInjectionHandler.TRACKED_LOOT_TABLES.entrySet();
            for (var entry : entries) {
                if (entry.getKey().toString().length() > maxLength) {
                    maxLength = entry.getKey().toString().length();
                }
            }
            for (var entry : entries) {
                String message = entry.getKey().toString();
                message = message + Strings.repeat('.', maxLength + 3 - message.length()) + String.valueOf(entry.getValue());
                serverPlayer.sendSystemMessage(Component.literal(message));
            }

            return 1;
        }

        throw ERROR_FAILED.create();
    }

    private static int exportHeldItem(CommandSourceStack source) throws CommandSyntaxException {
        var serverPlayer = source.getPlayer();
        if (serverPlayer != null) {
            var jewelry = JewelryData.get(serverPlayer.getMainHandItem());
            if (jewelry.isValid()) {
                try {
                    NativeImage image = new NativeImage(16, 16, false);
                    jewelry.pattern().value().partTemplate().stream().map(PartIngredient::part).forEach(part -> {
                        var material = jewelry.parts().get(part);
                        var sprite = AssetHandlerRegistry.JEWELRY_HANDLER.get().getSprite(AssetHandlerRegistry.JEWELRY_HANDLER.get().getSpriteLocation(part, material));
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

                    var fileName = serverPlayer.getMainHandItem().getHoverName().getString().toLowerCase(Locale.ENGLISH).chars().mapToObj(i -> ResourceLocation.isAllowedInResourceLocation((char) i) ? String.valueOf((char) i) : "_").collect(Collectors.joining()) + ".png";
                    Path dirPath = Path.of("screenshots/irons_jewelry");
                    Path filePath = dirPath.resolve(fileName);
                    if (Files.notExists(dirPath)) {
                        Files.createDirectories(dirPath);  // creates all nonexistent parent directories
                    }
                    if (Files.notExists(filePath)) {
                        Files.createFile(filePath);  // creates the actual file
                    }
                    image.writeToFile(filePath);
                    Component component = Component.literal("Exported " + fileName)
                            .withStyle(ChatFormatting.UNDERLINE)
                            .withStyle(p_168608_ -> p_168608_.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, filePath.toString())));
                    serverPlayer.sendSystemMessage(component);

                } catch (Exception e) {
                    serverPlayer.sendSystemMessage(Component.literal("Failed to make image file: " + e.getMessage()));
                }
                return 1;
            }
        }

        throw ERROR_FAILED.create();
    }

    private static int compareMaterialData(CommandSourceStack source) {
        var projectRoot = findProjectRoot();
        if (projectRoot == null) {
            source.sendSystemMessage(Component.literal("Could not locate project root (looked for src/main/resources/)").withStyle(ChatFormatting.RED));
            return 0;
        }

        var sourceDir = projectRoot.resolve("src/main/resources/data/irons_jewelry/irons_jewelry/material_old");
        var generatedDir = projectRoot.resolve("src/generated/resources/data/irons_jewelry/irons_jewelry/material");

        if (!Files.isDirectory(sourceDir)) {
            source.sendSystemMessage(Component.literal("Source material directory not found: " + sourceDir).withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!Files.isDirectory(generatedDir)) {
            source.sendSystemMessage(Component.literal("Generated material directory not found: " + generatedDir).withStyle(ChatFormatting.RED));
            source.sendSystemMessage(Component.literal("Run datagen first.").withStyle(ChatFormatting.YELLOW));
            return 0;
        }

        source.sendSystemMessage(Component.literal("Comparing material JSON files...").withStyle(ChatFormatting.GRAY));
        source.sendSystemMessage(Component.literal("  Source:    " + sourceDir).withStyle(ChatFormatting.GRAY));
        source.sendSystemMessage(Component.literal("  Generated: " + generatedDir).withStyle(ChatFormatting.GRAY));

        try {
            var sourceFiles = collectJsonFiles(sourceDir);
            var generatedFiles = collectJsonFiles(generatedDir);

            var allNames = new TreeSet<String>();
            allNames.addAll(sourceFiles.keySet());
            allNames.addAll(generatedFiles.keySet());

            int matches = 0;
            int mismatches = 0;

            for (var name : allNames) {
                var sourcePath = sourceFiles.get(name);
                var generatedPath = generatedFiles.get(name);

                if (sourcePath == null) {
                    source.sendSystemMessage(Component.literal("  [+] " + name + " (generated only)").withStyle(ChatFormatting.YELLOW));
                    mismatches++;
                    continue;
                }
                if (generatedPath == null) {
                    source.sendSystemMessage(Component.literal("  [-] " + name + " (source only, not generated)").withStyle(ChatFormatting.YELLOW));
                    mismatches++;
                    continue;
                }

                var sourceJson = JsonParser.parseString(Files.readString(sourcePath));
                var generatedJson = JsonParser.parseString(Files.readString(generatedPath));

                if (sourceJson.equals(generatedJson)) {
                    matches++;
                } else {
                    source.sendSystemMessage(Component.literal("  [!=] " + name).withStyle(ChatFormatting.RED));
                    reportJsonDiff(source, sourceJson, generatedJson, "      ");
                    mismatches++;
                }
            }

            var color = mismatches == 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
            source.sendSystemMessage(Component.literal(
                    String.format("Material comparison complete: %d/%d matched", matches, allNames.size())
            ).withStyle(color));
            return mismatches == 0 ? 1 : 0;

        } catch (IOException e) {
            source.sendSystemMessage(Component.literal("Error reading files: " + e.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static Path findProjectRoot() {
        var current = Path.of("").toAbsolutePath();
        for (int i = 0; i < 5; i++) {
            if (Files.isDirectory(current.resolve("src/main/resources"))) {
                return current;
            }
            current = current.getParent();
            if (current == null) break;
        }
        return null;
    }

    private static Map<String, Path> collectJsonFiles(Path dir) throws IOException {
        try (var stream = Files.list(dir)) {
            return stream.filter(p -> p.toString().endsWith(".json"))
                    .collect(Collectors.toMap(p -> p.getFileName().toString(), p -> p));
        }
    }

    private static void reportJsonDiff(CommandSourceStack source, JsonElement sourceElem, JsonElement generatedElem, String indent) {
        if (sourceElem.equals(generatedElem)) return;

        if (sourceElem.isJsonObject() && generatedElem.isJsonObject()) {
            var sourceObj = sourceElem.getAsJsonObject();
            var generatedObj = generatedElem.getAsJsonObject();
            var allKeys = new TreeSet<String>();
            sourceObj.keySet().forEach(allKeys::add);
            generatedObj.keySet().forEach(allKeys::add);

            for (var key : allKeys) {
                var sv = sourceObj.get(key);
                var gv = generatedObj.get(key);
                if (sv == null) {
                    source.sendSystemMessage(Component.literal(indent + key + ": absent in source, generated=" + gv).withStyle(ChatFormatting.YELLOW));
                } else if (gv == null) {
                    source.sendSystemMessage(Component.literal(indent + key + ": absent in generated, source=" + sv).withStyle(ChatFormatting.YELLOW));
                } else if (!sv.equals(gv)) {
                    if ((sv.isJsonObject() && gv.isJsonObject()) || (sv.isJsonArray() && gv.isJsonArray())) {
                        source.sendSystemMessage(Component.literal(indent + key + ":").withStyle(ChatFormatting.RED));
                        reportJsonDiff(source, sv, gv, indent + "  ");
                    } else {
                        source.sendSystemMessage(Component.literal(indent + key + ": source=" + sv + " | generated=" + gv).withStyle(ChatFormatting.RED));
                    }
                }
            }
        } else if (sourceElem.isJsonArray() && generatedElem.isJsonArray()) {
            var sourceArr = sourceElem.getAsJsonArray();
            var generatedArr = generatedElem.getAsJsonArray();
            int maxLen = Math.max(sourceArr.size(), generatedArr.size());
            for (int i = 0; i < maxLen; i++) {
                if (i >= sourceArr.size()) {
                    source.sendSystemMessage(Component.literal(indent + "[" + i + "]: absent in source, generated=" + generatedArr.get(i)).withStyle(ChatFormatting.YELLOW));
                } else if (i >= generatedArr.size()) {
                    source.sendSystemMessage(Component.literal(indent + "[" + i + "]: absent in generated, source=" + sourceArr.get(i)).withStyle(ChatFormatting.YELLOW));
                } else if (!sourceArr.get(i).equals(generatedArr.get(i))) {
                    source.sendSystemMessage(Component.literal(indent + "[" + i + "]:").withStyle(ChatFormatting.RED));
                    reportJsonDiff(source, sourceArr.get(i), generatedArr.get(i), indent + "  ");
                }
            }
        } else {
            source.sendSystemMessage(Component.literal(indent + "type mismatch: source=" + sourceElem + " | generated=" + generatedElem).withStyle(ChatFormatting.RED));
        }
    }
}
