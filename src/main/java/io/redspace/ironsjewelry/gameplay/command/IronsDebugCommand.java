package io.redspace.ironsjewelry.gameplay.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.client.ClientData;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.registry.DataAttachmentRegistry;
import io.redspace.ironsjewelry.registry.JewelryDataRegistries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.stream.Collectors;

public class IronsDebugCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_imbued_sword.failed"));
    private static final SuggestionProvider<CommandSourceStack> PATTERN_SUGGESTIONS = (context, builder) -> {
        var registry = JewelryDataRegistries.patternRegistry(context.getSource().registryAccess());
        var resources = registry.stream()
                .map(registry::getKey)
                .collect(Collectors.toSet());
        return SharedSuggestionProvider.suggestResource(resources, builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {

        pDispatcher.register(Commands.literal("ironsJewelry").requires((p_138819_) -> {
            return p_138819_.hasPermission(2);
        }).then(Commands.literal("learnPattern").then(Commands.argument("pattern", PatternCommandArgument.patternArgument()).suggests(PATTERN_SUGGESTIONS).executes((commandContext) -> {
            return learnPattern(commandContext.getSource(), commandContext.getArgument("pattern", String.class));
        })).then(Commands.literal("all").executes(context -> learnAllPatterns(context.getSource()))
        ).then(Commands.literal("unlearnAll").executes(context -> unlearnAllPatterns(context.getSource()))
        )).then(Commands.literal("clearClientCache").executes((commandContext -> {
            ClientData.MODEL_CACHE.clear();
            return 1;
        }))));
    }

    private static int learnPattern(CommandSourceStack source, String patternId) throws CommandSyntaxException {
        if (!patternId.contains(":")) {
            patternId = IronsJewelry.MODID + ":" + patternId;
        }

        var registry = JewelryDataRegistries.patternRegistry(source.registryAccess());
        var pattern = registry.get(ResourceLocation.parse(patternId));
        if (pattern != null) {
            var serverPlayer = source.getPlayer();
            if (serverPlayer != null) {
                return serverPlayer.getData(DataAttachmentRegistry.PLAYER_DATA).learn(serverPlayer, registry.wrapAsHolder(pattern)) ? 1 : 0;
            }

        }

        throw ERROR_FAILED.create();
    }

    private static int learnAllPatterns(CommandSourceStack source) throws CommandSyntaxException {
        var serverPlayer = source.getPlayer();
        if (serverPlayer != null) {
            var data = serverPlayer.getData(DataAttachmentRegistry.PLAYER_DATA);
            var registry = JewelryDataRegistries.patternRegistry(source.registryAccess());
            for (Map.Entry<ResourceKey<PatternDefinition>, PatternDefinition> entry : registry.entrySet()) {
                data.learn(serverPlayer, registry.wrapAsHolder(entry.getValue()));
            }
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
}
