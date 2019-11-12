package se.mickelus.tetra.items;

import com.google.common.collect.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockMatcher;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.eventbus.api.Event;
import se.mickelus.tetra.BleedingEffect;
import se.mickelus.tetra.EarthboundEffect;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.blocks.workbench.WorkbenchBlock;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.module.ItemEffectHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemModularHandheld extends ItemModular {

    private static final Set<Block> axeBlocks = Sets.newHashSet(Blocks.OAK_PLANKS, Blocks.SPRUCE_PLANKS,
            Blocks.BIRCH_PLANKS, Blocks.JUNGLE_PLANKS, Blocks.ACACIA_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.OAK_LOG,
            Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG,
            Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_JUNGLE_LOG,
            Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.BOOKSHELF, Blocks.CHEST, Blocks.PUMPKIN,
            Blocks.CARVED_PUMPKIN, Blocks.MELON, Blocks.LADDER, Blocks.OAK_BUTTON, Blocks.SPRUCE_BUTTON,
            Blocks.BIRCH_BUTTON, Blocks.JUNGLE_BUTTON, Blocks.ACACIA_BUTTON, Blocks.DARK_OAK_BUTTON,
            Blocks.OAK_PRESSURE_PLATE, Blocks.SPRUCE_PRESSURE_PLATE, Blocks.BIRCH_PRESSURE_PLATE,
            Blocks.JUNGLE_PRESSURE_PLATE, Blocks.ACACIA_PRESSURE_PLATE, Blocks.DARK_OAK_PRESSURE_PLATE);

    private static final Set<Material> axeMaterials = Sets.newHashSet(Material.WOOD);

    private static final Set<Block> pickaxeBlocks = Sets.newHashSet(Blocks.COAL_ORE, Blocks.COBBLESTONE,
            Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.ICE, Blocks.IRON_BLOCK,
            Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE, Blocks.MOSSY_COBBLESTONE, Blocks.NETHERRACK,
            Blocks.PACKED_ICE, Blocks.REDSTONE_ORE, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.STONE,
            Blocks.STONE_SLAB, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE, Blocks.RAIL, Blocks.ACTIVATOR_RAIL,
            Blocks.DETECTOR_RAIL, Blocks.POWERED_RAIL);

    private static final Set<Material> pickaxeMaterials = Sets.newHashSet(Material.IRON, Material.ANVIL, Material.ROCK);

    private static final Set<Block> shovelBlocks = Sets.newHashSet(Blocks.CLAY, Blocks.DIRT, Blocks.FARMLAND,
            Blocks.GRASS, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.SNOW, Blocks.SNOW_BLOCK, Blocks.SOUL_SAND,
            Blocks.GRASS_PATH, Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER,
            Blocks.MAGENTA_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER,
            Blocks.LIME_CONCRETE_POWDER, Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER,
            Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER,
            Blocks.BLUE_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER,
            Blocks.RED_CONCRETE_POWDER, Blocks.BLACK_CONCRETE_POWDER);

    private static final Set<Material> cuttingMaterials = Sets.newHashSet(Material.PLANTS, Material.TALL_PLANTS,
            Material.CORAL, Material.LEAVES, Material.GOURD, Material.WEB, Material.WOOL, Material.CARPET);

    // todo 1.14: test that this still works, possible cleanup. Possibly missing planks, stairs, slabs, doors
    private static final String[] denailTags = new String[] { "fence_gates/wooden", "fences/wooden", "chests/wooden"};
    private static final List<Predicate<BlockState>> denailBlocks = ImmutableList.of(
            BlockMatcher.forBlock(WorkbenchBlock.instance),
            BlockMatcher.forBlock(Blocks.CRAFTING_TABLE), BlockMatcher.forBlock(Blocks.BOOKSHELF),
            BlockMatcher.forBlock(Blocks.TRAPPED_CHEST), BlockMatcher.forBlock(Blocks.RAIL),
            BlockMatcher.forBlock(Blocks.ACTIVATOR_RAIL), BlockMatcher.forBlock(Blocks.DETECTOR_RAIL),
            BlockMatcher.forBlock(Blocks.POWERED_RAIL), BlockMatcher.forBlock(Blocks.OAK_SIGN),
            BlockMatcher.forBlock(Blocks.SPRUCE_SIGN), BlockMatcher.forBlock(Blocks.BIRCH_SIGN),
            BlockMatcher.forBlock(Blocks.ACACIA_SIGN), BlockMatcher.forBlock(Blocks.JUNGLE_SIGN),
            BlockMatcher.forBlock(Blocks.DARK_OAK_SIGN), BlockMatcher.forBlock(Blocks.OAK_WALL_SIGN),
            BlockMatcher.forBlock(Blocks.SPRUCE_WALL_SIGN), BlockMatcher.forBlock(Blocks.BIRCH_WALL_SIGN),
            BlockMatcher.forBlock(Blocks.ACACIA_WALL_SIGN), BlockMatcher.forBlock(Blocks.JUNGLE_WALL_SIGN),
            BlockMatcher.forBlock(Blocks.DARK_OAK_WALL_SIGN), BlockMatcher.forBlock(Blocks.WHITE_BED),
            BlockMatcher.forBlock(Blocks.ORANGE_BED), BlockMatcher.forBlock(Blocks.MAGENTA_BED),
            BlockMatcher.forBlock(Blocks.LIGHT_BLUE_BED), BlockMatcher.forBlock(Blocks.YELLOW_BED),
            BlockMatcher.forBlock(Blocks.LIME_BED), BlockMatcher.forBlock(Blocks.PINK_BED),
            BlockMatcher.forBlock(Blocks.GRAY_BED), BlockMatcher.forBlock(Blocks.LIGHT_GRAY_BED),
            BlockMatcher.forBlock(Blocks.CYAN_BED), BlockMatcher.forBlock(Blocks.PURPLE_BED),
            BlockMatcher.forBlock(Blocks.BLUE_BED), BlockMatcher.forBlock(Blocks.BROWN_BED),
            BlockMatcher.forBlock(Blocks.GREEN_BED), BlockMatcher.forBlock(Blocks.RED_BED),
            BlockMatcher.forBlock(Blocks.BLACK_BED), BlockMatcher.forBlock(Blocks.LADDER),
            BlockMatcher.forBlock(Blocks.JUKEBOX), BlockMatcher.forBlock(Blocks.NOTE_BLOCK),
            BlockMatcher.forBlock(Blocks.OAK_TRAPDOOR), BlockMatcher.forBlock(Blocks.SPRUCE_TRAPDOOR),
            BlockMatcher.forBlock(Blocks.BIRCH_TRAPDOOR), BlockMatcher.forBlock(Blocks.JUNGLE_TRAPDOOR),
            BlockMatcher.forBlock(Blocks.ACACIA_TRAPDOOR), BlockMatcher.forBlock(Blocks.DARK_OAK_TRAPDOOR),
            BlockMatcher.forBlock(Blocks.OAK_BUTTON), BlockMatcher.forBlock(Blocks.SPRUCE_BUTTON),
            BlockMatcher.forBlock(Blocks.BIRCH_BUTTON), BlockMatcher.forBlock(Blocks.JUNGLE_BUTTON),
            BlockMatcher.forBlock(Blocks.ACACIA_BUTTON), BlockMatcher.forBlock(Blocks.DARK_OAK_BUTTON),
            BlockMatcher.forBlock(Blocks.OAK_PRESSURE_PLATE), BlockMatcher.forBlock(Blocks.SPRUCE_PRESSURE_PLATE),
            BlockMatcher.forBlock(Blocks.BIRCH_PRESSURE_PLATE), BlockMatcher.forBlock(Blocks.JUNGLE_PRESSURE_PLATE),
            BlockMatcher.forBlock(Blocks.ACACIA_PRESSURE_PLATE), BlockMatcher.forBlock(Blocks.DARK_OAK_PRESSURE_PLATE),
            BlockMatcher.forBlock(Blocks.DAYLIGHT_DETECTOR)
            );

    protected static final Map<Block, BlockState> tillLookup = Maps.newHashMap(ImmutableMap.of(
            Blocks.GRASS_BLOCK, Blocks.FARMLAND.getDefaultState(),
            Blocks.GRASS_PATH, Blocks.FARMLAND.getDefaultState(),
            Blocks.DIRT, Blocks.FARMLAND.getDefaultState(),
            Blocks.COARSE_DIRT, Blocks.DIRT.getDefaultState()));

    protected static final Map<Block, BlockState> flattenLookup = Maps.newHashMap(ImmutableMap.of(
            Blocks.GRASS_BLOCK, Blocks.GRASS_PATH.getDefaultState()));


    protected static final UUID ARMOR_MODIFIER = UUID.fromString("D96050BE-6A94-4A27-AA0B-2AF705327BA4");

    // the base amount of damage the item should take after destroying a block
    protected int blockDestroyDamage = 1;

    // the base amount of damage the item should take after hitting an entity
    protected int entityHitDamage = 1;

    public ItemModularHandheld(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack itemStack, World world, BlockState state, BlockPos pos,
            LivingEntity entity) {
        if (state.getBlockHardness(world, pos) > 0) {
            applyDamage(blockDestroyDamage, itemStack, entity);

            if (!isBroken(itemStack)) {
                tickProgression(entity, itemStack, 1);
            }
        }

        if (!world.isRemote) {
            int intuitLevel = getEffectLevel(itemStack, ItemEffect.intuit);
            if (intuitLevel > 0) {
                int xp = state.getExpDrop(world, pos, getEffectLevel(itemStack, ItemEffect.fortune),
                        getEffectLevel(itemStack, ItemEffect.silkTouch));
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
                if (!CreatureAttribute.UNDEAD.equals(target.getCreatureAttribute())
                        && attacker.getRNG().nextFloat() < 0.3f) {
                    target.addPotionEffect(new EffectInstance(BleedingEffect.instance, 40, bleedingLevel));
                }
            }

            int arthropodLevel = getEffectLevel(itemStack, ItemEffect.arthropod);
            if (arthropodLevel > 0 && CreatureAttribute.ARTHROPOD.equals(target.getCreatureAttribute())) {
                int ticks = 20 + attacker.getRNG().nextInt(10 * arthropodLevel);
                target.addPotionEffect(new EffectInstance(Effects.SLOWNESS, ticks, 3));
            }

            // todo: only trigger if target is standing on stone/earth/sand/gravel
            int earthbindLevel = getEffectLevel(itemStack, ItemEffect.earthbind);
            if (earthbindLevel > 0 && attacker.getRNG().nextFloat() < Math.max(0.1, 0.5 * ( 1 - target.posY  / 128 ))) {
                target.addPotionEffect(new EffectInstance(EarthboundEffect.instance, 80, 0, false, true));

                // todo 1.14: check that this still renders
                if (target.world instanceof ServerWorld) {
                    BlockState blockState = target.world.getBlockState(new BlockPos(target.posX, target.posY - 1, target.posZ));
                    ((ServerWorld)target.world).spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, blockState),
                            target.posX, target.posY + 0.1, target.posZ,
                            16, 0, target.world.rand.nextGaussian() * 0.2, 0, 0.1);
                }
            }

            causeFierySelfEffect(attacker, itemStack, 1.4);
            causeEnderReverbEffect(attacker, itemStack, 1.5);

            tickProgression(attacker, itemStack, 1);
        }

        return true;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Direction facing = context.getFace();

        ItemStack itemStack = player.getHeldItem(hand);
        int flatteningLevel = getEffectLevel(itemStack, ItemEffect.flattening);
        int tillingLevel = getEffectLevel(itemStack, ItemEffect.tilling);

        causeFierySelfEffect(player, itemStack, 2);
        causeEnderReverbEffect(player, itemStack, 1.7);

        if (flatteningLevel > 0 && (tillingLevel > 0 && player.isSneaking() || tillingLevel == 0)) {
            return flattenPath(player, world, pos, hand, facing);
        } else if (tillingLevel > 0) {
            return tillBlock(context);
        }


        int denailingLevel = getEffectLevel(itemStack, ItemEffect.denailing);
        if (denailingLevel > 0 && player.getCooledAttackStrength(0) > 0.9) {
            ActionResultType result = denailBlock(player, world, pos, hand, facing);

            if (result.equals(ActionResultType.SUCCESS)) {
                player.resetCooldown();
            }

            return result;
        }

        return super.onItemUse(context);
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
                            target -> target instanceof EndermanEntity || target instanceof EndermiteEntity
                                    || target instanceof ShulkerEntity || target instanceof EnderDragonEntity);
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
     * @return ActionResultType.SUCCESS if successful, ActionResultType.FAIL if block cannot be edited by player,
     * otherwise ActionResultType.PASS
     */
    public ActionResultType flattenPath(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing) {
        ItemStack itemStack = player.getHeldItem(hand);

        if (!player.canPlayerEdit(pos.offset(facing), facing, itemStack)) {
            return ActionResultType.FAIL;
        }

        if (facing != Direction.DOWN && world.getBlockState(pos.up()).getMaterial() == Material.AIR
                && world.getBlockState(pos).getBlock() == Blocks.GRASS) {
            world.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1, 1);

            if (!world.isRemote) {
                world.setBlockState(pos, Blocks.GRASS_PATH.getDefaultState(), 11);
                applyDamage(blockDestroyDamage, itemStack, player);
                tickProgression(player, itemStack, blockDestroyDamage);
            }

            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }

    /**
     * Tills dirt or grass, turning it into farmland. Tilling coarse dirt turns it into dirt.
     * @param context the context of the item's usage
     * otherwise ActionResultType.PASS
     */
    public ActionResultType tillBlock(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Direction facing = context.getFace();

        ItemStack itemStack = player.getHeldItem(hand);

        if (!player.canPlayerEdit(pos.offset(facing), facing, itemStack)) {
            return ActionResultType.FAIL;
        }

        // fire the forge event manually as the helper damages durability
        UseHoeEvent event = new UseHoeEvent(context);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            return ActionResultType.FAIL;
        }
        if (event.getResult() == Event.Result.ALLOW) {
            applyDamage(blockDestroyDamage, itemStack, player);
            tickProgression(player, itemStack, blockDestroyDamage);
            return ActionResultType.SUCCESS;
        }

        if (facing != Direction.DOWN && world.isAirBlock(pos.up())) {
            BlockState newState = tillLookup.get(world.getBlockState(pos).getBlock());

            if (newState != null) {
                world.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1, 1);

                if (!world.isRemote) {
                    world.setBlockState(pos, newState, 11);
                    applyDamage(blockDestroyDamage, itemStack, player);
                }

                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.PASS;
    }

    /**
     * Instantly break plank based blocks.
     * @param player the responsible player entity
     * @param world the world in which the action takes place
     * @param pos the position in the world
     * @param hand the hand holding the tool
     * @param facing the clicked face
     * @return ActionResultType.SUCCESS if successful, ActionResultType.FAIL if block cannot be edited by player,
     * otherwise ActionResultType.PASS
     */
    public ActionResultType denailBlock(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing) {
        ItemStack itemStack = player.getHeldItem(hand);

        if (!player.canPlayerEdit(pos.offset(facing), facing, itemStack)) {
            return ActionResultType.FAIL;
        }

        BlockState blockState = world.getBlockState(pos);
        if (canDenail(blockState, world, pos)) {
            boolean success = ItemEffectHandler.breakBlock(world, player, player.getHeldItem(hand), pos, blockState);
            if (success) {
                applyDamage(blockDestroyDamage, itemStack, player);
                tickProgression(player, itemStack, blockDestroyDamage);

                world.playEvent(player, 2001, pos, Block.getStateId(blockState));
                player.resetCooldown();
                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.PASS;
    }

    private boolean canDenail(BlockState blockState, World world, BlockPos pos) {
        boolean matchOre = Optional.of(blockState.getBlock())
                .map(Block::getTags)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(ResourceLocation::toString)
                .anyMatch(tag -> Arrays.asList(denailTags).contains(tag));

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
                    target.getBoundingBox().grow(range, 0.25d, range)).stream()
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
        if (world instanceof ServerWorld) {
            ((ServerWorld)world).spawnParticle(ParticleTypes.SWEEP_ATTACK, x, y, z,
                    1, xOffset, 0, zOffset, 0);
        }
    }

    public static void spawnSweepParticles(LivingEntity attacker) {
        double xOffset = -MathHelper.sin(attacker.rotationYaw * 0.017453292F);
        double zOffset = MathHelper.cos(attacker.rotationYaw * 0.017453292F);

        spawnSweepParticles(attacker.world, attacker.posX + xOffset, attacker.posY + attacker.getHeight() * 0.5D,
                attacker.posZ + zOffset, xOffset, zOffset);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        setCooledAttackStrength(stack, player.getCooledAttackStrength(0.5f));
        return false;
    }

    public void setCooledAttackStrength(ItemStack itemStack, float strength) {
        NBTHelper.getTag(itemStack).putFloat(cooledStrengthKey, strength);
    }

    public static float getCooledAttackStrength(ItemStack itemStack) {
        return NBTHelper.getTag(itemStack).getFloat(cooledStrengthKey);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack itemStack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, itemStack);

        if (slot == EquipmentSlotType.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER,
                    "Weapon modifier", getDamageModifier(itemStack), AttributeModifier.Operation.ADDITION));

            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER,
                    "Weapon modifier", getSpeedModifier(itemStack), AttributeModifier.Operation.ADDITION));
        }

        if (slot == EquipmentSlotType.MAINHAND || slot == EquipmentSlotType.OFFHAND) {
            int armor = getEffectLevel(itemStack, ItemEffect.armor);
            if  (armor > 0) {
                multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MODIFIER,
                        "Weapon modifier", armor, AttributeModifier.Operation.ADDITION));
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
    public Set<ToolType> getToolTypes(ItemStack stack) {
        if (!isBroken(stack)) {
            return getCapabilities(stack).stream()
                    .map(Enum::toString)
                    .map(ToolType::get)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    @Override
    public int getHarvestLevel(ItemStack stack, ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState) {
        if (!isBroken(stack)) {
            int capabilityLevel = getCapabilityLevel(stack, tool);
            if (capabilityLevel > 0) {
                return capabilityLevel - 1;
            }
        }
        return -1;
    }

    @Override
    public boolean canHarvestBlock(ItemStack stack, BlockState state) {
        if (pickaxeMaterials.contains(state.getMaterial())) {
            return getHarvestLevel(stack, ToolType.PICKAXE, null, null) >= 0;
        } else if (state.getBlock().equals(Blocks.COBWEB)) {
            return getHarvestLevel(stack, ToolTypes.CUT, null, null) >= 0;
        }
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
        if (!isBroken(itemStack)) {
            ToolType tool = getEffectiveTool(blockState);

            float speed = (float) (4 + getSpeedModifier(itemStack)) * getCapabilityEfficiency(itemStack, tool);

            // todo: need a better way to handle this
            if (ToolTypes.CUT.equals(tool) && blockState.getBlock().equals(Blocks.COBWEB)) {
                speed *= 10;
            }

            if (speed < 1) {
                return 1;
            }
            return speed;
        }
        return 1;
    }

    public static ToolType getEffectiveTool(BlockState blockState) {
        ToolType tool = blockState.getBlock().getHarvestTool(blockState);

        if (tool != null) {
            return tool;
        }

        if (axeMaterials.contains(blockState.getMaterial())) {
            return ToolType.AXE;
        } else if (pickaxeMaterials.contains(blockState.getMaterial())) {
            return ToolType.PICKAXE;
        } else if (axeBlocks.contains(blockState.getBlock())) {
            return ToolType.AXE;
        } else if (pickaxeBlocks.contains(blockState.getBlock())) {
            return ToolType.PICKAXE;
        } else if (cuttingMaterials.contains(blockState.getMaterial())) {
            return ToolTypes.CUT;
        } else if (shovelBlocks.contains(blockState.getBlock())) {
            return ToolType.SHOVEL;
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

        nbt.remove("ench");

        // this stops the tooltip renderer from showing enchantments
        nbt.putInt("HideFlags", 1);

        if (getEffects(itemStack).contains(ItemEffect.silkTouch)) {
            Map<Enchantment, Integer> enchantments = new HashMap<>();
            enchantments.put(Enchantments.SILK_TOUCH, 1);
            EnchantmentHelper.setEnchantments(enchantments, itemStack);
        }
    }
}
