package se.mickelus.tetra.items.modular.impl.shield;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.NoSuchElementException;

// todo 1.18: fix up shield renderer
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class ModularShieldBannerModel extends Model {
    private final ModelPart root;

    public ModularShieldBannerModel(ModelPart modelPart) {
        super(RenderType::entityTranslucent);
        this.root = modelPart;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("tetra:banner/tower", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-6.0F, -11.0F, -2.005F, 12.0F, 22.0F, 1.0F), PartPose.ZERO);

        for (int i = 0; i < 4; i++) {
            parts.addOrReplaceChild("tetra:banner/heater" + i, CubeListBuilder.create()
                    .texOffs(0, 5)
                    .addBox(-6.0F, -6.0F, -2.005F, 12.0F, 12.0F, 1.0F), PartPose.ZERO);
        }

        parts.addOrReplaceChild("tetra:banner/buckler", CubeListBuilder.create()
                        .texOffs(2, 7)
                        .addBox(-4.0F, -4.0F, -2.005F, 8.0F, 8.0F, 1.0F),
                PartPose.rotation(0, 0, (float) (-Math.PI / 4)));

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void renderToBuffer(PoseStack matrixStack, VertexConsumer vertexBuilder, int light, int overlay, float red, float green, float blue, float alpha) {
//        towerBanner.render(matrixStack, vertexBuilder, light, overlay, red, green, blue, alpha);
    }

    public ModelPart getModel(String modelType) {
        try {
            return root.getChild(modelType);
        } catch (NoSuchElementException e) {
        }
        return null;
    }
}
