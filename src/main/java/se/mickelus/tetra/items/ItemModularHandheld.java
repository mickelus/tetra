package se.mickelus.tetra.items;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.PotionBleeding;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.module.ItemEffectHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ItemModularHandheld extends ItemModular {

    private static final Set<Block> axeBlocks = Sets.newHashSet(Blocks.PLANKS, Blocks.BOOKSHELF, Blocks.LOG, Blocks.LOG2, Blocks.CHEST, Blocks.PUMPKIN, Blocks.LIT_PUMPKIN, Blocks.MELON_BLOCK, Blocks.LADDER, Blocks.WOODEN_BUTTON, Blocks.WOODEN_PRESSURE_PLATE);
    private static final Set<Material> axeMaterials = Sets.newHashSet(Material.WOOD);

    private static final Set<Block> pickaxeBlocks = Sets.newHashSet(Blocks.ACTIVATOR_RAIL, Blocks.COAL_ORE, Blocks.COBBLESTONE, Blocks.DETECTOR_RAIL, Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.DOUBLE_STONE_SLAB, Blocks.GOLDEN_RAIL, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.ICE, Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE, Blocks.LIT_REDSTONE_ORE, Blocks.MOSSY_COBBLESTONE, Blocks.NETHERRACK, Blocks.PACKED_ICE, Blocks.RAIL, Blocks.REDSTONE_ORE, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.STONE, Blocks.STONE_SLAB, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE);
    private static final Set<Material> pickaxeMaterials = Sets.newHashSet(Material.IRON, Material.ANVIL, Material.ROCK);

    private static final Set<Block> shovelBlocks = Sets.newHashSet(Blocks.CLAY, Blocks.DIRT, Blocks.FARMLAND, Blocks.GRASS, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.SNOW, Blocks.SNOW_LAYER, Blocks.SOUL_SAND, Blocks.GRASS_PATH, Blocks.CONCRETE_POWDER);


    private static final Set<Material> cuttingMaterials = Sets.newHashSet(Material.PLANTS, Material.VINE, Material.CORAL, Material.LEAVES, Material.GOURD, Material.WEB, Material.CLOTH);

    protected static final UUID ARMOR_MODIFIER = UUID.fromString("D96050BE-6A94-4A27-AA0B-2AF705327BA4");

    // the base amount of damage the item should take after destroying a block
    protected int blockDestroyDamage = 1;

    // the base amount of damage the item should take after hitting an entity
    protected int entityHitDamage = 1;

    @Override
    public boolean onBlockDestroyed(ItemStack itemStack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        if (state.getBlockHardness(worldIn, pos) > 0) {
            applyDamage(blockDestroyDamage, itemStack, entityLiving);
        }

        return true;
    }


    @Override
    public boolean hitEntity(ItemStack itemStack, EntityLivingBase target, EntityLivingBase attacker) {
        applyDamage(entityHitDamage, itemStack, attacker);
        if (!isBroken(itemStack)) {
            getAllModules(itemStack).forEach(module -> module.hitEntity(itemStack, target, attacker));

            int fieryLevel = getEffectLevel(itemStack, ItemEffect.fiery);
            if (fieryLevel > 0) {
                target.setFire(fieryLevel * 4);
            }

            int knockbackLevel = getEffectLevel(itemStack, ItemEffect.knockback);
            if (knockbackLevel > 0) {
                target.knockBack(attacker, knockbackLevel * 0.5f,
                        MathHelper.sin(attacker.rotationYaw * 0.017453292F),
                        -MathHelper.cos(attacker.rotationYaw * 0.017453292F));
            }

            int sweepingLevel = getEffectLevel(itemStack, ItemEffect.sweeping);
            if (sweepingLevel > 0) {
                sweepAttack(itemStack, target, attacker, sweepingLevel, knockbackLevel);
            }

            int bleedingLevel = getEffectLevel(itemStack, ItemEffect.bleeding);
            if (bleedingLevel > 0) {
                if (!EnumCreatureAttribute.UNDEAD.equals(target.getCreatureAttribute()) && Math.random() > 0.7f) {
                    target.addPotionEffect(new PotionEffect(PotionBleeding.instance, 40, bleedingLevel));
                }
            }

            int arthropodLevel = getEffectLevel(itemStack, ItemEffect.arthropod);
            if (arthropodLevel > 0 && EnumCreatureAttribute.ARTHROPOD.equals(target.getCreatureAttribute())) {
                int ticks = 20 + attacker.getRNG().nextInt(10 * arthropodLevel);
                target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, ticks, 3));
            }
        }

        return true;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = player.getHeldItem(hand);
        int flatteningLevel = getEffectLevel(itemStack, ItemEffect.flattening);
        int tillingLevel = getEffectLevel(itemStack, ItemEffect.tilling);

        if (flatteningLevel > 0 && (tillingLevel > 0 && player.isSneaking() || tillingLevel == 0)) {
            return flattenPath(player, world, pos, hand, facing);
        } else if (tillingLevel > 0) {
            return tillBlock(player, world, pos, hand, facing);
        }

        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    /**
     * Flattens grass into a path similar to how vanilla shovels does it.
     * @param player the responsible player entity
     * @param world the world in which the action takes place
     * @param pos the position in the world
     * @param hand the hand holding the tool
     * @param facing the clicked face
     * @return EnumActionResult.SUCCESS if successful, EnumActionResult.FAIL if block cannot be edited by player,
     * otherwise EnumActionResult.PASS
     */
    public EnumActionResult flattenPath(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing) {
        ItemStack itemStack = player.getHeldItem(hand);

        if (!player.canPlayerEdit(pos.offset(facing), facing, itemStack)) {
            return EnumActionResult.FAIL;
        }

        if (facing != EnumFacing.DOWN && world.getBlockState(pos.up()).getMaterial() == Material.AIR
                && world.getBlockState(pos).getBlock() == Blocks.GRASS) {
            world.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1, 1);

            if (!world.isRemote) {
                world.setBlockState(pos, Blocks.GRASS_PATH.getDefaultState(), 11);
                applyDamage(blockDestroyDamage, itemStack, player);
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    /**
     * Tills dirt or grass, turning it into farmland. Tilling coarse dirt turns it into dirt.
     * @param player the responsible player entity
     * @param world the world in which the action takes place
     * @param pos the position in the world
     * @param hand the hand holding the tool
     * @param facing the clicked face
     * @return EnumActionResult.SUCCESS if successful, EnumActionResult.FAIL if block cannot be edited by player,
     * otherwise EnumActionResult.PASS
     */
    public EnumActionResult tillBlock(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing) {
        ItemStack itemStack = player.getHeldItem(hand);

        if (!player.canPlayerEdit(pos.offset(facing), facing, itemStack)) {
            return EnumActionResult.FAIL;
        }

        // fire the forge event manually as the helper damages durability
        UseHoeEvent event = new UseHoeEvent(player, itemStack, world, pos);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            return EnumActionResult.FAIL;
        }
        if (event.getResult() == Event.Result.ALLOW) {
            applyDamage(blockDestroyDamage, itemStack, player);
            return EnumActionResult.SUCCESS;
        }

        IBlockState currentState = world.getBlockState(pos);
        Block block = currentState.getBlock();

        if (facing != EnumFacing.DOWN && world.isAirBlock(pos.up())) {
            IBlockState newState = null;

            if (block == Blocks.GRASS || block == Blocks.GRASS_PATH
                    || BlockDirt.DirtType.DIRT == currentState.getValue(BlockDirt.VARIANT)) {
                newState = Blocks.FARMLAND.getDefaultState();
            } else if (BlockDirt.DirtType.COARSE_DIRT == currentState.getValue(BlockDirt.VARIANT)) {
                newState = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT);
            }

            if (newState != null) {
                world.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1, 1);

                if (!world.isRemote) {
                    world.setBlockState(pos, newState, 11);
                    applyDamage(blockDestroyDamage, itemStack, player);
                }

                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    /**
     * Perfoms a sweeping attack, dealing damage and playing effects similar to vanilla swords.
     * @param itemStack the itemstack used for the attack
     * @param target the attacking entity
     * @param attacker the attacked entity
     * @param sweepingLevel the level of the sweeping effect of the itemstack
     * @param knockbackLevel the level of the knockback effect of the itemstack
     */
    private void sweepAttack(ItemStack itemStack, EntityLivingBase target, EntityLivingBase attacker, int sweepingLevel, int knockbackLevel) {
        float cooldown = 1;
        if (attacker instanceof EntityPlayer) {
            cooldown = ItemModularHandheld.getCooledAttackStrength(itemStack);
        }

        if (cooldown > 0.9) {
            float damage = (float) Math.max((getDamageModifier(itemStack) + 1) * (sweepingLevel * 0.125f), 1);
            float knockback = sweepingLevel > 4 ? (knockbackLevel + 1) * 0.5f : 0.5f;

            attacker.world.getEntitiesWithinAABB(EntityLivingBase.class,
                    target.getEntityBoundingBox().expand(1.0d, 0.25d, 1.0d)).stream()
                    .filter(entity -> entity != attacker)
                    .filter(entity -> !attacker.isOnSameTeam(entity))
                    .filter(entity -> attacker.getDistanceSq(entity) < 9.0D)
                    .forEach(entity -> {
                        entity.knockBack(attacker, knockback,
                                MathHelper.sin(attacker.rotationYaw * 0.017453292f),
                                -MathHelper.cos(attacker.rotationYaw * 0.017453292f));
                        if (attacker instanceof EntityPlayer) {
                            entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) attacker), damage);
                        } else {
                            entity.attackEntityFrom(DamageSource.causeIndirectDamage(attacker, entity), damage);
                        }
                    });

            attacker.world.playSound(null, attacker.posX, attacker.posY, attacker.posZ,
                    SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, attacker.getSoundCategory(), 1.0F, 1.0F);
            spawnSweepParticles(attacker);
        }
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

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        setCooledAttackStrength(stack, player.getCooledAttackStrength(0.5f));
        return false;
    }

    public void setCooledAttackStrength(ItemStack itemStack, float strength) {
        NBTHelper.getTag(itemStack).setFloat(cooledStrengthKey, strength);
    }

    public static float getCooledAttackStrength(ItemStack itemStack) {
        return NBTHelper.getTag(itemStack).getFloat(cooledStrengthKey);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack itemStack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, itemStack);

        if (slot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", getDamageModifier(itemStack), 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", getSpeedModifier(itemStack), 0));
        }

        if (slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND) {
            int armor = getEffectLevel(itemStack, ItemEffect.armor);
            if  (armor > 0) {
                multimap.put(SharedMonsterAttributes.ARMOR.getName(),
                        new AttributeModifier(ARMOR_MODIFIER, "Weapon modifier", armor, 0));
            }
        }

        return multimap;
    }

    public double getDamageModifier(ItemStack itemStack) {
        if (isBroken(itemStack)) {
            return 0;
        }

        double damageModifier = getAllModules(itemStack).stream()
            .map(itemModule -> itemModule.getDamageModifier(itemStack))
            .reduce(0d, Double::sum);

        damageModifier = Arrays.stream(getSynergyData(itemStack))
            .map(synergyData -> synergyData.damage)
            .reduce(damageModifier, Double::sum);

        return getAllModules(itemStack).stream()
            .map(itemModule -> itemModule.getDamageMultiplierModifier(itemStack))
            .reduce(damageModifier, (a, b) -> a*b);
    }

    public static double getDamageModifierStatic(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemModularHandheld) {
            return ((ItemModularHandheld) itemStack.getItem()).getDamageModifier(itemStack);
        }
        return 0;
    }

    public double getSpeedModifier(ItemStack itemStack) {
        double speedModifier = getAllModules(itemStack).stream()
            .map(itemModule -> itemModule.getSpeedModifier(itemStack))
            .reduce(-2.4d, Double::sum);

        speedModifier = Arrays.stream(getSynergyData(itemStack))
            .map(synergyData -> synergyData.speed)
            .reduce(speedModifier, Double::sum);

        speedModifier = getAllModules(itemStack).stream()
            .map(itemModule -> itemModule.getSpeedMultiplierModifier(itemStack))
            .reduce(speedModifier, (a, b) -> a*b);

        speedModifier *= getCounterWeightMultiplier(itemStack);

        if (speedModifier < -4) {
            speedModifier = -3.9d;
        }

        return speedModifier;
    }

    public double getCounterWeightMultiplier(ItemStack itemStack) {
        int counterWeightLevel = getEffectLevel(itemStack, ItemEffect.counterweight);
        if (counterWeightLevel > 0) {
            int integrityCost = getIntegrityCost(itemStack);

            return 1.5 - Math.abs(counterWeightLevel + integrityCost) * 0.2;
        }
        return 1;
    }

    public static double getSpeedModifierStatic(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemModularHandheld) {
            return ((ItemModularHandheld) itemStack.getItem()).getSpeedModifier(itemStack);
        }
        return 2;
    }

    @Override
    public Set<String> getToolClasses(ItemStack itemStack) {
        if (!isBroken(itemStack)) {
            return getCapabilities(itemStack).stream()
                .map(Enum::toString)
                .collect(Collectors.toSet());
        }
        return Collections.EMPTY_SET;
    }

    @Override
    public int getHarvestLevel(ItemStack itemStack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState) {
        if (!isBroken(itemStack)) {
            int capabilityLevel = getCapabilityLevel(itemStack, toolClass);
            if (capabilityLevel > 0) {
                return capabilityLevel - 1;
            }
        }
        return -1;
    }

    @Override
    public boolean canHarvestBlock(IBlockState blockState, ItemStack itemStack) {
        if (pickaxeMaterials.contains(blockState.getMaterial())) {
            return getHarvestLevel(itemStack, "pickaxe", null, null) >= 0;
        } else if (blockState.getBlock().equals(Blocks.WEB)) {
            return getHarvestLevel(itemStack, "cut", null, null) >= 0;
        }
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack itemStack, IBlockState blockState) {
        if (!isBroken(itemStack)) {
            String tool = getEffectiveTool(blockState);

            float speed = (float) (4 + getSpeedModifier(itemStack)) * getCapabilityEfficiency(itemStack, tool);

            // todo: need a better way to handle this
            if ("cut".equals(tool) && blockState.getBlock().equals(Blocks.WEB)) {
                speed *= 10;
            }

            if (speed < 1) {
                return 1;
            }
            return speed;
        }
        return 1;
    }

    public static String getEffectiveTool(IBlockState blockState) {
        String tool = blockState.getBlock().getHarvestTool(blockState);

        if (tool != null) {
            return tool;
        }

        if (axeMaterials.contains(blockState.getMaterial())) {
            return "axe";
        } else if (pickaxeMaterials.contains(blockState.getMaterial())) {
            return "pickaxe";
        } else if (axeBlocks.contains(blockState.getBlock())) {
            return "axe";
        } else if (pickaxeBlocks.contains(blockState.getBlock())) {
            return "pickaxe";
        } else if (cuttingMaterials.contains(blockState.getMaterial())) {
            return "cut";
        } else if (shovelBlocks.contains(blockState.getBlock())) {
            return "shovel";
        }
        return null;
    }
}
