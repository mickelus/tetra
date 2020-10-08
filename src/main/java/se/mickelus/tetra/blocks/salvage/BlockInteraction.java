package se.mickelus.tetra.blocks.salvage;

import com.google.common.base.Predicates;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.state.Property;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.util.RotationHelper;
import se.mickelus.tetra.advancements.BlockInteractionCriterion;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.properties.PropertyHelper;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.util.CastOptional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BlockInteraction {
    public ToolType requiredTool;
    public int requiredLevel;

    // if false the player needs to have an item that provides the required tool in their inventory for the interaction to be visible
    public boolean alwaysReveal = true;

    public Direction face;
    public float minX;
    public float minY;
    public float maxX;
    public float maxY;

    public Predicate<BlockState> predicate;

    public InteractionOutcome outcome;

    public <V extends Comparable<V>> BlockInteraction(ToolType requiredTool, int requiredLevel, Direction face,
            float minX, float maxX, float minY, float maxY, InteractionOutcome outcome) {

        this.requiredTool = requiredTool;
        this.requiredLevel = requiredLevel;
        this.face = face;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;

        this.outcome = outcome;
    }

    public BlockInteraction(ToolType requiredTool, int requiredLevel, Direction face, float minX, float maxX, float minY,
            float maxY, Predicate<BlockState> predicate, InteractionOutcome outcome) {
        this(requiredTool, requiredLevel, face, minX, maxX, minY, maxY, outcome);

        this.predicate = predicate;
    }

    public <V extends Comparable<V>> BlockInteraction(ToolType requiredTool, int requiredLevel, Direction face,
            float minX, float maxX, float minY, float maxY, Property<V> property, V propertyValue, InteractionOutcome outcome) {
        this(requiredTool, requiredLevel, face, minX, maxX, minY, maxY, new PropertyMatcher().where(property, Predicates.equalTo(propertyValue)),
                outcome);
    }

    public boolean applicableForBlock(World world, BlockPos pos, BlockState blockState) {
        return predicate.test(blockState);
    }

    public boolean isWithinBounds(double x, double y) {
        return minX <= x && x <= maxX && minY <= y && y <= maxY;
    }

    public boolean isPotentialInteraction(World world, BlockPos pos, BlockState blockState, Direction hitFace, Collection<ToolType> availableTools) {
        return isPotentialInteraction(world, pos, blockState, Direction.NORTH, hitFace, availableTools);
    }

    public boolean isPotentialInteraction(World world, BlockPos pos, BlockState blockState, Direction blockFacing, Direction hitFace,
            Collection<ToolType> availableTools) {
        return applicableForBlock(world, pos, blockState)
                && RotationHelper.rotationFromFacing(blockFacing).rotate(face).equals(hitFace)
                && (alwaysReveal || availableTools.contains(requiredTool));
    }

    public void applyOutcome(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, Direction hitFace) {
        outcome.apply(world, pos, blockState, player, hand, hitFace);
    }

    public static ActionResultType attemptInteraction(World world, BlockState blockState, BlockPos pos, PlayerEntity player, Hand hand,
            BlockRayTraceResult rayTrace) {
        ItemStack heldStack = player.getHeldItem(hand);
        Collection<ToolType> availableTools = PropertyHelper.getItemTools(heldStack);

        if (Hand.MAIN_HAND == hand) {
            if (player.getCooledAttackStrength(0) < 0.8) {
                if (player.getHeldItemOffhand().isEmpty()) {
                    player.resetCooldown();
                    return ActionResultType.FAIL;
                }
                return ActionResultType.PASS;
            }
        } else {
            if (player.getCooldownTracker().hasCooldown(heldStack.getItem())) {
                return ActionResultType.FAIL;
            }
        }

        AxisAlignedBB boundingBox = blockState.getShape(world, pos).getBoundingBox();
        double hitU = 16 * getHitU(rayTrace.getFace(), boundingBox,
                rayTrace.getHitVec().x - pos.getX(),
                rayTrace.getHitVec().y - pos.getY(),
                rayTrace.getHitVec().z - pos.getZ());
        double hitV = 16 * getHitV(rayTrace.getFace(), boundingBox,
                rayTrace.getHitVec().x - pos.getX(),
                rayTrace.getHitVec().y - pos.getY(),
                rayTrace.getHitVec().z - pos.getZ());

        BlockInteraction possibleInteraction = CastOptional.cast(blockState.getBlock(), IInteractiveBlock.class)
                .map(block -> block.getPotentialInteractions(world, pos, blockState, rayTrace.getFace(), availableTools))
                .map(Arrays::stream).orElseGet(Stream::empty)
                .filter(interaction -> interaction.isWithinBounds(hitU, hitV))
                .filter(interaction -> PropertyHelper.getItemToolLevel(heldStack, interaction.requiredTool) >= interaction.requiredLevel)
                .findFirst()
                .orElse(null);

        if (possibleInteraction != null) {
            possibleInteraction.applyOutcome(world, pos, blockState, player, hand, rayTrace.getFace());

            if (availableTools.contains(possibleInteraction.requiredTool) && heldStack.isDamageable()) {
                if (heldStack.getItem() instanceof ModularItem) {
                    ((ModularItem) heldStack.getItem()).applyDamage(2, heldStack, player);
                } else {
                    heldStack.damageItem(2, player, breaker -> breaker.sendBreakAnimation(breaker.getActiveHand()));
                }
            }

            if (player instanceof ServerPlayerEntity) {
                BlockState newState = world.getBlockState(pos);

                BlockInteractionCriterion.trigger((ServerPlayerEntity) player, newState, possibleInteraction.requiredTool,
                        possibleInteraction.requiredLevel);
            }

            if (Hand.MAIN_HAND == hand) {
                player.resetCooldown();
            } else {
                int cooldown = CastOptional.cast(heldStack.getItem(), ItemModularHandheld.class)
                        .map(item -> (int) (20 * item.getCooldownBase(heldStack)))
                        .orElse(10);
                player.getCooldownTracker().setCooldown(heldStack.getItem(), cooldown);
            }

            if (player.world.isRemote) {
                InteractiveBlockOverlay.markDirty();
            }

            return ActionResultType.func_233537_a_(player.world.isRemote);
        }
        return ActionResultType.PASS;
    }

    public static BlockInteraction getInteractionAtPoint(PlayerEntity player, BlockState blockState, BlockPos pos, Direction hitFace,
            double hitX, double hitY, double hitZ) {
        AxisAlignedBB boundingBox = blockState.getShape(player.world, pos).getBoundingBox();
        double hitU = getHitU(hitFace, boundingBox, hitX, hitY, hitZ);
        double hitV = getHitV(hitFace, boundingBox, hitX, hitY, hitZ);

        return CastOptional.cast(blockState.getBlock(), IInteractiveBlock.class)
                .map(block -> block.getPotentialInteractions(player.world, pos, blockState, hitFace, PropertyHelper.getPlayerTools(player)))
                .map(Arrays::stream).orElseGet(Stream::empty)
                .filter(interaction -> interaction.isWithinBounds(hitU * 16, hitV * 16))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the horizontal coordinate for where the face was clicked, between 0-1.
     * @param facing
     * @param boundingBox
     * @param hitX
     * @param hitY
     * @param hitZ
     * @return
     */
    private static double getHitU(Direction facing, AxisAlignedBB boundingBox, double hitX, double hitY, double hitZ) {
        switch (facing) {
            case DOWN:
                return boundingBox.maxX - hitX;
            case UP:
                return boundingBox.maxX - hitX;
            case NORTH:
                return boundingBox.maxX - hitX;
            case SOUTH:
                return hitX - boundingBox.minX;
            case WEST:
                return hitZ - boundingBox.minZ;
            case EAST:
                return boundingBox.maxZ - hitZ;
        }
        return 0;
    }

    /**
     * Returns the vertical coordinate for where the face was clicked, between 0-1.
     * @param facing
     * @param boundingBox
     * @param hitX
     * @param hitY
     * @param hitZ
     * @return
     */
    private static double getHitV(Direction facing, AxisAlignedBB boundingBox, double hitX, double hitY, double hitZ) {
        switch (facing) {
            case DOWN:
                return boundingBox.maxZ - hitZ;
            case UP:
                return boundingBox.maxZ - hitZ;
            case NORTH:
                return boundingBox.maxY - hitY;
            case SOUTH:
                return boundingBox.maxY - hitY;
            case WEST:
                return boundingBox.maxY - hitY;
            case EAST:
                return boundingBox.maxY - hitY;
        }
        return 0;
    }

    public static List<ItemStack> getLoot(ResourceLocation lootTable, PlayerEntity player, Hand hand, ServerWorld world,
            BlockState blockState) {
        LootTable table = world.getServer().getLootTableManager().getLootTableFromLocation(lootTable);

        LootContext context = new LootContext.Builder(world)
                .withLuck(player.getLuck())
                .withParameter(LootParameters.THIS_ENTITY, player)
                .withParameter(LootParameters.BLOCK_STATE, blockState)
                .withParameter(LootParameters.TOOL, player.getHeldItem(hand))
                .withParameter(LootParameters.THIS_ENTITY, player)
                .withParameter(LootParameters.field_237457_g_, player.getPositionVec())
                .build(LootParameterSets.BLOCK);

        return table.generate(context);
    }

    public static void dropLoot(ResourceLocation lootTable, PlayerEntity player, Hand hand, ServerWorld world, BlockState blockState) {
        getLoot(lootTable, player, hand, world, blockState).forEach(itemStack -> {
            if (!player.inventory.addItemStackToInventory(itemStack)) {
                player.dropItem(itemStack, false);
            }
        });
    }
}
