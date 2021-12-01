package se.mickelus.tetra.items.modular.impl.shield;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.module.data.ModuleModel;
import se.mickelus.tetra.util.CastOptional;

import java.util.Collection;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ModularShieldISTER extends BlockEntityWithoutLevelRenderer {

    private final ModularShieldModel model = new ModularShieldModel();

    public ModularShieldISTER() {}

    @Override
    public void renderByItem(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
//        boolean flag = itemStack.getChildTag("BlockEntityTag") != null;
        matrixStack.pushPose();
        matrixStack.scale(1.0F, -1.0F, -1.0F);

        Collection<ModuleModel> models = CastOptional.cast(itemStack.getItem(), ModularShieldItem.class)
                .map(item -> item.getModels(itemStack, null))
                .orElse(ImmutableList.of());

        // handle
        models.stream()
                .forEach(modelData -> {
                    ModelPart modelRenderer = model.getModel(modelData.type);
                    if (model.bannerModel.isBannerModel(modelRenderer)) {
                        if (itemStack.getTagElement("BlockEntityTag") != null) { // banner data is stored in a compound keyed with "BlockEntityTag"
                            renderBanner(itemStack, modelRenderer, matrixStack, buffer, combinedLight, combinedOverlay);
                        }
                    } else if (modelRenderer != null) {
                        Material material = new Material(TextureAtlas.LOCATION_BLOCKS, modelData.location);
                        VertexConsumer vertexBuilder = material.sprite().wrap(
                                ItemRenderer.getFoilBuffer(buffer, model.renderType(material.atlasLocation()), false, itemStack.hasFoil()));

                        float r = ((modelData.tint >> 16) & 0xFF) / 255f; // red
                        float g = ((modelData.tint >>  8) & 0xFF) / 255f; // green
                        float b = ((modelData.tint >>  0) & 0xFF) / 255f; // blue
                        float a = ((modelData.tint >> 24) & 0xFF) / 255f; // alpha

                        // reset alpha to 1 if it's 0 to avoid mistakes & make things cleaner
                        a = a == 0 ? 1 : a;

                        modelRenderer.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay, r, g, b, a);
                    }
                });



        matrixStack.popPose();
    }

    private void renderBanner(ItemStack itemStack, ModelPart modelRenderer, PoseStack matrixStack, MultiBufferSource buffer,
            int combinedLight, int combinedOverlay) {
        List<Pair<BannerPattern, DyeColor>> list = BannerBlockEntity.createPatterns(ShieldItem.getColor(itemStack), BannerBlockEntity.getItemPatterns(itemStack));

        for(int i = 0; i < 17 && i < list.size(); ++i) {
            Pair<BannerPattern, DyeColor> pair = list.get(i);
            float[] tint = pair.getSecond().getTextureDiffuseColors();
            Material material = new Material(Sheets.SHIELD_SHEET, pair.getFirst().location(false));
            VertexConsumer vertexBuilder = material.sprite().wrap(ItemRenderer.getFoilBuffer(buffer, RenderType.entitySmoothCutout(material.atlasLocation()), false, itemStack.hasFoil()));
            modelRenderer.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay, tint[0], tint[1], tint[2], 1.0f);
        }
    }

    private void renderEtching(ItemStack itemStack, ModelPart modelRenderer, PoseStack matrixStack, MultiBufferSource buffer,
            int combinedLight, int combinedOverlay) {
        List<Pair<BannerPattern, DyeColor>> list = BannerBlockEntity.createPatterns(ShieldItem.getColor(itemStack), BannerBlockEntity.getItemPatterns(itemStack));

        for(int i = 0; i < 17 && i < list.size(); ++i) {
            Pair<BannerPattern, DyeColor> pair = list.get(i);
            if (!pair.getFirst().equals(BannerPattern.BASE)) {
                float[] tint = pair.getSecond().getTextureDiffuseColors();
                Material material = new Material(Sheets.SHIELD_SHEET, pair.getFirst().location(false));
                VertexConsumer vertexBuilder = material.sprite().wrap(ItemRenderer.getFoilBuffer(buffer, RenderType.entityNoOutline(material.atlasLocation()), false, itemStack.hasFoil()));
                modelRenderer.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay, tint[0], tint[1], tint[2], 0.7f);
            }
        }
    }
}
