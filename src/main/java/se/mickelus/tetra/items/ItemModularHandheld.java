package se.mickelus.tetra.items;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.oredict.OreDictionary;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.PotionBleeding;
import se.mickelus.tetra.PotionEarthbound;
import se.mickelus.tetra.blocks.workbench.BlockWorkbench;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.module.ItemEffectHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemModularHandheld extends ItemModular {

    private static final Set<Block> axeBlocks = Sets.newHashSet(Blocks.PLANKS, Blocks.BOOKSHELF, Blocks.LOG, Blocks.LOG2, Blocks.CHEST, Blocks.PUMPKIN, Blocks.LIT_PUMPKIN, Blocks.MELON_BLOCK, Blocks.LADDER, Blocks.WOODEN_BUTTON, Blocks.WOODEN_PRESSURE_PLATE);
    private static final Set<Material> axeMaterials = Sets.newHashSet(Material.WOOD);

    private static final Set<Block> pickaxeBlocks = Sets.newHashSet(Blocks.ACTIVATOR_RAIL, Blocks.COAL_ORE, Blocks.COBBLESTONE, Blocks.DETECTOR_RAIL, Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.DOUBLE_STONE_SLAB, Blocks.GOLDEN_RAIL, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.ICE, Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE, Blocks.LIT_REDSTONE_ORE, Blocks.MOSSY_COBBLESTONE, Blocks.NETHERRACK, Blocks.PACKED_ICE, Blocks.RAIL, Blocks.REDSTONE_ORE, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.STONE, Blocks.STONE_SLAB, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE);
    private static final Set<Material> pickaxeMaterials = Sets.newHashSet(Material.IRON, Material.ANVIL, Material.ROCK);

    private static final Set<Block> shovelBlocks = Sets.newHashSet(Blocks.CLAY, Blocks.DIRT, Blocks.FARMLAND, Blocks.GRASS, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.SNOW, Blocks.SNOW_LAYER, Blocks.SOUL_SAND, Blocks.GRASS_PATH, Blocks.CONCRETE_POWDER);

    private static final Set<Material> cuttingMaterials = Sets.newHashSet(Material.PLANTS, Material.VINE, Material.CORAL, Material.LEAVES, Material.GOURD, Material.WEB, Material.CLOTH);

    private static final String[] denailOreDict = new String[] { "plankWood", "slabWood", "stairWood", "fenceWood", "fenceGateWood", "doorWood", "chestWood"};
    private static final List<Predicate<IBlockState>> denailBlocks = ImmutableList.of(
            BlockMatcher.forBlock(Blocks.CRAFTING_TABLE),
            BlockStateMatcher.forBlock(BlockWorkbench.instance).where(BlockWorkbench.propVariant, Predicates.equalTo(BlockWorkbench.Variant.wood)),
            BlockMatcher.forBlock(Blocks.BOOKSHELF),
            BlockMatcher.forBlock(Blocks.TRAPPED_CHEST),
            BlockMatcher.forBlock(Blocks.RAIL),
            BlockMatcher.forBlock(Blocks.ACTIVATOR_RAIL),
            BlockMatcher.forBlock(Blocks.DETECTOR_RAIL),
            BlockMatcher.forBlock(Blocks.GOLDEN_RAIL),
            BlockMatcher.forBlock(Blocks.STANDING_SIGN),
            BlockMatcher.forBlock(Blocks.WALL_SIGN),
            BlockMatcher.forBlock(Blocks.BED),
            BlockMatcher.forBlock(Blocks.LADDER),
            BlockMatcher.forBlock(Blocks.JUKEBOX),
            BlockMatcher.forBlock(Blocks.NOTEBLOCK),
            BlockMatcher.forBlock(Blocks.TRAPDOOR),
            BlockMatcher.forBlock(Blocks.WOODEN_PRESSURE_PLATE),
            BlockMatcher.forBlock(Blocks.WOODEN_BUTTON),
            BlockMatcher.forBlock(Blocks.DAYLIGHT_DETECTOR),
            BlockMatcher.forBlock(Blocks.DAYLIGHT_DETECTOR_INVERTED)
            );

    protected static final UUID ARMOR_MODIFIER = UUID.fromString("D96050BE-6A94-4A27-AA0B-2AF705327BA4");

    // the base amount of damage the item should take after destroying a block
    protected int blockDestroyDamage = 1;

    // the base amount of damage the item should take after hitting an entity
    protected int entityHitDamage = 1;

    @Override
    public boolean onBlockDestroyed(ItemStack itemStack, World world, IBlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getBlockHardness(world, pos) > 0) {
            applyDamage(blockDestroyDamage, itemStack, entity);

            if (!isBroken(itemStack)) {
                tickProgression(entity, itemStack, 1);
            }
        }

        if (!world.isRemote) {
            int intuitLevel = getEffectLevel(itemStack, ItemEffect.intuit);
            if (intuitLevel > 0) {
                int xp = state.getBlock().getExpDrop(state, world, pos, getEffectLevel(itemStack, ItemEffect.fortune));
                if (xp > 0) {
                    tickHoningProgression(entity, itemStack, xp);
                }
            }
        }

        causeFierySelfEffect(entity, itemStack, 1);
        causeEnderReverbEffect(entity, itemStack, 1);

        return true;
    }

    @Override
    public boolean hitEntity(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
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
                if (!EnumCreatureAttribute.UNDEAD.equals(target.getCreatureAttribute()) && attacker.getRNG().nextFloat() < 0.3f) {
                    target.addPotionEffect(new PotionEffect(PotionBleeding.instance, 40, bleedingLevel));
                }
            }

            int arthropodLevel = getEffectLevel(itemStack, ItemEffect.arthropod);
            if (arthropodLevel > 0 && EnumCreatureAttribute.ARTHROPOD.equals(target.getCreatureAttribute())) {
                int ticks = 20 + attacker.getRNG().nextInt(10 * arthropodLevel);
                target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, ticks, 3));
            }

            // todo: only trigger if target is standing on stone/earth/sand/gravel
            int earthbindLevel = getEffectLevel(itemStack, ItemEffect.earthbind);
            if (earthbindLevel > 0 && attacker.getRNG().nextFloat() < Math.max(0.1, 0.5 * ( 1 - target.posY  / 128 ))) {
                target.addPotionEffect(new PotionEffect(PotionEarthbound.instance, 80, 0, false, true));

                if (target.world instanceof WorldServer) {
                    ((WorldServer)target.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, target.posX, target.posY + 0.1, target.posZ,
                            16, 0, 0.1,0, target.world.rand.nextGaussian() * 0.2,
                            Block.getStateId(target.world.getBlockState(new BlockPos(target.posX, target.posY - 1, target.posZ))));
                }
            }

            causeFierySelfEffect(attacker, itemStack, 1.4);
            causeEnderReverbEffect(attacker, itemStack, 1.5);

            tickProgression(attacker, itemStack, 1);
        }

        return true;
    }

    @Override
    public EnumActionResult onItemUse(PlayerEntity player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = player.getHeldItem(hand);
        int flatteningLevel = getEffectLevel(itemStack, ItemEffect.flattening);
        int tillingLevel = getEffectLevel(itemStack, ItemEffect.tilling);

        causeFierySelfEffect(player, itemStack, 2);
        causeEnderReverbEffect(player, itemStack, 1.7);

        if (flatteningLevel > 0 && (tillingLevel > 0 && player.isSneaking() || tillingLevel == 0)) {
            return flattenPath(player, world, pos, hand, facing);
        } else if (tillingLevel > 0) {
            return tillBlock(player, world, pos, hand, facing);
        }


        int denailingLevel = getEffectLevel(itemStack, ItemEffect.denailing);
        if (denailingLevel > 0 && player.getCooledAttackStrength(0) > 0.9) {
            EnumActionResult result = denailBlock(player, world, pos, hand, facing);

            if (result.equals(EnumActionResult.SUCCESS)) {
                player.resetCooldown();
            }

            return result;
        }

        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    protected void causeFierySelfEffect(LivingEntity entity, ItemStack itemStack, double multiplier) {
        if (!entity.world.isRemote) {
            double fierySelfEfficiency = getEffectEfficiency(itemStack, ItemEffect.fierySelf);
            if (fierySelfEfficiency > 0) {
                BlockPos pos = entity.getPosition();
                float temperature = entity.world.getBiome(pos).getTemperature(pos);
                if (entity.getRNG().nextDouble() < fierySelfEfficiency * temperature * multiplier) {
                    entity.setFire(getEffectLevel(itemStack, ItemEffect.fierySelf));
                }
            }
        }
    }
    
    protected void causeEnderReverbEffect(LivingEntity entity, ItemStack itemStack, double multiplier) {
        if (!entity.world.isRemote) {
            double effectProbability = getEffectEfficiency(itemStack, ItemEffect.enderReverb);
            if (effectProbability > 0) {
                if (entity.getRNG().nextDouble() < effectProbability * multiplier) {
                    AxisAlignedBB aabb = new AxisAlignedBB(entity.getPosition()).grow(24);
                    List<LivingEntity> nearbyTargets = entity.world.getEntitiesWithinAABB(LivingEntity.class, aabb,
                            target -> target instanceof EntityEnderman || target instanceof EntityEndermite || target instanceof EntityShulker || target instanceof EntityDragon);
                    if (nearbyTargets.size() > 0) {
                        nearbyTargets.get(entity.getRNG().nextInt(nearbyTargets.size())).setRevengeTarget(entity);
                    }
                }
            }
        }
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
    public EnumActionResult flattenPath(PlayerEntity player, World world, BlockPos pos, EnumHand hand, EnumFacing facing) {
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
                tickProgression(player, itemStack, blockDestroyDamage);
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
    public EnumActionResult tillBlock(PlayerEntity player, World world, BlockPos pos, EnumHand hand, EnumFacing facing) {
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
            tickProgression(player, itemStack, blockDestroyDamage);
            return EnumActionResult.SUCCESS;
        }

        IBlockState currentState = world.getBlockState(pos);
        Block block = currentState.getBlock();

        if (facing != EnumFacing.DOWN && world.isAirBlock(pos.up())) {
            IBlockState newState = null;

            if (block == Blocks.GRASS || block == Blocks.GRASS_PATH
                    || (block == Blocks.DIRT && BlockDirt.DirtType.DIRT == currentState.getValue(BlockDirt.VARIANT))) {
                newState = Blocks.FARMLAND.getDefaultState();
            } else if (block == Blocks.DIRT && BlockDirt.DirtType.COARSE_DIRT == currentState.getValue(BlockDirt.VARIANT)) {
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
     * Instantly break plank based blocks.
     * @param player the responsible player entity
     * @param world the world in which the action takes place
     * @param pos the position in the world
     * @param hand the hand holding the tool
     * @param facing the clicked face
     * @return EnumActionResult.SUCCESS if successful, EnumActionResult.FAIL if block cannot be edited by player,
     * otherwise EnumActionResult.PASS
     */
    public EnumActionResult denailBlock(PlayerEntity player, World world, BlockPos pos, EnumHand hand, EnumFacing facing) {
        ItemStack itemStack = player.getHeldItem(hand);

        if (!player.canPlayerEdit(pos.offset(facing), facing, itemStack)) {
            return EnumActionResult.FAIL;
        }

        IBlockState blockState = world.getBlockState(pos);
        if (canDenail(blockState, world, pos)) {
            boolean success = ItemEffectHandler.breakBlock(world, player, player.getHeldItem(hand), pos, blockState);
            if (success) {
                applyDamage(blockDestroyDamage, itemStack, player);
                tickProgression(player, itemStack, blockDestroyDamage);

                world.playEvent(player, 2001, pos, Block.getStateId(blockState));
                player.resetCooldown();
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    private boolean canDenail(IBlockState blockState, World world, BlockPos pos) {
        boolean matchOre = Optional.of(blockState.getBlock().getPickBlock(blockState, null, world, pos, null))
                .map(OreDictionary::getOreIDs)
                .map(Stream::of)
                .orElseGet(Stream::empty)
                .flatMapToInt(Arrays::stream)
                .mapToObj(OreDictionary::getOreName)
                .anyMatch(ore1 -> Arrays.asList(denailOreDict).contains(ore1));

        if (matchOre) {
            return true;
        }

        return denailBlocks.stream()
                .anyMatch(predicate -> predicate.test(blockState));
    }

    /**
     * Perfoms a sweeping attack, dealing damage and playing effects similar to vanilla swords.
     * @param itemStack the itemstack used for the attack
     * @param target the attacking entity
     * @param attacker the attacked entity
     * @param sweepingLevel the level of the sweeping effect of the itemstack
     * @param knockbackLevel the level of the knockback effect of the itemstack
     */
    private void sweepAttack(ItemStack itemStack, LivingEntity target, LivingEntity attacker, int sweepingLevel, int knockbackLevel) {
        float cooldown = 1;
        if (attacker instanceof PlayerEntity) {
            cooldown = ItemModularHandheld.getCooledAttackStrength(itemStack);
        }

        if (cooldown > 0.9) {
            float damage = (float) Math.max((getDamageModifier(itemStack) + 1) * (sweepingLevel * 0.125f), 1);
            float knockback = sweepingLevel > 4 ? (knockbackLevel + 1) * 0.5f : 0.5f;
            double range = 1 + getEffectEfficiency(itemStack, ItemEffect.sweeping);

            // range values set up to mimic vanilla behaviour
            attacker.world.getEntitiesWithinAABB(LivingEntity.class,
                    target.getEntityBoundingBox().grow(range, 0.25d, range)).stream()
                    .filter(entity -> entity != attacker)
                    .filter(entity -> !attacker.isOnSameTeam(entity))
                    .filter(entity -> attacker.getDistanceSq(entity) < (range + 2) * (range + 2))
                    .forEach(entity -> {
                        entity.knockBack(attacker, knockback,
                                MathHelper.sin(attacker.rotationYaw * 0.017453292f),
                                -MathHelper.cos(attacker.rotationYaw * 0.017453292f));
                        if (attacker instanceof PlayerEntity) {
                            entity.attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity) attacker), damage);
                        } else {
                            entity.attackEntityFrom(DamageSource.causeIndirectDamage(attacker, entity), damage);
                        }
                    });

            attacker.world.playSound(null, attacker.posX, attacker.posY, attacker.posZ,
                    SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, attacker.getSoundCategory(), 1.0F, 1.0F);
            spawnSweepParticles(attacker);
        }
    }

    /**
     * Spawns sweeping particles in the given world at the given coordinates. Similar to the sweeping particle used
     * by vanilla swords.
     * @param world The world in which to spawn the particle
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param xOffset x offset which is later multiplied by a random number (0-1)
     * @param zOffset z offset which is later multiplied by a random number (0-1)
     */
    public static void spawnSweepParticles(World world, double x, double y, double z, double xOffset, double zOffset) {
        if (world instanceof WorldServer) {
            ((WorldServer)world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, x, y, z,
                    1, xOffset, 0, zOffset, 0);
        }
    }

    public static void spawnSweepParticles(LivingEntity attacker) {
        double xOffset = -MathHelper.sin(attacker.rotationYaw * 0.017453292F);
        double zOffset = MathHelper.cos(attacker.rotationYaw * 0.017453292F);

        spawnSweepParticles(attacker.world, attacker.posX + xOffset, attacker.posY + attacker.height * 0.5D, attacker.posZ + zOffset, xOffset, zOffset);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
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
                .mapToDouble(itemModule -> itemModule.getDamageModifier(itemStack))
                .sum();

        damageModifier = Arrays.stream(getSynergyData(itemStack))
                .mapToDouble(synergyData -> synergyData.damage)
                .reduce(damageModifier, Double::sum);

        damageModifier = Arrays.stream(getSynergyData(itemStack))
                .mapToDouble(synergyData -> synergyData.damageMultiplier)
                .reduce(damageModifier, (a, b) -> a * b);

        return getAllModules(itemStack).stream()
                .map(itemModule -> itemModule.getDamageMultiplierModifier(itemStack))
                .reduce(damageModifier, (a, b) -> a * b);
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
                .mapToDouble(synergyData -> synergyData.attackSpeed)
                .reduce(speedModifier, Double::sum);

        speedModifier = Arrays.stream(getSynergyData(itemStack))
                .mapToDouble(synergyData -> synergyData.attackSpeedMultiplier)
                .reduce(speedModifier, (a, b) -> a * b);

        speedModifier = getAllModules(itemStack).stream()
                .map(itemModule -> itemModule.getSpeedMultiplierModifier(itemStack))
                .reduce(speedModifier, (a, b) -> a * b);

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

            return 0.5 + Math.abs(counterWeightLevel + integrityCost) * 0.2;
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
        return Collections.emptySet();
    }

    @Override
    public int getHarvestLevel(ItemStack itemStack, String toolClass, @Nullable PlayerEntity player, @Nullable IBlockState blockState) {
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

    @Override
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(final ItemStack stack, final ItemStack book) {
        return false;
    }

    @Override
    public ItemStack onCraftConsumeCapability(ItemStack providerStack, ItemStack targetStack, PlayerEntity player,
            Capability capability, int capabilityLevel, boolean consumeResources) {
        if (consumeResources) {
            applyDamage(capabilityLevel, providerStack, player);

            causeFierySelfEffect(player, providerStack, capabilityLevel * 2);
            causeEnderReverbEffect(player, providerStack, capabilityLevel * 2);

            tickProgression(player, providerStack, capabilityLevel * 2);
        }

        return super.onCraftConsumeCapability(providerStack, targetStack, player, capability, capabilityLevel, consumeResources);
    }

    @Override
    public ItemStack onActionConsumeCapability(ItemStack providerStack, ItemStack targetStack, PlayerEntity player,
            Capability capability, int capabilityLevel, boolean consumeResources) {
        if (consumeResources) {
            applyDamage(capabilityLevel, providerStack, player);

            causeFierySelfEffect(player, providerStack, capabilityLevel * 2);
            causeEnderReverbEffect(player, providerStack, capabilityLevel * 2);

            tickProgression(player, providerStack, capabilityLevel * 2);
        }

        return super.onCraftConsumeCapability(providerStack, targetStack, player, capability, capabilityLevel, consumeResources);
    }

    @Override
    public void assemble(ItemStack itemStack) {
        super.assemble(itemStack);

        CompoundNBT nbt = NBTHelper.getTag(itemStack);

        nbt.removeTag("ench");

        // this stops the tooltip renderer from showing enchantments
        nbt.setInteger("HideFlags", 1);

        if (getEffects(itemStack).contains(ItemEffect.silkTouch)) {
            Map<Enchantment, Integer> enchantments = new HashMap<>();
            enchantments.put(Enchantments.SILK_TOUCH, 1);
            EnchantmentHelper.setEnchantments(enchantments, itemStack);
        }
    }
}
