package io.redspace.ironsjewelry.compat;

import io.redspace.ironsjewelry.compat.apothic_attributes.ApothicAttributesProxy;
import io.redspace.ironsjewelry.compat.apothic_attributes.ApothicAttributesProxyImpl;
import io.redspace.ironsjewelry.core.CooldownHandler;
import net.neoforged.fml.ModList;

import java.util.Map;

public class CompatHandler {
    public static ApothicAttributesProxy APOTH_PROXY = ApothicAttributesProxy.NOOP;

    private static final Map<String, Runnable> MOD_MAP = Map.of(
            "apothic_attributes", () -> APOTH_PROXY = new ApothicAttributesProxyImpl(),
            "irons_spellbooks", () -> CooldownHandler.INSTANCE = new ISSCooldownHandler()
    );

    public static void init() {
        MOD_MAP.forEach((modid, supplier) -> {
            if (ModList.get().isLoaded(modid)) {
                supplier.run();
            }
        });
    }
}
