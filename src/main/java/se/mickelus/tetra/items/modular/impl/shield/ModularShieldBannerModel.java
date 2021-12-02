package se.mickelus.tetra.items.modular.impl.shield;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class ModularShieldBannerModel extends Model {
    public final ModelPart towerBanner;
    public final ModelPart heaterBanner;
    public final ModelPart bucklerBanner;

    public ModularShieldBannerModel() {
        super(RenderType::entityTranslucent);
        texWidth = 64;
        texHeight = 64;

        towerBanner = new ModelPart(this, 0, 0);
        towerBanner.addBox(-6.0F, -11.0F, -2.005F, 12.0F, 22.0F, 1.0F, 0.0F);

        heaterBanner = new ModelPart(this, 0, 5);
        heaterBanner.addBox(-6.0F, -6.0F, -2.005F, 12.0F, 12.0F, 1.0F, 0.0F);

        bucklerBanner = new ModelPart(this, 2, 7);
        bucklerBanner.addBox(-4.0F, -4.0F, -2.005F, 8.0F, 8.0F, 1.0F, 0.0F);
        bucklerBanner.zRot = (float) (- Math.PI / 4);
    }

    public void renderToBuffer(PoseStack matrixStack, VertexConsumer vertexBuilder, int light, int overlay, float red, float green, float blue, float alpha) {
        towerBanner.render(matrixStack, vertexBuilder, light, overlay, red, green, blue, alpha);
    }

    public boolean isBannerModel(ModelPart modelRenderer) {
        return modelRenderer == towerBanner || modelRenderer == heaterBanner || modelRenderer == bucklerBanner;
    }
}
