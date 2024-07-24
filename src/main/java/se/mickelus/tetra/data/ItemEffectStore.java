package se.mickelus.tetra.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.Gson;
import se.mickelus.mutil.data.DataDistributor;
import se.mickelus.mutil.data.DataStore;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.data.ItemEffectData;

import java.util.stream.Stream;

public class ItemEffectStore extends DataStore<ItemEffectData> {
    public static Multimap<ItemEffect, ItemEffectData> onHitEffects = ArrayListMultimap.create();
    public static Multimap<ItemEffect, ItemEffectData> onMineBlockEffects = ArrayListMultimap.create();
    public static Multimap<ItemEffect, ItemEffectData> onBreakBlockEffects = ArrayListMultimap.create();

    public ItemEffectStore(Gson gson, String namespace, String directory, DataDistributor synchronizer) {
        super(gson, namespace, directory, ItemEffectData.class, synchronizer);
    }

    @Override
    protected void processData() {
        onHitEffects = dataMap.values().stream()
                .filter(data -> data.trigger.type.equals("tetra:apply_hit_effects"))
                .collect(Multimaps.flatteningToMultimap(
                        entry -> entry.effect,
                        Stream::of,
                        ArrayListMultimap::create));

        onMineBlockEffects = dataMap.values().stream()
                .filter(data -> data.trigger.type.equals("tetra:mine_block"))
                .collect(Multimaps.flatteningToMultimap(
                        entry -> entry.effect,
                        Stream::of,
                        ArrayListMultimap::create));

        onBreakBlockEffects = dataMap.values().stream()
                .filter(data -> data.trigger.type.equals("tetra:break_block"))
                .collect(Multimaps.flatteningToMultimap(
                        entry -> entry.effect,
                        Stream::of,
                        ArrayListMultimap::create));
    }
}
