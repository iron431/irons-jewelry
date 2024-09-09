package io.redspace.ironsjewelry.client;

import net.minecraft.client.resources.model.BakedModel;

import java.util.HashMap;

public class ClientModelCache {
    //TODO: ensure good memory usage/management
    //TODO: clear cache on resourcepack reload
    //Map of HashCodes to baked geometry. Every unique combination of colors/materials/patterns should generate a unique, deterministic hash code.
    public static final HashMap<Integer, BakedModel> MODEL_CACHE = new HashMap<>();

}
