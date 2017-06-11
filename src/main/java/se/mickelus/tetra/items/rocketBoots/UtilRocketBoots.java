package se.mickelus.tetra.items.rocketBoots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

public class UtilRocketBoots {

    public static final String activeKey = "active";
    public static final String chargedKey = "charged";
    public static final String fuelKey = "fuel";
    public static final String cooldownKey = "cooldown";

    public static final int fuelCapacity = 100;
    public static final int fuelCost = 3;
    public static final int fuelCostCharged = 20;
    public static final int fuelRecharge = 1;
    public static final int cooldownTicks = 80;

    public static boolean hasBoots(EntityPlayer player) {
        ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);

        return stack != null && stack.getItem() instanceof ItemRocketBoots && stack.hasTagCompound();
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
        tag.setInteger(cooldownKey, cooldownTicks);
    }

    public static void rechargeFuel(NBTTagCompound tag) {
        int fuel = tag.getInteger(fuelKey);
        int cooldown = tag.getInteger(cooldownKey);
        if (cooldown > 0) {
            tag.setInteger(cooldownKey, cooldown - 1);
        } else if (fuel + fuelRecharge < fuelCapacity) {
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
