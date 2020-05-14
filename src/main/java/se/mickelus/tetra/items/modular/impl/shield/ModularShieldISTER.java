package se.mickelus.tetra.items.modular.impl.shield;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.data.ModuleModel;
import se.mickelus.tetra.util.CastOptional;

import java.util.Collection;

public class ModularShieldISTER extends ItemStackTileEntityRenderer {

    private final ModularShieldModel modelShield = new ModularShieldModel();
    public static final ModularShieldISTER instance = new ModularShieldISTER();

    public ModularShieldISTER() {}

    @Override
    public void render(ItemStack itemStack, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
//        boolean flag = itemStack.getChildTag("BlockEntityTag") != null;
        matrixStack.push();
        matrixStack.scale(1.0F, -1.0F, -1.0F);

        Collection<ModuleModel> models = CastOptional.cast(itemStack.getItem(), ModularShieldItem.class)
                .map(item -> item.getModels(itemStack, null))
                .orElse(ImmutableList.of());

        // handle
        models.stream()
                .forEach(model -> {
                    ModelRenderer modelRenderer = modelShield.getModel(model.type);
                    if (modelRenderer != null) {
                        Material material = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, model.location);
                        IVertexBuilder ivertexbuilder = material.getSprite().wrapBuffer(ItemRenderer.getBuffer(buffer, modelShield.getRenderType(material.getAtlasLocation()), false, itemStack.hasEffect()));

                        float r = ((model.tint >> 16) & 0xFF) / 255f; // red
                        float g = ((model.tint >>  8) & 0xFF) / 255f; // green
                        float b = ((model.tint >>  0) & 0xFF) / 255f; // blue
                        float a = ((model.tint >> 24) & 0xFF) / 255f; // alpha

                        // reset alpha to 1 if it's 0 to avoid mistakes & make things cleaner
                        a = a == 0 ? 1 : a;

                        modelRenderer.render(matrixStack, ivertexbuilder, combinedLight, combinedOverlay, r, g, b, a);
                    }
                });


//        if (flag) {
//            List<Pair<BannerPattern, DyeColor>> list = BannerTileEntity.func_230138_a_(ShieldItem.getColor(itemStackIn), BannerTileEntity.func_230139_a_(itemStackIn));
//            BannerTileEntityRenderer.func_230180_a_(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, modelShield.getTowerPlate(), material, false, list);
//        } else {
//            modelShield.getTowerPlate().render(matrixStackIn, ivertexbuilder, combinedLightIn, combinedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F);
//        }

        matrixStack.pop();
    }
}
