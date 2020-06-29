package se.mickelus.tetra.items.modular.impl.shield;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.ShieldModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModularShieldBannerModel extends Model {
    public final ModelRenderer towerBanner;
    public final ModelRenderer heaterBanner;
    public final ModelRenderer bucklerBanner;

    public ModularShieldBannerModel() {
        super(RenderType::getEntityTranslucent);
        textureWidth = 64;
        textureHeight = 64;

        towerBanner = new ModelRenderer(this, 0, 0);
        towerBanner.addBox(-6.0F, -11.0F, -2.005F, 12.0F, 22.0F, 1.0F, 0.0F);

        heaterBanner = new ModelRenderer(this, 0, 5);
        heaterBanner.addBox(-6.0F, -6.0F, -2.005F, 12.0F, 12.0F, 1.0F, 0.0F);

        bucklerBanner = new ModelRenderer(this, 2, 7);
        bucklerBanner.addBox(-4.0F, -4.0F, -2.005F, 8.0F, 8.0F, 1.0F, 0.0F);
        bucklerBanner.rotateAngleZ = (float) (- Math.PI / 4);
    }

    public void render(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int light, int overlay, float red, float green, float blue, float alpha) {
        towerBanner.render(matrixStack, vertexBuilder, light, overlay, red, green, blue, alpha);
    }

    public boolean isBannerModel(ModelRenderer modelRenderer) {
        return modelRenderer == towerBanner || modelRenderer == heaterBanner || modelRenderer == bucklerBanner;
    }
}
