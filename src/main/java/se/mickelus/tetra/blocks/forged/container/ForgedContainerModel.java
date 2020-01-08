package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class ForgedContainerModel extends Model {
    public RendererModel lid;
    public RendererModel base;

    public RendererModel locks[];

    public ForgedContainerModel() {
        lid = new RendererModel(this, 0, 0).setTextureSize(128, 64);
        lid.addBox(0, -3, -14, 30, 3, 14, 0);
        lid.rotationPointX = 1;
        lid.rotationPointY = 7;
        lid.rotationPointZ = 15;

        locks = new RendererModel[4];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new RendererModel(this, 0, 0).setTextureSize(128, 64);
            locks[i].addBox(-2 + i * 6, -1, -14.0020000000949949026f, 2, 3, 1, 0);
            locks[i].rotationPointX = 8;
            locks[i].rotationPointY = 7;
            locks[i].rotationPointZ = 15;
        }


        base = new RendererModel(this, 0, 17).setTextureSize(128, 64);
        base.addBox(0, 1, 0, 30, 9, 14, 0);
        base.rotationPointX = 1;
        base.rotationPointY = 6;
        base.rotationPointZ = 1;
    }


    public void render(Boolean[] locked) {
        lid.render(0.0625F);

        for (int i = 0; i < locks.length; i++) {
            if (locked[i]) {
                locks[i].render(0.0625F);
            }
        }
        base.render(0.0625F);
    }
}
