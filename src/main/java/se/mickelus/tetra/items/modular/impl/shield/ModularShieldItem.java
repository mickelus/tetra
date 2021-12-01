package se.mickelus.tetra.items.modular.impl.shield;

import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.items.modular.impl.BlockProgressOverlay;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.RemoveSchematic;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.properties.TetraAttributes;
import se.mickelus.tetra.util.CastOptional;

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
                .stacksTo(1)
                .fireResistant()
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

        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    @Override
    public void init(PacketHandler packetHandler) {
        DataManager.synergyData.onReload(() -> synergies = DataManager.instance.getSynergyData("shield/"));
    }

    @Override
    public void clientInit() {
        super.clientInit();

        ItemProperties.register(this, new ResourceLocation("blocking"),
                (itemStack, world, entity) -> entity != null && entity.isUsingItem() && entity.getUseItem() == itemStack ? 1.0F : 0.0F);

        MinecraftForge.EVENT_BUS.register(new BlockProgressOverlay(Minecraft.getInstance()));
    }

    private ItemStack copyBanner(ItemStack original, ItemStack replacement) {
        if (equals(replacement.getItem())) {
            Optional.ofNullable(original.getTagElement("BlockEntityTag"))
                    .ifPresent(tag -> {
                        replacement.getOrCreateTag().put("BlockEntityTag", tag);

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
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack itemStack) {
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
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
