package io.redspace.ironsjewelry.core.data;

import io.redspace.ironsjewelry.core.Pattern;

import java.util.List;

/**
 * @param pattern Holds the structure for the piece of jewelry, and how its parts yield a bonus
 * @param parts   The part instances this piece of jewelry is made of
 */
public record JewelryData(Pattern pattern, List<PartInstance> parts) {
    @Override
    public int hashCode() {
        return pattern.hashCode() * 31 + parts.hashCode();
    }
}
