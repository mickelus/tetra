package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelForgedContainer extends ModelBase {
    public ModelRenderer lid;
    public ModelRenderer base;

    public ModelRenderer locks[];

    public ModelForgedContainer() {
        lid = new ModelRenderer(this, 0, 0).setTextureSize(128, 64);
        lid.addBox(0, -3, -14, 30, 3, 14, 0);
        lid.rotationPointX = 1;
        lid.rotationPointY = 7;
        lid.rotationPointZ = 15;

        locks = new ModelRenderer[4];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ModelRenderer(this, 0, 0).setTextureSize(128, 64);
            locks[i].addBox(-2 + i * 6, -1, -14.0020000000949949026f, 2, 3, 1, 0);
            locks[i].rotationPointX = 8;
            locks[i].rotationPointY = 7;
            locks[i].rotationPointZ = 15;
        }


        base = new ModelRenderer(this, 0, 17).setTextureSize(128, 64);
        base.addBox(0, 1, 0, 30, 9, 14, 0);
        base.rotationPointX = 1;
        base.rotationPointY = 6;
        base.rotationPointZ = 1;
    }


    public void render(boolean[] locked) {
        lid.render(0.0625F);

        for (int i = 0; i < locks.length; i++) {
            if (locked[i]) {
                locks[i].render(0.0625F);
            }
        }
        base.render(0.0625F);
    }
}
