package io.redspace.ironsjewelry.data_registry;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.BonusSource;
import io.redspace.ironsjewelry.core.Pattern;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class PatternDataHandler extends SimpleJsonResourceReloadListener {
    private static Map<ResourceLocation, Pattern> INSTANCE;
    public static final Codec<Pattern> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.list(PartIngredient.CODEC).fieldOf("requiredParts").forGetter(Pattern::partTemplate),
            Codec.list(BonusSource.CODEC).fieldOf("bonuses").forGetter(Pattern::bonuses),
            Codec.BOOL.fieldOf("unlockedByDefault").forGetter(Pattern::unlockedByDefault)
    ).apply(builder, Pattern::new));

    public PatternDataHandler() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "irons_jewelry/patterns");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        ImmutableMap.Builder<ResourceLocation, Pattern> builder = ImmutableMap.builder();
        RegistryOps<JsonElement> registryops = this.makeConditionalOps(); // Neo: add condition context

        for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if (resourcelocation.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.

            try {
                var decoded = CODEC.parse(registryops, entry.getValue()).getOrThrow(JsonParseException::new);
                builder.put(resourcelocation, decoded);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                IronsJewelry.LOGGER.error("Parsing error loading pattern {}", resourcelocation, jsonparseexception);
            }
        }

        INSTANCE = builder.build();
    }
}
