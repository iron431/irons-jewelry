package io.redspace.ironsjewelry.gameplay.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.client.ClientData;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.data_registry.PatternDataHandler;
import io.redspace.ironsjewelry.registry.DataAttachmentRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.stream.Collectors;

public class IronsDebugCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_imbued_sword.failed"));
    private static final SuggestionProvider<CommandSourceStack> PATTERN_SUGGESTIONS = (context, builder) -> {
        var resources = PatternDataHandler.patterns().stream()
                .map(PatternDefinition::id)
                .collect(Collectors.toSet());
        return SharedSuggestionProvider.suggestResource(resources, builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("ironsJewelry").requires((p_138819_) -> {
            return p_138819_.hasPermission(2);
        }).then(Commands.literal("learnPattern").then(Commands.argument("pattern", PatternCommandArgument.patternArgument()).suggests(PATTERN_SUGGESTIONS).executes((commandContext) -> {
            return learnPattern(commandContext.getSource(), commandContext.getArgument("pattern", String.class));
        }))).then(Commands.literal("clearClientCache").executes((commandContext -> {
            ClientData.MODEL_CACHE.clear();
            return 1;
        }))));
    }

    private static int learnPattern(CommandSourceStack source, String patternId) throws CommandSyntaxException {
        if (!patternId.contains(":")) {
            patternId = IronsJewelry.MODID + ":" + patternId;
        }

        var pattern = PatternDataHandler.get(ResourceLocation.parse(patternId));

        var serverPlayer = source.getPlayer();
        if (serverPlayer != null) {
            return serverPlayer.getData(DataAttachmentRegistry.PLAYER_DATA).learn(serverPlayer, pattern) ? 1 : 0;
        }

        throw ERROR_FAILED.create();
    }
}
