package se.mickelus.tetra.blocks.salvage;

import com.google.common.base.Predicates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.advancements.BlockInteractionCriterion;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.properties.PropertyHelper;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.mutil.util.RotationHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
@ParametersAreNonnullByDefault
public class BlockInteraction {
    public ToolAction requiredTool;
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

    // if this interaction should apply tool usage effects (honing, reverb, self fiery etc)
    protected boolean applyUsageEffects = true;

    public <V extends Comparable<V>> BlockInteraction(ToolAction requiredTool, int requiredLevel, Direction face,
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

    public BlockInteraction(ToolAction requiredTool, int requiredLevel, Direction face, float minX, float maxX, float minY,
            float maxY, Predicate<BlockState> predicate, InteractionOutcome outcome) {
        this(requiredTool, requiredLevel, face, minX, maxX, minY, maxY, outcome);

        this.predicate = predicate;
    }

    public <V extends Comparable<V>> BlockInteraction(ToolAction requiredTool, int requiredLevel, Direction face,
            float minX, float maxX, float minY, float maxY, Property<V> property, V propertyValue, InteractionOutcome outcome) {
        this(requiredTool, requiredLevel, face, minX, maxX, minY, maxY, new PropertyMatcher().where(property, Predicates.equalTo(propertyValue)),
                outcome);
    }

    public boolean applicableForBlock(Level world, BlockPos pos, BlockState blockState) {
        return predicate.test(blockState);
    }

    public boolean isWithinBounds(double x, double y) {
        return minX <= x && x <= maxX && minY <= y && y <= maxY;
    }

    public boolean isPotentialInteraction(Level world, BlockPos pos, BlockState blockState, Direction hitFace, Collection<ToolAction> availableTools) {
        return isPotentialInteraction(world, pos, blockState, Direction.NORTH, hitFace, availableTools);
    }

    public boolean isPotentialInteraction(Level world, BlockPos pos, BlockState blockState, Direction blockFacing, Direction hitFace,
            Collection<ToolAction> availableTools) {
        return applicableForBlock(world, pos, blockState)
                && RotationHelper.rotationFromFacing(blockFacing).rotate(face).equals(hitFace)
                && (alwaysReveal || availableTools.contains(requiredTool));
    }

    public void applyOutcome(Level world, BlockPos pos, BlockState blockState, @Nullable Player player, @Nullable InteractionHand hand, Direction hitFace) {
        outcome.apply(world, pos, blockState, player, hand, hitFace);
    }

    public static InteractionResult attemptInteraction(Level world, BlockState blockState, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult rayTrace) {
        ItemStack heldStack = player.getItemInHand(hand);
        Collection<ToolAction> availableTools = PropertyHelper.getItemTools(heldStack);

        AABB boundingBox = blockState.getShape(world, pos, CollisionContext.of(player)).bounds();
        double hitU = 16 * getHitU(rayTrace.getDirection(), boundingBox,
                rayTrace.getLocation().x - pos.getX(),
                rayTrace.getLocation().y - pos.getY(),
                rayTrace.getLocation().z - pos.getZ());
        double hitV = 16 * getHitV(rayTrace.getDirection(), boundingBox,
                rayTrace.getLocation().x - pos.getX(),
                rayTrace.getLocation().y - pos.getY(),
                rayTrace.getLocation().z - pos.getZ());

        BlockInteraction possibleInteraction = CastOptional.cast(blockState.getBlock(), IInteractiveBlock.class)
                .map(block -> block.getPotentialInteractions(world, pos, blockState, rayTrace.getDirection(), availableTools))
                .map(Arrays::stream).orElseGet(Stream::empty)
                .filter(interaction -> interaction.isWithinBounds(hitU, hitV))
                .filter(interaction -> PropertyHelper.getItemToolLevel(heldStack, interaction.requiredTool) >= interaction.requiredLevel)
                .findFirst()
                .orElse(null);

        if (possibleInteraction != null) {
            if (InteractionHand.MAIN_HAND == hand) {
                if (player.getAttackStrengthScale(0) < 0.8) {
                    if (player.getOffhandItem().isEmpty()) {
                        player.resetAttackStrengthTicker();
                        return InteractionResult.FAIL;
                    }
                    return InteractionResult.PASS;
                }
            } else {
                if (player.getCooldowns().isOnCooldown(heldStack.getItem())) {
                    return InteractionResult.FAIL;
                }
            }

            possibleInteraction.applyOutcome(world, pos, blockState, player, hand, rayTrace.getDirection());

            if (availableTools.contains(possibleInteraction.requiredTool) && heldStack.isDamageableItem()) {
                if (heldStack.getItem() instanceof IModularItem) {
                    IModularItem item = (IModularItem) heldStack.getItem();

                    item.applyDamage(2, heldStack, player);
                    if (possibleInteraction.applyUsageEffects) {
                        item.applyUsageEffects(player, heldStack, possibleInteraction.requiredLevel * 2);
                    }
                } else {
                    heldStack.hurtAndBreak(2, player, breaker -> breaker.broadcastBreakEvent(breaker.getUsedItemHand()));
                }
            }

            if (player instanceof ServerPlayer) {
                BlockState newState = world.getBlockState(pos);

                BlockInteractionCriterion.trigger((ServerPlayer) player, newState, possibleInteraction.requiredTool,
                        possibleInteraction.requiredLevel);
            }

            if (InteractionHand.MAIN_HAND == hand) {
                player.resetAttackStrengthTicker();
            } else {
                int cooldown = CastOptional.cast(heldStack.getItem(), ItemModularHandheld.class)
                        .map(item -> (int) (20 * item.getCooldownBase(heldStack)))
                        .orElse(10);
                player.getCooldowns().addCooldown(heldStack.getItem(), cooldown);
            }

            if (player.level.isClientSide) {
                InteractiveBlockOverlay.markDirty();
            }

            return InteractionResult.sidedSuccess(player.level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static BlockInteraction getInteractionAtPoint(Player player, BlockState blockState, BlockPos pos, Direction hitFace,
            double hitX, double hitY, double hitZ) {
        AABB boundingBox = blockState.getShape(player.level, pos).bounds();
        double hitU = getHitU(hitFace, boundingBox, hitX, hitY, hitZ);
        double hitV = getHitV(hitFace, boundingBox, hitX, hitY, hitZ);

        return CastOptional.cast(blockState.getBlock(), IInteractiveBlock.class)
                .map(block -> block.getPotentialInteractions(player.level, pos, blockState, hitFace, PropertyHelper.getPlayerTools(player)))
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
    private static double getHitU(Direction facing, AABB boundingBox, double hitX, double hitY, double hitZ) {
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
    private static double getHitV(Direction facing, AABB boundingBox, double hitX, double hitY, double hitZ) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockInteraction that = (BlockInteraction) o;
        return requiredLevel == that.requiredLevel &&
                Float.compare(that.minX, minX) == 0 &&
                Float.compare(that.minY, minY) == 0 &&
                Float.compare(that.maxX, maxX) == 0 &&
                Float.compare(that.maxY, maxY) == 0 &&
                Objects.equals(requiredTool, that.requiredTool) &&
                face == that.face;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requiredTool, requiredLevel, face, minX, minY, maxX, maxY);
    }

    public static List<ItemStack> getLoot(ResourceLocation lootTable, Player player, InteractionHand hand, ServerLevel world,
            BlockState blockState) {
        LootTable table = world.getServer().getLootTables().get(lootTable);

        LootContext context = new LootContext.Builder(world)
                .withLuck(player.getLuck())
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.BLOCK_STATE, blockState)
                .withParameter(LootContextParams.TOOL, player.getItemInHand(hand))
                .withParameter(LootContextParams.ORIGIN, player.position())
                .create(LootContextParamSets.BLOCK);

        return table.getRandomItems(context);
    }

    public static List<ItemStack> getLoot(ResourceLocation lootTable, ServerLevel world, BlockPos pos, BlockState blockState) {
        LootTable table = world.getServer().getLootTables().get(lootTable);

        LootContext context = new LootContext.Builder(world)
                .withParameter(LootContextParams.BLOCK_STATE, blockState)
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .create(LootContextParamSets.BLOCK);

        return table.getRandomItems(context);
    }

    public static void dropLoot(ResourceLocation lootTable, @Nullable Player player, @Nullable InteractionHand hand, ServerLevel world, BlockState blockState) {
        getLoot(lootTable, player, hand, world, blockState).forEach(itemStack -> {
            if (!player.getInventory().add(itemStack)) {
                player.drop(itemStack, false);
            }
        });
    }
    public static void dropLoot(ResourceLocation lootTable, ServerLevel world, BlockPos pos, BlockState blockState) {
        getLoot(lootTable, world, pos, blockState).forEach(itemStack -> {
            Block.popResource(world, pos, itemStack);
        });
    }
}
