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
public class ModularShieldModel extends Model {
    private final ModelPart towerPlate;

    private final ModelPart heaterPlate;

    private final ModelPart bucklerPlate;

    private final ModelPart grip;
    private ModelPart straps;

    private ModelPart boss;

    public static final String towerModelType = "shield_tower";
    public static final String heaterModelType = "shield_heater";
    public static final String bucklerModelType = "shield_buckler";
    public static final String gripModelType = "shield_grip";
    public static final String strapsModelType = "shield_straps";
    public static final String bossModelType = "shield_boss";

    public ModularShieldBannerModel bannerModel;

    public static final String towerBannerModelType = "banner_tower";
    public static final String heaterBannerModelType = "banner_heater";
    public static final String bucklerBannerModelType = "banner_buckler";

    public ModularShieldModel() {
        super(RenderType::entityTranslucent);
        texWidth = 32;
        texHeight = 32;

        towerPlate = new ModelPart(this, 0, 0);
        towerPlate.addBox(-6.0F, -11.0F, -2.0F, 12.0F, 22.0F, 1.0F, 0.0F);

        heaterPlate = new ModelPart(this, 0, 0);
        heaterPlate.addBox(-7.0F, -8.0F, -2.0F, 14.0F, 16.0F, 1.0F, 0.0F);

        bucklerPlate = new ModelPart(this, 0, 0);
        bucklerPlate.addBox(-5.0F, -5.0F, -2.0F, 10.0F, 10.0F, 1.0F, 0.0F);
        bucklerPlate.zRot = (float) (- Math.PI / 4);

        grip = new ModelPart(this, 0, 0);
        grip.addBox(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 6.0F, 0.0F);

        straps = new ModelPart(this, 0, 0);
        straps.addBox(2, -3, -1, 1, 6, 1, 0);
        straps.addBox(-3, -3, -1, 1, 6, 1, 0);

        boss = new ModelPart(this, 0, 0);
        boss.addBox(-5.0F, -5.0F, -2.01F, 10.0F, 10.0F, 1.0F, 0.0F);

        bannerModel = new ModularShieldBannerModel();
    }

    public ModelPart getModel(String modelType) {
        switch (modelType) {
            case towerModelType:
                return towerPlate;
            case heaterModelType:
                return heaterPlate;
            case bucklerModelType:
                return bucklerPlate;
            case gripModelType:
                return grip;
            case strapsModelType:
                return straps;
            case bossModelType:
                return boss;
            case towerBannerModelType:
                return bannerModel.towerBanner;
            case heaterBannerModelType:
                return bannerModel.heaterBanner;
            case bucklerBannerModelType:
                return bannerModel.bucklerBanner;
        }

        return null;
    }

    public void renderToBuffer(PoseStack matrixStack, VertexConsumer vertexBuilder, int light, int overlay, float red, float green, float blue, float alpha) {
        this.towerPlate.render(matrixStack, vertexBuilder, light, overlay, red, green, blue, alpha);
        this.grip.render(matrixStack, vertexBuilder, light, overlay, red, green, blue, alpha);
    }
}
