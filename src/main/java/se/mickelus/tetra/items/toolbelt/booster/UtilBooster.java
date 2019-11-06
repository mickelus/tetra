package se.mickelus.tetra.items.toolbelt.booster;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.toolbelt.UtilToolbelt;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryQuickslot;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryStorage;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryToolbelt;
import se.mickelus.tetra.module.ItemEffect;

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

    public static boolean hasBooster(PlayerEntity player) {
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


    public static boolean hasFuel(CompoundNBT tag, boolean charged) {
        if (charged) {
            return tag.getInt(fuelKey) >= fuelCostCharged;
        }
        return tag.getInt(fuelKey) >= fuelCost;
    }

    public static int getFuel(CompoundNBT tag) {
        return tag.getInt(fuelKey);
    }

    public static float getFuelPercent(CompoundNBT tag) {
        return tag.getInt(fuelKey) * 1F / fuelCapacity;
    }

    public static void boostPlayer(PlayerEntity player, CompoundNBT tag, int level) {
        float boostBase = boostStrength + boostStrength * (level - 1) * 0.4f;
        if (player.isElytraFlying()) {
            Vec3d vec3d = player.getLookVec();
            player.addVelocity(
                    vec3d.x * 0.01f + (vec3d.x * 1.5f - player.getMotion().x) * 0.05f,
                    vec3d.y * 0.01f + (vec3d.y * 1.5f - player.getMotion().y) * 0.05f,
                    vec3d.z * 0.01f + (vec3d.z * 1.5f - player.getMotion().z) * 0.05f);
        } else if (player.getMotion().y > -0.1) {
            if (player.isSneaking()) {
                player.addVelocity(0, boostBase / 1.5, 0);
            } else {
                player.addVelocity(0, boostBase, 0);
            }
            player.fallDistance = 0;
        } else {
            player.addVelocity(0, boostBase + 0.8 * -player.getMotion().y, 0);
        }

        if (player.world instanceof ServerWorld) {
            ((ServerWorld) player.world).spawnParticle(ParticleTypes.SMOKE, player.posX - 0.2 + Math.random() * 0.4,
                    player.posY + Math.random() * 0.2, player.posZ - 0.2 + Math.random() * 0.4, 10, 0,
                    0, 0, 0.1D);
        }
    }

    public static void boostPlayerCharged(PlayerEntity player, CompoundNBT tag, int level) {
        float boostBase = chargedBoostStrength + chargedBoostStrength * (level - 1) * boostLevelMultiplier;
        Vec3d lookVector = player.getLookVec();
        player.addVelocity(
                lookVector.x * boostBase,
                Math.max(lookVector.y * boostBase / 2, 0.1),
                lookVector.z * boostBase);
        player.velocityChanged = true;


        if (player.world instanceof ServerWorld) {
            ((ServerWorld)player.world).spawnParticle(ParticleTypes.LARGE_SMOKE, player.posX,
                    player.posY, player.posZ, 10, 0,
                    0, 0, 0.1D);
            ((ServerWorld)player.world).spawnParticle(ParticleTypes.FLAME, player.posX,
                    player.posY, player.posZ, 3, 0,
                    0, 0, 0.1D);
        }
    }

    public static void consumeFuel(CompoundNBT tag, boolean charged) {
        if (charged) {
            tag.putInt(fuelKey, tag.getInt(fuelKey) - fuelCostCharged);
        } else {
            tag.putInt(fuelKey, tag.getInt(fuelKey) - fuelCost);
        }
        tag.putInt(cooldownKey, cooldownTicks);
    }

    public static void rechargeFuel(CompoundNBT tag, ItemStack itemStack) {
        int fuel = tag.getInt(fuelKey);
        int buffer = tag.getInt(bufferKey);
        int cooldown = tag.getInt(cooldownKey);
        if (cooldown > 0) {
            tag.putInt(cooldownKey, cooldown - 1);
        } else if (fuel + fuelRecharge < fuelCapacity) {
            if (buffer > 0) {
                tag.putInt(fuelKey, fuel + fuelRecharge);
                tag.putInt(bufferKey, buffer - 1);
            } else {
                refuelBuffer(tag, itemStack);
            }
        }
    }

    private static void refuelBuffer(CompoundNBT tag, ItemStack itemStack) {
        InventoryToolbelt inventory = new InventoryQuickslot(itemStack);
        int index = inventory.getFirstIndexForItem(Items.GUNPOWDER);
        if (index != -1) {
            inventory.decrStackSize(index, 1);
            tag.putInt(bufferKey, gunpowderGain);
            return;
        }

        inventory = new InventoryStorage(itemStack);
        index = inventory.getFirstIndexForItem(Items.GUNPOWDER);
        if (index != -1) {
            inventory.decrStackSize(index, 1);
            tag.putInt(bufferKey, gunpowderGain);
            return;
        }

        tag.putInt(cooldownKey, cooldownTicks);
    }

    public static boolean isActive(CompoundNBT tag) {
        return tag.getBoolean(activeKey);
    }

    public static void setActive(CompoundNBT tag, boolean active, boolean charged) {
        tag.putBoolean(activeKey, active);
        if (charged) {
            tag.putBoolean(chargedKey, charged);
        }
    }

}
