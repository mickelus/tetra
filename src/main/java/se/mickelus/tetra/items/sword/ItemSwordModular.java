package se.mickelus.tetra.items.sword;

import com.google.common.collect.Multimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.module.BasicSchema;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.RepairSchema;
import se.mickelus.tetra.network.PacketPipeline;

import javax.annotation.Nullable;

public class ItemSwordModular extends ItemModular {

    public final static String bladeKey = "sword:blade";
    public final static String hiltKey = "sword:hilt";


    static final String unlocalizedName = "sword_modular";

    public ItemSwordModular() {
        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        GameRegistry.register(this);
        setCreativeTab(TetraCreativeTabs.getInstance());

        majorModuleNames = new String[]{"Blade", "Hilt"};
        majorModuleKeys = new String[]{bladeKey, hiltKey};
        minorModuleNames = new String[]{"Guard", "Pommel", "Fuller"};
        minorModuleKeys = new String[]{"sword:guard", "sword:pommel", "sword:fuller"};
    }

    @Override
    public IItemPropertyGetter getPropertyGetter(ResourceLocation key) {
        return super.getPropertyGetter(key);
    }

    @Override
    public void init(PacketPipeline packetPipeline) {
        new BladeModule(bladeKey);
        new BasicSchema("blade_schema", BladeModule.instance);

        new ShortBladeModule(bladeKey);
        new BasicSchema("short_blade_schema", ShortBladeModule.instance);

        new HeavyBladeModule(bladeKey);
        new BasicSchema("heavy_blade_schema", HeavyBladeModule.instance);

        new HiltModule(hiltKey);
        new BasicSchema("hilt_schema", HiltModule.instance);

        new RepairSchema(this);

        ItemUpgradeRegistry.instance.registerPlaceholder(this::replaceSword);
    }

    private ItemStack replaceSword(ItemStack originalStack) {
        Item originalItem = originalStack.getItem();

        if (!(originalItem instanceof ItemSword)) {
            return null;
        }

        return createItemStack(((ItemSword) originalItem).getToolMaterialName());
    }

    private ItemStack createItemStack(String material) {
        ItemStack itemStack = new ItemStack(this);
        itemStack.setTagCompound(new NBTTagCompound());

        ItemStack bladeMaterial;
        switch (material) {
            case "WOOD":
                bladeMaterial = new ItemStack(Blocks.PLANKS, 2);
                break;
            case "STONE":
                bladeMaterial = new ItemStack(Blocks.COBBLESTONE, 2);
                break;
            case "IRON":
                bladeMaterial = new ItemStack(Items.IRON_INGOT, 2);
                break;
            case "DIAMOND":
                bladeMaterial = new ItemStack(Items.DIAMOND, 2);
                break;
            default:
                bladeMaterial = new ItemStack(Blocks.PLANKS, 2);
                break;
        }

        BladeModule.instance.addModule(itemStack, new ItemStack[] {bladeMaterial}, false);
        HiltModule.instance.addModule(itemStack, new ItemStack[] {new ItemStack(Items.STICK)}, false);
        return itemStack;
    }

    public String getItemStackDisplayName(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        if (stack.hasTagCompound() && tag.hasKey(BladeModule.materialKey)) {
            return tag.getString(BladeModule.materialKey);
        }
        return "§kUnknown";
    }

    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack itemStack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, itemStack);

        if (slot == EntityEquipmentSlot.MAINHAND) {
            double damageModifier = getAllModules(itemStack).stream()
                    .map(itemModule -> itemModule.getDamageModifier(itemStack))
                    .reduce(0d, Double::sum);

            damageModifier = getAllModules(itemStack).stream()
                    .map(itemModule -> itemModule.getDamageMultiplierModifier(itemStack))
                    .reduce(damageModifier, (a, b) -> a*b);

            double speedModifier = getAllModules(itemStack).stream()
                    .map(itemModule -> itemModule.getSpeedModifier(itemStack))
                    .reduce(-2.4d, Double::sum);

            speedModifier = getAllModules(itemStack).stream()
                    .map(itemModule -> itemModule.getSpeedMultiplierModifier(itemStack))
                    .reduce(speedModifier, (a, b) -> a*b);

            if (speedModifier < -4) {
                speedModifier = -3.9d;
            }

            if (isBroken(itemStack)) {
                damageModifier = 0;
                speedModifier = 2;
            }

            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", damageModifier, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", speedModifier, 0));
        }

        return multimap;
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return super.getArmorTexture(stack, entity, slot, type);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack itemStack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        if (state.getBlockHardness(worldIn, pos) > 0) {
            applyDamage(2, itemStack, entityLiving);
        }

        return true;
    }
}
