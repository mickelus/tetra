package se.mickelus.tetra.items.toolbelt.booster;

import baubles.api.BaublesApi;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import se.mickelus.tetra.items.ItemEffect;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.toolbelt.UtilToolbelt;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryQuickslot;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryStorage;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryToolbelt;

public class UtilBooster {

    public static final String activeKey = "booster.active";
    public static final String chargedKey = "booster.charged";
    public static final String fuelKey = "booster.fuel";
    public static final String bufferKey = "booster.buffer";
    public static final String cooldownKey = "booster.cooldown";

    public static final int fuelCapacity = 110;
    public static final int fuelCost = 1;
    public static final int fuelCostCharged = 40;
    public static final int fuelRecharge = 1;
    public static final int cooldownTicks = 20;

    public static final int gunpowderGain = 80;

    public static final float boostStrength = 0.035f;
    public static final float chargedBoostStrength = 1.2f;
    public static final float boostLevelMultiplier = 0.4f;

    public static boolean hasBooster(EntityPlayer player) {
        ItemStack itemStack = UtilToolbelt.findToolbelt(player);

        return canBoost(itemStack);
    }

    public static boolean canBoost(ItemStack itemStack) {
        return getBoosterLevel(itemStack) > 0;
    }

    public static int getBoosterLevel(ItemStack itemStack) {
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) itemStack.getItem();
            return item.getEffectLevel(itemStack, ItemEffect.booster);
        }

        return 0;
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

    public static void boostPlayer(EntityPlayer player, NBTTagCompound tag, int level) {
        float boostBase = boostStrength + boostStrength * (level - 1) * 0.4f;
        if (player.isElytraFlying()) {
            Vec3d vec3d = player.getLookVec();
            player.addVelocity(
                    vec3d.x * 0.01f + (vec3d.x * 1.5f - player.motionX) * 0.05f,
                    vec3d.y * 0.01f + (vec3d.y * 1.5f - player.motionY) * 0.05f,
                    vec3d.z * 0.01f + (vec3d.z * 1.5f - player.motionZ) * 0.05f);
        } else if (player.motionY > -0.1) {
            if (player.isSneaking()) {
                player.addVelocity(0, boostBase / 1.5, 0);
            } else {
                player.addVelocity(0, boostBase, 0);
            }
            player.fallDistance = 0;
        } else {
            player.addVelocity(0, boostBase + 0.8 * -player.motionY, 0);
        }

        if (player.world instanceof WorldServer) {
            ((WorldServer) player.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, player.posX - 0.2 + Math.random() * 0.4,
                    player.posY + Math.random() * 0.2, player.posZ - 0.2 + Math.random() * 0.4, 10, 0,
                    0, 0, 0.1D);
        }
    }

    public static void boostPlayerCharged(EntityPlayer player, NBTTagCompound tag, int level) {
        float boostBase = chargedBoostStrength + chargedBoostStrength * (level - 1) * boostLevelMultiplier;
        Vec3d lookVector = player.getLookVec();
        player.addVelocity(
                lookVector.x * boostBase,
                Math.max(lookVector.y * boostBase / 2, 0.1),
                lookVector.z * boostBase);
        player.velocityChanged = true;


        if (player.world instanceof WorldServer) {
            ((WorldServer)player.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, player.posX,
                    player.posY, player.posZ, 10, 0,
                    0, 0, 0.1D);
            ((WorldServer)player.world).spawnParticle(EnumParticleTypes.FLAME, player.posX,
                    player.posY, player.posZ, 3, 0,
                    0, 0, 0.1D);
        }
    }

    public static void consumeFuel(NBTTagCompound tag, boolean charged) {
        if (charged) {
            tag.setInteger(fuelKey, tag.getInteger(fuelKey) - fuelCostCharged);
        } else {
            tag.setInteger(fuelKey, tag.getInteger(fuelKey) - fuelCost);
        }
        tag.setInteger(cooldownKey, cooldownTicks);
    }

    public static void rechargeFuel(NBTTagCompound tag, ItemStack itemStack) {
        int fuel = tag.getInteger(fuelKey);
        int buffer = tag.getInteger(bufferKey);
        int cooldown = tag.getInteger(cooldownKey);
        if (cooldown > 0) {
            tag.setInteger(cooldownKey, cooldown - 1);
        } else if (fuel + fuelRecharge < fuelCapacity) {
            if (buffer > 0) {
                tag.setInteger(fuelKey, fuel + fuelRecharge);
                tag.setInteger(bufferKey, buffer - 1);
            } else {
                refuelBuffer(tag, itemStack);
            }
        }
    }

    private static void refuelBuffer(NBTTagCompound tag, ItemStack itemStack) {
        InventoryToolbelt inventory = new InventoryQuickslot(itemStack);
        int index = inventory.getFirstIndexForItem(Items.GUNPOWDER);
        if (index != -1) {
            inventory.decrStackSize(index, 1);
            tag.setInteger(bufferKey, gunpowderGain);
            return;
        }

        inventory = new InventoryStorage(itemStack);
        index = inventory.getFirstIndexForItem(Items.GUNPOWDER);
        if (index != -1) {
            inventory.decrStackSize(index, 1);
            tag.setInteger(bufferKey, gunpowderGain);
            return;
        }

        tag.setInteger(cooldownKey, cooldownTicks);
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
