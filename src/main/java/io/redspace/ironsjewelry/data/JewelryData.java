package io.redspace.ironsjewelry.data;

import io.redspace.ironsjewelry.core.AbstractPattern;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

/**
 * @param pattern Pattern tells the item what parts to expect, and how to assemble the piece of jewerly based on those parts
 * @param parts   Map of PartData ids to PartInstance
 */
public record JewelryData(AbstractPattern pattern, Map<ResourceLocation, PartInstance> parts) {

    //TODO: hash code implementation

    @Override
    public int hashCode() {
        return 0;
    }

    public Optional<PartInstance> getFirstPart() {
        if (parts.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(parts.get(pattern.template().values().stream().findFirst().get().partId()));
        }
    }
}
