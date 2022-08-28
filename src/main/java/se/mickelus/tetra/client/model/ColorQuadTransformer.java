package se.mickelus.tetra.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.client.model.IQuadTransformer;

public class ColorQuadTransformer implements IQuadTransformer {
    int color;

    public ColorQuadTransformer(int color) {
        int fixedAlpha = ((color >> 24) & 0xFF) == 0
                ? 0xFF
                : ((color >> 24) & 0xFF);

        this.color = ((color >> 16) & 0xff) |
                (color & 0xff00) |
                ((color << 16) & 0xff0000) |
                (fixedAlpha << 24);
    }

    @Override
    public void processInPlace(BakedQuad quad) {
        int[] vertices = quad.getVertices();
        for (int i = 0; i < 4; i++) {
            vertices[i * IQuadTransformer.STRIDE + IQuadTransformer.COLOR] = color;
        }
    }
}
