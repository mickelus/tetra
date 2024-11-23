package se.mickelus.tetra.items.modular.impl;

import com.google.common.collect.Lists;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.RepairSchematic;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
public class ModularBladedItem extends ItemModularHandheld {
    public final static String bladeKey = "sword/blade";
    public final static String hiltKey = "sword/hilt";

    public final static String guardKey = "sword/guard";
    public final static String pommelKey = "sword/pommel";
    public final static String fullerKey = "sword/fuller";

    public static final String identifier = "modular_sword";

    @ObjectHolder(registryName = "item", value = TetraMod.MOD_ID + ":" + identifier)
    public static ModularBladedItem instance;

    public ModularBladedItem() {
        super(new Item.Properties().stacksTo(1).fireResistant());

        blockDestroyDamage = 2;

        majorModuleKeys = new String[] { bladeKey, hiltKey };
        minorModuleKeys = new String[] { fullerKey, guardKey, pommelKey };

        requiredModules = new String[] { bladeKey, hiltKey };

        updateConfig(ConfigHandler.honeSwordBase.get(), ConfigHandler.honeSwordIntegrityMultiplier.get());

        SchematicRegistry.instance.registerSchematic(new RepairSchematic(this, identifier));
    }

    public static Collection<ItemStack> getCreativeTabItemStacks() {
        return Lists.newArrayList(
                createItemStack("short_blade", "iron", "stick")
        );
    }

    public static ItemStack createItemStack(String blade, String bladeMaterial, String hiltMaterial) {
        ItemStack itemStack = new ItemStack(instance);

        IModularItem.putModuleInSlot(itemStack, bladeKey, "sword/" + blade, "sword/" + blade + "_material", blade + "/" + bladeMaterial);
        IModularItem.putModuleInSlot(itemStack, hiltKey, "sword/basic_hilt", "sword/basic_hilt_material", "basic_hilt/" + hiltMaterial);

        IModularItem.updateIdentifier(itemStack);

        return itemStack;
    }

    @Override
    public void commonInit(PacketHandler packetHandler) {
        DataManager.instance.synergyData.onReload(() -> synergies = DataManager.instance.synergyData.getOrdered("sword/"));
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

        if (isBlocking(itemStack, entity)) {
            return super.getModelCacheKey(itemStack, entity) + ":blocking";
        }

        return super.getModelCacheKey(itemStack, entity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getTransformVariant(ItemStack itemStack, @Nullable LivingEntity entity) {
        if (isThrowing(itemStack, entity)) {
            return "throwing";
        }
        if (isBlocking(itemStack, entity)) {
            return "blocking";
        }
        return null;
    }
}
