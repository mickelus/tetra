package se.mickelus.tetra.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.Gson;
import se.mickelus.mutil.data.DataDistributor;
import se.mickelus.mutil.data.DataStore;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.modifier.ModifierEffect;

import java.util.stream.Stream;

public class ModifierEffectStore extends DataStore<ModifierEffect> {
    public static Multimap<ItemEffect, ModifierEffect> hitDamageModifiers = ArrayListMultimap.create();
    public static Multimap<ItemEffect, ModifierEffect> breakSpeedModifiers = ArrayListMultimap.create();

    public ModifierEffectStore(Gson gson, String namespace, String directory, DataDistributor synchronizer) {
        super(gson, namespace, directory, ModifierEffect.class, synchronizer);
    }

    @Override
    protected void processData() {
        hitDamageModifiers = dataMap.values().stream()
                .filter(data -> data.type().getKey().equals("tetra:hit_damage"))
                .collect(Multimaps.flatteningToMultimap(
                        ModifierEffect::effect,
                        Stream::of,
                        ArrayListMultimap::create));

        breakSpeedModifiers = dataMap.values().stream()
                .filter(data -> data.type().getKey().equals("tetra:break_speed"))
                .collect(Multimaps.flatteningToMultimap(
                        entry -> entry.effect(),
                        Stream::of,
                        ArrayListMultimap::create));
    }
}
