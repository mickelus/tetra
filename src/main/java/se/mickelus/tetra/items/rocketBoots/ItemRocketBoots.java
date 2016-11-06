package se.mickelus.tetra.items.rocketBoots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import se.mickelus.tetra.TetraCreativeTabs;

public class ItemRocketBoots extends ItemArmor {

    private static ItemRocketBoots instance;
    private final String unlocalizedName = "rocket_boots";

    private static final String activeKey = "active";
    private static final String chargedKey = "charged";
    private static final String fuelKey = "fuel";

    private static final int fuelCapacity = 100;
    private static final int fuelCost = 3;
    private static final int fuelCostCharged = 20;
    private static final int fuelRecharge = 1;

    public ItemRocketBoots() {
        super(ArmorMaterial.LEATHER, 1, EntityEquipmentSlot.FEET);

        maxStackSize = 1;

        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        GameRegistry.register(this);
        setCreativeTab(TetraCreativeTabs.getInstance());

        instance = this;
    }

    public static ItemRocketBoots getInstance() {
        return instance;
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound tag = stack.getTagCompound();
        boolean charged = tag.getBoolean(chargedKey);
        if (isActive(tag) && hasFuel(tag, charged)) {
            if (charged) {
                boostPlayerCharged(player, tag);
            } else {
                boostPlayer(player, tag);
            }

            consumeFuel(tag, charged);
        } else {
            rechargeFuel(tag);
        }

        if (charged) {
            tag.setBoolean(chargedKey, false);
        }
    }

    public static boolean hasFuel(NBTTagCompound tag, boolean charged) {
        if (charged) {
            return tag.getInteger(fuelKey) >= fuelCostCharged;
        }
        return tag.getInteger(fuelKey) >= fuelCost;
    }

    public static int getFuel(NBTTagCompound tag) {
        return tag.getInteger(fuelKey);
    }

    public static float getFuelPercent(NBTTagCompound tag) {
        return tag.getInteger(fuelKey) * 1F / fuelCapacity;
    }

    public static void boostPlayer(EntityPlayer player, NBTTagCompound tag) {
        player.addVelocity(0, 0.1, 0);

        if (player.motionY > -0.1) {
            player.fallDistance = 0;
        }
    }

    public static void boostPlayerCharged(EntityPlayer player, NBTTagCompound tag) {
        System.out.println("boosting");

        Vec3d lookVector = player.getLookVec();
        //player.addVelocity(0, 5, 0);
        player.addVelocity(lookVector.xCoord, Math.max(lookVector.yCoord * 0.06, 0.2), lookVector.zCoord);
    }

    public static void consumeFuel(NBTTagCompound tag, boolean charged) {
        if (charged) {
            tag.setInteger(fuelKey, tag.getInteger(fuelKey) - fuelCostCharged);
        } else {
            tag.setInteger(fuelKey, tag.getInteger(fuelKey) - fuelCost);
        }
    }

    public static void rechargeFuel(NBTTagCompound tag) {
        int fuel = tag.getInteger(fuelKey);
        if (fuel + fuelRecharge < fuelCapacity) {
            tag.setInteger(fuelKey, fuel + fuelRecharge);
        }
    }

    public static boolean isActive(NBTTagCompound tag) {
        return tag.getBoolean(activeKey);
    }

    public static void setActive(NBTTagCompound tag, boolean active, boolean charged) {
        tag.setBoolean(activeKey, active);
        if (charged) {
            tag.setBoolean(chargedKey, charged);
        }
    }

}
