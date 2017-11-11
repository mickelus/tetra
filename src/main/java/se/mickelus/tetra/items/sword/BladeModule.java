package se.mickelus.tetra.items.sword;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.WeaponModuleData;

public class BladeModule extends ItemModuleMajor<WeaponModuleData> {

    public static final String key = "basic_blade";
    public static final String materialKey = "basic_blade_material";

    public static BladeModule instance;

    public BladeModule(String slotKey) {
        super(slotKey, key, materialKey);

        data = new WeaponModuleData[] {
            new WeaponModuleData("basic_blade/oak", "minecraft:planks", 100, 0, 1, 0, 0x866526, 0, 0),
            new WeaponModuleData("basic_blade/cobblestone", "minecraft:cobblestone", 150, -1, 2, 0, 0x9a9a9a, 0, 0),
            new WeaponModuleData("basic_blade/stone", "minecraft:stone", 300, -1, 3, 0, 0x9a9a9a, 0, 0),
            new WeaponModuleData("basic_blade/iron", "minecraft:iron_ingot", 300, 0, 3, 0, 0xd8d8d8, 0, 0),
            new WeaponModuleData("basic_blade/gold", "minecraft:gold_ingot", 30, 0, 4, 0, 0xeaee57, 0, 0),
            new WeaponModuleData("basic_blade/diamond", "minecraft:diamond", 540, -1, 5, 0, 0x33ebcb, 0, 0),
            new WeaponModuleData("basic_blade/obsidian", "minecraft:obsidian", 720, -3, 5, 0, 0x3c3056, 0, 0)
        };

        instance = this;
        ItemUpgradeRegistry.instance.registerModule(key, this);
    }

    @Override
    public ItemStack[] removeModule(ItemStack targetStack, ItemStack[] tools) {
        return new ItemStack[0];
    }

    @Override
    public double getDamageModifier(ItemStack itemStack) {
        return getData(itemStack).damage;
    }

    @Override
    public void hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        attacker.world.getEntitiesWithinAABB(EntityLivingBase.class,
                target.getEntityBoundingBox().expand(1.0D, 0.25D, 1.0D))
                .stream()
                .filter(entity -> entity != attacker)
                .filter(entity -> !attacker.isOnSameTeam(entity))
                .filter(entity -> attacker.getDistanceSqToEntity(entity) < 9.0D)
                .forEach(entity -> {
                    entity.knockBack(attacker, 0.4F,
                            MathHelper.sin(attacker.rotationYaw * 0.017453292F),
                            -MathHelper.cos(attacker.rotationYaw * 0.017453292F));
                    if (attacker instanceof EntityPlayer) {
                        entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) attacker), 1);
                    } else {
                        entity.attackEntityFrom(DamageSource.causeIndirectDamage(attacker, entity), 1);
                    }
                });

        attacker.world.playSound(null, attacker.posX, attacker.posY, attacker.posZ,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, attacker.getSoundCategory(), 1.0F, 1.0F);
        spawnSweepParticles(attacker);
    }

    private void spawnSweepParticles(EntityLivingBase attacker) {
        double d0 = (double)(-MathHelper.sin(attacker.rotationYaw * 0.017453292F));
        double d1 = (double)MathHelper.cos(attacker.rotationYaw * 0.017453292F);

        if (attacker.world instanceof WorldServer)
        {
            ((WorldServer)attacker.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, attacker.posX + d0,
                    attacker.posY + attacker.height * 0.5D, attacker.posZ + d1, 0, d0,
                    0.0D, d1, 0.0D);
        }
    }
}
