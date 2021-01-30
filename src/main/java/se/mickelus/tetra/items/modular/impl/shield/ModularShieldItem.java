package se.mickelus.tetra.items.modular.impl.shield;

import com.google.common.collect.Multimap;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.items.modular.impl.BlockProgressOverlay;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.RemoveSchematic;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.properties.TetraAttributes;
import se.mickelus.tetra.util.CastOptional;
import se.mickelus.tetra.util.NBTHelper;

import java.util.Optional;

public class ModularShieldItem extends ItemModularHandheld {
    public final static String plateKey = "shield/plate";
    public final static String gripKey = "shield/grip";
    public final static String bossKey = "shield/boss";

    public static final String unlocalizedName = "modular_shield";

    public static final String bannerImprovementKey = "shield/banner";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ModularShieldItem instance;

    public ModularShieldItem() {
        super(new Properties()
                .maxStackSize(1)
                .isImmuneToFire()
                .setISTER(() -> ModularShieldISTER::new));
        setRegistryName(unlocalizedName);

        majorModuleKeys = new String[] {plateKey, gripKey };
        minorModuleKeys = new String[] { bossKey };

        requiredModules = new String[] {plateKey, gripKey };

        updateConfig(ConfigHandler.honeShieldBase.get(), ConfigHandler.honeShieldIntegrityMultiplier.get());

        SchematicRegistry.instance.registerSchematic(new RepairSchematic(this));
        SchematicRegistry.instance.registerSchematic(new ApplyBannerSchematic());
        RemoveSchematic.registerRemoveSchematics(this);

        ItemUpgradeRegistry.instance.registerReplacementHook(this::copyBanner);

        DispenserBlock.registerDispenseBehavior(this, ArmorItem.DISPENSER_BEHAVIOR);
    }

    @Override
    public void clientInit() {
        super.clientInit();

        ItemModelsProperties.registerProperty(this, new ResourceLocation("blocking"),
                (itemStack, world, entity) -> entity != null && entity.isHandActive() && entity.getActiveItemStack() == itemStack ? 1.0F : 0.0F);

        MinecraftForge.EVENT_BUS.register(new BlockProgressOverlay(Minecraft.getInstance()));
    }

    private ItemStack copyBanner(ItemStack original, ItemStack replacement) {
        if (equals(replacement.getItem())) {
            Optional.ofNullable(original.getChildTag("BlockEntityTag"))
                    .ifPresent(tag -> {
                        NBTHelper.getTag(replacement).put("BlockEntityTag", tag);

                        CastOptional.cast(getModuleFromSlot(replacement, plateKey), ItemModuleMajor.class)
                                .filter(module -> module.acceptsImprovement(bannerImprovementKey))
                                .ifPresent(module -> module.addImprovement(replacement, bannerImprovementKey, 0));
                    });

        }

        return replacement;
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack itemStack) {
        if (slot == EquipmentSlotType.MAINHAND || slot == EquipmentSlotType.OFFHAND) {
            return getAttributeModifiersCached(itemStack);
        }

        return AttributeHelper.emptyMap;
    }

    @Override
    public double getAbilityBaseDamage(ItemStack itemStack) {
        return getAttributeValue(itemStack, TetraAttributes.abilityDamage.get());
    }

    @Override
    public double getCooldownBase(ItemStack itemStack) {
        return getAttributeValue(itemStack, TetraAttributes.abilityCooldown.get());
    }
}
