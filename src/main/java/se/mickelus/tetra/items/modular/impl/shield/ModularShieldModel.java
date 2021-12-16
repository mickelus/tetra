package se.mickelus.tetra.items.modular.impl.shield;


import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.data.DataManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Optional;

@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class ModularShieldModel extends Model {
    private static final Logger logger = LogManager.getLogger();
    private final ModelPart root;

    public ModularShieldModel(ModelPart modelPart) {
        super(RenderType::entityTranslucent);

        this.root = modelPart;
    }

    private static Optional<Pair<ResourceLocation, ShieldModelData>> getModel(ResourceManager resourceManager, ResourceLocation resourceLocation) {
        try {
            return Optional.of(resourceManager.getResource(resourceLocation))
                    .map(Resource::getInputStream)
                    .map(inputStream -> new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
                    .map(reader -> GsonHelper.fromJson(DataManager.gson, reader, JsonElement.class))
                    .map(json -> ShieldModelData.codec.decode(JsonOps.INSTANCE, json))
                    .map(DataResult::result)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(Pair::getFirst)
                    .map(model -> new Pair<>(resourceLocation, model));
        } catch (IOException | JsonParseException e) {
            logger.warn("Failed to parse model data from '{}': {}", resourceLocation, e);
        }

        return Optional.empty();
    }

    private static String trimResourceLocation(ResourceLocation rl) {
        return rl.getNamespace() + ":" + rl.getPath().substring(22, rl.getPath().length() - 5);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        resourceManager.listResources("models/modular/shield/", path -> path.endsWith(".json")).stream()
                .map(rl -> getModel(resourceManager, rl))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(pair -> pair.getSecond().populatePartDefinition(parts.addOrReplaceChild(trimResourceLocation(pair.getFirst()),
                        CubeListBuilder.create(), PartPose.ZERO)));

        return LayerDefinition.create(mesh, 32, 32);
    }

    public ModelPart getModel(String modelType) {
        try {
            return root.getChild(modelType);
        } catch (NoSuchElementException e) {}
        return null;
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer vertexBuilder, int light, int overlay, float red, float green, float blue, float alpha) {}
}
