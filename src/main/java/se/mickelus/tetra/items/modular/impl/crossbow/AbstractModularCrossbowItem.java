package se.mickelus.tetra.items.modular.impl.crossbow;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.forged.chthonic.ChthonicExtractorBlock;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.model.FilteredGridTextureModelData;
import se.mickelus.tetra.module.model.GridTextureModelData;
import se.mickelus.tetra.module.model.IModuleModel;
import se.mickelus.tetra.module.schematic.RepairSchematic;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public abstract class AbstractModularCrossbowItem extends ModularItem {
    public final static String staveKey = "crossbow/stave";
    public final static String stockKey = "crossbow/stock";
    public final static String stringKey = "crossbow/string";

    public final static String attachmentAKey = "crossbow/attachment_0";
    public final static String attachmentBKey = "crossbow/attachment_1";

    public static final String identifier = "modular_crossbow";
    private static final GuiModuleOffsets majorOffsets = new GuiModuleOffsets(-13, 0, -13, 18);
    private static final GuiModuleOffsets minorOffsets = new GuiModuleOffsets(4, -1, 13, 12, 4, 25);
    protected GridTextureModelData arrowModel = new GridTextureModelData(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "item/module/crossbow/arrow"));
    protected GridTextureModelData extractorModel = new GridTextureModelData(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "item/module/crossbow/extractor"));
    protected GridTextureModelData fireworkModel = new GridTextureModelData(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "item/module/crossbow/firework"));
    // used to pick projectiles from the player inventory

    public AbstractModularCrossbowItem(Properties properties) {
        super(properties);

        majorModuleKeys = new String[] { staveKey, stockKey };
        minorModuleKeys = new String[] { attachmentAKey, stringKey, attachmentBKey };

        requiredModules = new String[] { stringKey, stockKey, staveKey };

        updateConfig(ConfigHandler.HONE_CROSSBOW_BASE_DEFAULT, ConfigHandler.HONE_CROSSBOW_INTEGRITY_MULTIPLIER_DEFAULT);

        SchematicRegistry.instance.registerSchematic(new RepairSchematic(this, identifier));
    }

    @Override
    public void commonInit(PacketHandler packetHandler) {
        DataManager.instance.synergyData.onReload(() -> synergies = DataManager.instance.synergyData.getOrdered("crossbow/"));
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }

    public abstract boolean isLoaded(ItemStack itemStack);

    public abstract float getProgress(ItemStack itemStack, @Nullable LivingEntity entity);

    protected abstract ItemStack getFirstProjectile(ItemStack itemStack);

    private String getDrawVariant(ItemStack itemStack, @Nullable LivingEntity entity) {
        float progress = getProgress(itemStack, entity);

        if (isLoaded(itemStack)) {
            return "loaded";
        } else if (progress == 0) {
            return "undrawn";
        } else if (progress < 0.58) {
            return "draw_0";
        } else if (progress < 1) {
            return "draw_1";
        }
        return "draw_2";
    }

    private String getProjectileVariant(ItemStack itemStack) {
        ItemStack projectileStack = getFirstProjectile(itemStack);

        if (projectileStack.getItem() instanceof FireworkRocketItem) {
            return "p1";
        }

        if (ChthonicExtractorBlock.item.equals(projectileStack.getItem()) || ChthonicExtractorBlock.usedItem.equals(projectileStack.getItem())) {
            return "p2";
        }

        return "p0";
    }

    private IModuleModel getProjectileModel(ItemStack itemStack) {
        ItemStack projectileStack = getFirstProjectile(itemStack);

        if (projectileStack.getItem() instanceof FireworkRocketItem) {
            return fireworkModel;
        }

        if (ChthonicExtractorBlock.item.equals(projectileStack.getItem()) || ChthonicExtractorBlock.usedItem.equals(projectileStack.getItem())) {
            return extractorModel;
        }

        return arrowModel;
    }

    @Override
    public String getModelCacheKey(ItemStack itemStack, LivingEntity entity) {
        return super.getModelCacheKey(itemStack, entity) + ":" + getDrawVariant(itemStack, entity) + getProjectileVariant(itemStack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ImmutableList<IModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity) {
        String modelType = getDrawVariant(itemStack, entity);

        ImmutableList<IModuleModel> models = getAllModules(itemStack).stream()
                .sorted(Comparator.comparing(ItemModule::getRenderLayer))
                .map(module -> module.getModels(itemStack))
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(IModuleModel::getRenderLayer))
                .filter(model -> filterModel(model, modelType))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        if (isLoaded(itemStack)) {
            return ImmutableList.<IModuleModel>builder()
                    .addAll(models)
                    .add(getProjectileModel(itemStack))
                    .build();
        }

        return models;
    }

    private static boolean filterModel(IModuleModel model, String filter) {
        return !(model instanceof FilteredGridTextureModelData filteredModel) || filteredModel.getFilter().equals(filter);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMajorGuiOffsets(ItemStack itemStack) {
        return majorOffsets;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMinorGuiOffsets(ItemStack itemStack) {
        return minorOffsets;
    }
}
