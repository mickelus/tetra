package se.mickelus.tetra.items.modular.impl.shield;

import com.google.common.collect.Multimap;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.items.modular.impl.BlockProgressOverlay;
import se.mickelus.tetra.module.SchemaRegistry;
import se.mickelus.tetra.module.schema.RemoveSchema;
import se.mickelus.tetra.module.schema.RepairSchema;

public class ModularShieldItem extends ItemModularHandheld {
    public final static String shieldKey = "shield/plate";
    public final static String gripKey = "shield/grip";
    public final static String bossKey = "shield/boss";

    public static final String unlocalizedName = "modular_shield";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ModularShieldItem instance;

    public ModularShieldItem() {
        super(new Properties()
                .maxStackSize(1)
                .setISTER(() -> ModularShieldISTER::new));
        setRegistryName(unlocalizedName);

        majorModuleKeys = new String[] { shieldKey, gripKey };
        minorModuleKeys = new String[] { bossKey };

        requiredModules = new String[] { shieldKey, gripKey };

        updateConfig(ConfigHandler.honeShieldBase.get(), ConfigHandler.honeShieldIntegrityMultiplier.get());

        SchemaRegistry.instance.registerSchema(new RepairSchema(this));
        RemoveSchema.registerRemoveSchemas(this);

        this.addPropertyOverride(new ResourceLocation("blocking"), (itemStack, world, entity) -> {
            return entity != null && entity.isHandActive() && entity.getActiveItemStack() == itemStack ? 1.0F : 0.0F;
        });

        DispenserBlock.registerDispenseBehavior(this, ArmorItem.DISPENSER_BEHAVIOR);
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack itemStack) {
        Multimap<String, AttributeModifier> modifiers = super.getAttributeModifiers(slot, itemStack);
        modifiers.removeAll(SharedMonsterAttributes.ATTACK_SPEED.getName());
        return modifiers;
    }

    @Override
    public void clientInit() {
        super.clientInit();

        MinecraftForge.EVENT_BUS.register(new BlockProgressOverlay(Minecraft.getInstance()));
    }
}
