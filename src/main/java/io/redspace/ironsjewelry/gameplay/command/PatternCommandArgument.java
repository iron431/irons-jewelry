package io.redspace.ironsjewelry.gameplay.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;

public class PatternCommandArgument implements ArgumentType<String> {
    public static PatternCommandArgument patternArgument() {
        return new PatternCommandArgument();
    }

    @Override
    public String parse(final StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();

        while (reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
            reader.skip();
        }

        return reader.getString().substring(i, reader.getCursor());
    }

//    @Override
//    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
//        var registry = JewelryDataRegistries.patternRegistry(context.getSource())
//        var registeredSpells = PatternDataHandler.patterns().stream().map(entry -> {
//            if (entry.id().getNamespace().equals(IronsJewelry.MODID)) {
//                return entry.id().getPath();
//            } else {
//                return entry.id().toString();
//            }
//        }).sorted((s1, s2) -> {
//            if (s1.contains(":") && s2.contains(":")) {
//                return s1.compareTo(s2);
//            } else if (s1.contains(":")) {
//                return 1;
//            } else if (s2.contains(":")) {
//                return -1;
//            } else {
//                return s1.compareTo(s2);
//            }
//        }).toList();
//
//        return SharedSuggestionProvider.suggest(registeredSpells, builder);
//    }

    @Override
    public Collection<String> getExamples() {
        return List.of();
    }
}