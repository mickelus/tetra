package se.mickelus.tetra.items.modular.impl;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.SchemaRegistry;
import se.mickelus.tetra.module.schema.RemoveSchema;
import se.mickelus.tetra.module.schema.RepairSchema;
import se.mickelus.tetra.network.PacketHandler;

import javax.annotation.Nullable;


public class ModularSingleHeadedItem extends ItemModularHandheld {

    public final static String headKey = "single/head";
    public final static String handleKey = "single/handle";

    public final static String bindingKey = "single/binding";

    private static final String unlocalizedName = "modular_single";

    @OnlyIn(Dist.CLIENT)
    private static final GuiModuleOffsets majorOffsets = new GuiModuleOffsets(1, -3, -11, 21);
    @OnlyIn(Dist.CLIENT)
    private static final GuiModuleOffsets minorOffsets = new GuiModuleOffsets(-14, 0);

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ModularSingleHeadedItem instance;

    public ModularSingleHeadedItem() {
        super(new Properties().maxStackSize(1));
        setRegistryName(unlocalizedName);

        entityHitDamage = 1;

        majorModuleKeys = new String[] { headKey, handleKey };
        minorModuleKeys = new String[] { bindingKey };

        requiredModules = new String[] { handleKey, headKey };

        updateConfig(ConfigHandler.honeSingleBase.get(), ConfigHandler.honeSingleIntegrityMultiplier.get());


        SchemaRegistry.instance.registerSchema(new RepairSchema(this));
        RemoveSchema.registerRemoveSchemas(this);
    }

    @Override
    public void init(PacketHandler packetHandler) {
        DataManager.synergyData.onReload(() -> synergies = DataManager.instance.getSynergyData("single"));
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }

    @Override
    public String getModelCacheKey(ItemStack itemStack, LivingEntity entity) {
        if (isThrowing(itemStack, entity)) {
            return super.getModelCacheKey(itemStack, entity) + ":throwing";

        }

        return super.getModelCacheKey(itemStack, entity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getTransformVariant(ItemStack itemStack, @Nullable LivingEntity entity) {
        if (isThrowing(itemStack, entity)) {
            return "throwing";
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMajorGuiOffsets() {
        return majorOffsets;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMinorGuiOffsets() {
        return minorOffsets;
    }
}


