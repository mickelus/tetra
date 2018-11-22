package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.mickelus.tetra.blocks.hammer.BlockHammerBase;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;

import java.util.Arrays;
import java.util.Collection;

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

        outcome = ((world, pos, blockState, player) -> blockState.withProperty(BlockHammerBase.propFacing, EnumFacing.WEST));
    }

    public boolean applicableForState(IBlockState blockState) {
        return propertyValue.equals(blockState.getValue(property));
    }

    public boolean isWithinBounds(EnumFacing face, float x, float y) {
        return this.face.equals(face) && minX <= x && x <= maxX && minY <= y && y <= maxY;
    }

    public boolean isPotentialInteraction(IBlockState blockState, EnumFacing face, Collection<Capability> availableCapabilities) {
        return applicableForState(blockState) && this.face.equals(face) && availableCapabilities.contains(requiredCapability);
    }

    public void applyOutcome(World world, BlockPos pos, IBlockState blockState, EntityPlayer player) {
        outcome.apply(world, pos, blockState, player);
    }

    public static boolean attemptInteraction(World world, IBlockState blockState, BlockPos pos,EntityPlayer player,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        BlockInteraction interaction;

        BlockInteraction[] potentialInteractions = Optional.of(blockState)

        interaction = Arrays.stream(getPotentialInteractions(blockState, facing, CapabilityHelper.getPlayerCapabilities(player)))
                .filter(potentialInteraction -> potentialInteraction.isWithinBounds(facing, hitX * 16, hitY * 16))
                .findFirst()
                .orElse(null);

        if (interaction != null) {
            interaction.applyOutcome(world, pos, blockState, player);
            return true;
        }
        return false;
    }
}
