package se.mickelus.tetra.items.modular.impl.shield;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.TetraMod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class ModularShieldRenderer extends BlockEntityWithoutLevelRenderer {
    private final Cache<String, BakedModel> modelCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public static ModelLayerLocation layer = new ModelLayerLocation(new ResourceLocation(TetraMod.MOD_ID, "item/shield"), "main");
    public static ModelLayerLocation bannerLayer = new ModelLayerLocation(new ResourceLocation(TetraMod.MOD_ID, "item/shield_banner"), "main");
    private final EntityModelSet modelSet;
    public ModularShieldBannerModel bannerModel;
    private ModularShieldModel model;

    public ModularShieldRenderer(Minecraft minecraft) {
        super(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());

        this.modelSet = minecraft.getEntityModels();

        this.model = new ModularShieldModel(modelSet.bakeLayer(layer));
        this.bannerModel = new ModularShieldBannerModel(modelSet.bakeLayer(bannerLayer));

        ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(this);
    }

    @Override
    public void onResourceManagerReload(ResourceManager p_172555_) {
        this.model = new ModularShieldModel(modelSet.bakeLayer(layer));
        this.bannerModel = new ModularShieldBannerModel(modelSet.bakeLayer(bannerLayer));
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemDisplayContext displayContext, PoseStack matrixStack, MultiBufferSource buffer,
            int combinedLight, int combinedOverlay) {
//        boolean flag = itemStack.getChildTag("BlockEntityTag") != null;
        matrixStack.pushPose();
        matrixStack.scale(1.0F, -1.0F, -1.0F);

        CastOptional.cast(itemStack.getItem(), ModularShieldItem.class)
                .stream()
                .map(item -> item.getModels(itemStack, null))
                .flatMap(Collection::stream)
                .filter(model -> model instanceof ShieldModuleModel)
                .map(model -> (ShieldModuleModel) model)
                .forEach(modelData -> {
                    ModelPart bannerPart = bannerModel.getModel(modelData.getModel().toString());
                    if (bannerPart != null) {
                        if (itemStack.getTagElement("BlockEntityTag") != null) { // banner data is stored in a compound keyed with "BlockEntityTag"
                            renderBanner(itemStack, bannerPart, matrixStack, buffer, combinedLight, combinedOverlay);
                        }
                        return;
                    }

                    ModelPart modelPart = model.getModel(modelData.getModel().toString());
                    if (modelPart != null) {
                        Material material = new Material(TextureAtlas.LOCATION_BLOCKS, modelData.getTexture());
                        VertexConsumer vertexBuilder = material.sprite().wrap(
                                ItemRenderer.getFoilBuffer(buffer, model.renderType(material.atlasLocation()), false, itemStack.hasFoil()));

                        float r = modelData.getTint().getRedFloat();
                        float g = modelData.getTint().getGreenFloat();
                        float b = modelData.getTint().getBlueFloat();
                        float a = modelData.getTint().getAlphaFloat();

                        // reset alpha to 1 if it's 0 to avoid mistakes & make things cleaner
                        a = a == 0 ? 1 : a;

                        modelPart.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay, r, g, b, a);
                    }
                });


        matrixStack.popPose();
    }

    private void renderBanner(ItemStack itemStack, ModelPart modelRenderer, PoseStack matrixStack, MultiBufferSource buffer,
            int combinedLight, int combinedOverlay) {
        List<Pair<Holder<BannerPattern>, DyeColor>> list = BannerBlockEntity.createPatterns(ShieldItem.getColor(itemStack), BannerBlockEntity.getItemPatterns(itemStack));

        for (int i = 0; i < 17 && i < list.size(); ++i) {
            Pair<Holder<BannerPattern>, DyeColor> pair = list.get(i);
            float[] tint = pair.getSecond().getTextureDiffuseColors();
            pair.getFirst().unwrapKey().map(Sheets::getShieldMaterial).ifPresent(material -> {
                VertexConsumer vertexBuilder = material.sprite().wrap(ItemRenderer.getFoilBuffer(buffer, RenderType.entitySmoothCutout(material.atlasLocation()), false, itemStack.hasFoil()));
                modelRenderer.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay, tint[0], tint[1], tint[2], 1.0f);
            });
        }
    }

    private void renderEtching(ItemStack itemStack, ModelPart modelRenderer, PoseStack matrixStack, MultiBufferSource buffer,
            int combinedLight, int combinedOverlay) {
        List<Pair<Holder<BannerPattern>, DyeColor>> list = BannerBlockEntity.createPatterns(ShieldItem.getColor(itemStack), BannerBlockEntity.getItemPatterns(itemStack));

        for (int i = 0; i < 17 && i < list.size(); ++i) {
            Pair<Holder<BannerPattern>, DyeColor> pair = list.get(i);
            if (!BannerPatterns.BASE.equals(pair.getFirst().unwrapKey().orElse(null))) {
                float[] tint = pair.getSecond().getTextureDiffuseColors();
                pair.getFirst().unwrapKey().map(Sheets::getShieldMaterial).ifPresent(material -> {
                    VertexConsumer vertexBuilder = material.sprite().wrap(ItemRenderer.getFoilBuffer(buffer, RenderType.entityNoOutline(material.atlasLocation()), false, itemStack.hasFoil()));
                    modelRenderer.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay, tint[0], tint[1], tint[2], 0.7f);
                });
            }
        }
    }


}
