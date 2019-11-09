package se.mickelus.tetra.client.model;

import javax.annotation.Nonnull;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraftforge.client.model.BakedModelWrapper;

public class BakedWrapper extends BakedModelWrapper<SimpleBakedModel> {

    private ItemOverrideList itemOverrideList;

    public BakedWrapper(SimpleBakedModel originalModel, ItemOverrideList itemOverrideList) {
        super(originalModel);

        this.itemOverrideList = itemOverrideList;
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return itemOverrideList;
    }
}