package se.mickelus.tetra.items.modular.impl.shield;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModularShieldModel extends Model {
    private final ModelRenderer towerPlate;
    private final ModelRenderer heaterPlate;
    private final ModelRenderer bucklerPlate;

    private final ModelRenderer grip;

    public static final String towerModelType = "shield_tower";
    public static final String heaterModelType = "shield_heater";
    public static final String bucklerModelType = "shield_buckler";
    public static final String gripModelType = "shield_grip";

    public ModularShieldModel() {
        super(RenderType::getEntityTranslucent);
        textureWidth = 32;
        textureHeight = 32;

        towerPlate = new ModelRenderer(this, 0, 0);
        towerPlate.addBox(-6.0F, -11.0F, -2.0F, 12.0F, 22.0F, 1.0F, 0.0F);

        heaterPlate = new ModelRenderer(this, 0, 0);
        heaterPlate.addBox(-7.0F, -8.0F, -2.0F, 14.0F, 16.0F, 1.0F, 0.0F);

        bucklerPlate = new ModelRenderer(this, 0, 0);
        bucklerPlate.addBox(-5.0F, -5.0F, -2.0F, 10.0F, 10.0F, 1.0F, 0.0F);
        bucklerPlate.rotateAngleZ = (float) (- Math.PI / 4);

        grip = new ModelRenderer(this, 0, 0);
        grip.addBox(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 6.0F, 0.0F);
    }

    public ModelRenderer getModel(String modelType) {
        switch (modelType) {
            case towerModelType:
                return towerPlate;
            case heaterModelType:
                return heaterPlate;
            case bucklerModelType:
                return bucklerPlate;
            case gripModelType:
                return grip;
        }

        return null;
    }

    public void render(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int light, int overlay, float red, float green, float blue, float alpha) {
        this.towerPlate.render(matrixStack, vertexBuilder, light, overlay, red, green, blue, alpha);
        this.grip.render(matrixStack, vertexBuilder, light, overlay, red, green, blue, alpha);
    }
}
