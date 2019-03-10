package se.mickelus.tetra.blocks.salvage;

import com.google.common.base.Predicates;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.mickelus.tetra.RotationHelper;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BlockInteraction {
    public Capability requiredCapability;
    public int requiredLevel;

    public EnumFacing face;
    public float minX;
    public float minY;
    public float maxX;
    public float maxY;

    public Predicate<IBlockState> predicate;

    public InteractionOutcome outcome;

    public float successChance = 1;

    public <V extends Comparable<V>> BlockInteraction(Capability requiredCapability, int requiredLevel, EnumFacing face, float minX, float maxX, float minY,
            float maxY, IProperty<V> property, V propertyValue, InteractionOutcome outcome) {

        this.requiredCapability = requiredCapability;
        this.requiredLevel = requiredLevel;
        this.face = face;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.predicate = new PropertyMatcher().where(property, Predicates.equalTo(propertyValue));

        this.outcome = outcome;
    }

    public BlockInteraction(Capability requiredCapability, int requiredLevel, EnumFacing face, float minX, float maxX, float minY,
                            float maxY, Predicate<IBlockState> predicate, InteractionOutcome outcome) {

        this.requiredCapability = requiredCapability;
        this.requiredLevel = requiredLevel;
        this.face = face;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.predicate = predicate;

        this.outcome = outcome;
    }


    public boolean applicableForState(IBlockState blockState) {
        return predicate.test(blockState);
    }

    public boolean isWithinBounds(float x, float y) {
        return minX <= x && x <= maxX && minY <= y && y <= maxY;
    }

    public boolean isPotentialInteraction(IBlockState blockState, EnumFacing hitFace, Collection<Capability> availableCapabilities) {
        return isPotentialInteraction(blockState, EnumFacing.NORTH, hitFace, availableCapabilities);
    }

    public boolean isPotentialInteraction(IBlockState blockState, EnumFacing blockFacing, EnumFacing hitFace,
                                          Collection<Capability> availableCapabilities) {
        return applicableForState(blockState)
                && RotationHelper.rotationFromFacing(blockFacing).rotate(face).equals(hitFace)
                && availableCapabilities.contains(requiredCapability);
    }

    public void applyOutcome(World world, BlockPos pos, IBlockState blockState, EntityPlayer player, EnumHand hand, EnumFacing hitFace) {
        outcome.apply(world, pos, blockState, player, hand, hitFace);
    }

    public static boolean attemptInteraction(World world, IBlockState blockState, BlockPos pos, EntityPlayer player,
                                             EnumHand hand, EnumFacing hitFace, float hitX, float hitY, float hitZ) {
        ItemStack heldStack = player.getHeldItem(hand);
        Collection<Capability> availableCapabilities = CapabilityHelper.getItemCapabilities(heldStack);

        if (player.getCooledAttackStrength(0) < 0.8) {
            player.resetCooldown();
            return false;
        }

        float hitU = getHitU(hitFace, hitX, hitY, hitZ);
        float hitV = getHitV(hitFace, hitX, hitY, hitZ);

        BlockInteraction possibleInteraction = Optional.of(blockState.getBlock())
                .filter(block -> block instanceof IBlockCapabilityInteractive)
                .map(block -> (IBlockCapabilityInteractive) block)
                .map(block -> block.getPotentialInteractions(blockState, hitFace, availableCapabilities))
                .map(Arrays::stream).orElseGet(Stream::empty)
                .filter(interaction -> interaction.isWithinBounds(hitU * 16, hitV * 16))
                .filter(interaction -> CapabilityHelper.getItemCapabilityLevel(heldStack, interaction.requiredCapability) >= interaction.requiredLevel)
                .findFirst()
                .orElse(null);

        if (possibleInteraction != null) {
            possibleInteraction.applyOutcome(world, pos, blockState, player, hand, hitFace);

            if (availableCapabilities.contains(possibleInteraction.requiredCapability) && heldStack.isItemStackDamageable()) {
                heldStack.damageItem(2, player);
            }

            player.resetCooldown();
            return true;
        }
        return false;
    }

    public static BlockInteraction getInteractionAtPoint(EntityPlayer player, IBlockState blockState, EnumFacing hitFace, float hitX, float hitY,
            float hitZ) {
        float hitU = getHitU(hitFace, hitX, hitY, hitZ);
        float hitV = getHitV(hitFace, hitX, hitY, hitZ);

        return Optional.of(blockState.getBlock())
                .filter(block -> block instanceof IBlockCapabilityInteractive)
                .map(block -> (IBlockCapabilityInteractive) block)
                .map(block -> block.getPotentialInteractions(blockState, hitFace, CapabilityHelper.getPlayerCapabilities(player)))
                .map(Arrays::stream).orElseGet(Stream::empty)
                .filter(interaction -> interaction.isWithinBounds(hitU * 16, hitV * 16))
                .findFirst()
                .orElse(null);
    }

    private static float getHitU(EnumFacing facing, float hitX, float hitY, float hitZ) {
        switch (facing) {
            case DOWN:
                return hitX;
            case UP:
                return 1 - hitX;
            case NORTH:
                return 1 - hitX;
            case SOUTH:
                return hitX;
            case WEST:
                return hitZ;
            case EAST:
                return 1 - hitZ;
        }
        return 0;
    }

    private static float getHitV(EnumFacing facing, float hitX, float hitY, float hitZ) {
        switch (facing) {
            case DOWN:
                return 1 - hitZ;
            case UP:
                return 1 - hitZ;
            case NORTH:
                return 1 - hitY;
            case SOUTH:
                return 1 - hitY;
            case WEST:
                return 1 - hitY;
            case EAST:
                return 1 - hitY;
        }
        return 0;
    }
}
