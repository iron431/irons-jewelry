package io.redspace.ironsjewelry.compat;

import io.redspace.ironsjewelry.core.CooldownHandler;
import net.neoforged.fml.ModList;

import java.util.Map;

public class CompatHandler {
    private static final Map<String, Runnable> MOD_MAP = Map.of(
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
