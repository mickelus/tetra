package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class BlockInteraction {
    public Capability requiredCapability;
    public int requiredLevel;

    public EnumFacing face;
    public float minX;
    public float minY;
    public float maxX;
    public float maxY;

    public IProperty property;
    public Object propertyValue;

    public InteractionOutcome outcome;

    public BlockInteraction(Capability requiredCapability, int requiredLevel, EnumFacing face, float minX, float minY,
                            float maxX, float maxY, IProperty property, Object propertyValue, InteractionOutcome outcome) {

        this.requiredCapability = requiredCapability;
        this.requiredLevel = requiredLevel;
        this.face = face;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.property = property;
        this.propertyValue = propertyValue;

        this.outcome = outcome;
    }

    public boolean applicableForState(IBlockState blockState) {
        return propertyValue.equals(blockState.getValue(property));
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
                && face.equals(rotationFromFacing(blockFacing).rotate(hitFace))
                && availableCapabilities.contains(requiredCapability);
    }

    public void applyOutcome(World world, BlockPos pos, IBlockState blockState, EntityPlayer player, EnumFacing hitFace) {
        outcome.apply(world, pos, blockState, player, hitFace);
    }

    public static Rotation rotationFromFacing(EnumFacing facing) {
        switch (facing) {
            case UP:
            case DOWN:
            case NORTH:
                return Rotation.NONE;
            case SOUTH:
                return Rotation.CLOCKWISE_180;
            case EAST:
                return Rotation.CLOCKWISE_90;
            case WEST:
                return Rotation.COUNTERCLOCKWISE_90;
            default:
                return Rotation.NONE;
        }
    }

    public static boolean attemptInteraction(World world, IBlockState blockState, BlockPos pos, EntityPlayer player,
                                             EnumHand hand, EnumFacing hitFace, float hitX, float hitY, float hitZ) {
        ItemStack heldStack = player.getHeldItem(hand);
        Collection<Capability> availableCapabilities = CapabilityHelper.getItemCapabilities(heldStack);

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
            possibleInteraction.applyOutcome(world, pos, blockState, player, hitFace);

            if (availableCapabilities.contains(possibleInteraction.requiredCapability) && heldStack.isItemStackDamageable()) {
                heldStack.damageItem(2, player);
            }

            return true;
        }
        return false;
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
