package se.mickelus.tetra.client.model;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraftforge.client.model.IQuadTransformer;

import java.util.ArrayList;
import java.util.List;

public class QuadTransformerBuilder {
    private final Int2ObjectMap<List<IQuadTransformer>> transformers;

    public QuadTransformerBuilder() {
        transformers = new Int2ObjectOpenHashMap<>();
    }

    public QuadTransformerBuilder add(int layer, IQuadTransformer transformer) {
        if (!transformers.containsKey(layer)) {
            transformers.put(layer, new ArrayList<>());
        }

        transformers.get(layer).add(transformer);
        return this;
    }

    public Int2ObjectMap<List<IQuadTransformer>> get() {
        return transformers;
    }
}
