package se.mickelus.tetra.items.modular.impl.shield;

import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import se.mickelus.tetra.compat.forge.common.util.NonNullLazy;
import se.mickelus.tetra.compat.forge.registries.ObjectHolder;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.properties.TetraAttributes;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
@ParametersAreNonnullByDefault
public class ModularShieldItem extends ItemModularHandheld {
    public final static String plateKey = "shield/plate";
    public final static String gripKey = "shield/grip";
    public final static String bossKey = "shield/boss";

    public static final String identifier = "modular_shield";

    public static final String bannerImprovementKey = "shield/banner";

    @ObjectHolder(registryName = "item", value = TetraMod.MOD_ID + ":" + identifier)
    public static ModularShieldItem instance;

    public ModularShieldItem() {
        super(new Properties()
                .stacksTo(1)
                .fireResistant());
        instance = this;

        majorModuleKeys = new String[] { plateKey, gripKey };
        minorModuleKeys = new String[] { bossKey };

        requiredModules = new String[] { plateKey, gripKey };

        updateConfig(ConfigHandler.HONE_SHIELD_BASE_DEFAULT, ConfigHandler.HONE_SHIELD_INTEGRITY_MULTIPLIER_DEFAULT);

        SchematicRegistry.instance.registerSchematic(new RepairSchematic(this, identifier));
        SchematicRegistry.instance.registerSchematic(new ApplyBannerSchematic());

        ItemUpgradeRegistry.instance.registerReplacementHook(this::copyBanner);

        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    @OnlyIn(Dist.CLIENT)
    public IClientItemExtensions createClientExtensions() {
        NonNullLazy<BlockEntityWithoutLevelRenderer> renderer = NonNullLazy.of(() -> new ModularShieldRenderer(Minecraft.getInstance()));
        return new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer.get();
            }
        };
    }

    @Override
    public void commonInit(PacketHandler packetHandler) {
        DataManager.instance.synergyData.onReload(() -> synergies = DataManager.instance.synergyData.getOrdered("shield/"));
    }

    @Override
    public void clientInit() {
        super.clientInit();

        ItemProperties.register(this, ResourceLocation.withDefaultNamespace("blocking"),
                (itemStack, world, entity, i) -> isBlocking(itemStack, entity) ? 1.0F : 0.0F);
        ItemProperties.register(this, ResourceLocation.withDefaultNamespace("throwing"),
                (itemStack, world, entity, i) -> isThrowing(itemStack, entity) ? 1.0F : 0.0F);
    }

    private ItemStack copyBanner(ItemStack original, ItemStack replacement) {
        if (equals(replacement.getItem())) {
            Optional.ofNullable(original.get(DataComponents.BASE_COLOR)).ifPresent(color -> {
                replacement.set(DataComponents.BASE_COLOR, color);
                replacement.set(DataComponents.BANNER_PATTERNS, original.getOrDefault(DataComponents.BANNER_PATTERNS,
                        net.minecraft.world.level.block.entity.BannerPatternLayers.EMPTY));

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
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack itemStack) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        getAttributeModifiersCached(itemStack).forEach((attribute, modifier) ->
                builder.add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), modifier, EquipmentSlotGroup.HAND));
        return builder.build();
    }

    @Override
    public double getAbilityBaseDamage(@Nullable LivingEntity entity, ItemStack itemStack) {
        if (entity != null) {
            AttributeInstance entityInstance = entity.getAttribute(AttributeHelper.getHolder(TetraAttributes.abilityDamage.get()));
            if (entityInstance != null) {
                return AttributeHelper.calculateValue(TetraAttributes.abilityDamage.get(), entityInstance.getModifiers(),
                        getAttributeModifiersCached(itemStack).get(TetraAttributes.abilityDamage.get()));
            }
        }
        return getAttributeValue(itemStack, TetraAttributes.abilityDamage.get());
    }

    @Override
    public double getCooldownBase(ItemStack itemStack) {
        return getAttributeValue(itemStack, TetraAttributes.abilityCooldown.get());
    }
}
