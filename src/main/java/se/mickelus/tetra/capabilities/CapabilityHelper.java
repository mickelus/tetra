package se.mickelus.tetra.capabilities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CapabilityHelper {

    public static int getItemCapabilityLevel(ItemStack itemStack, Capability capability) {
        return Optional.of(itemStack)
                .filter(stack -> !stack.isEmpty())
                .map(CapabilityHelper::getReplacement)
                .filter(stack -> stack.getItem() instanceof ICapabilityProvider)
                .map(stack -> ((ICapabilityProvider) stack.getItem()).getCapabilityLevel(stack, capability))
                .orElse(0);
    }

    public static Collection<Capability> getItemCapabilities(ItemStack itemStack) {
        return Optional.of(itemStack)
                .filter(stack -> !stack.isEmpty())
                .map(CapabilityHelper::getReplacement)
                .filter(stack -> stack.getItem() instanceof ICapabilityProvider)
                .map(stack -> ((ICapabilityProvider) stack.getItem()).getCapabilities(stack))
                .orElse(Collections.emptyList());
    }

    public static int getPlayerEffectLevel(PlayerEntity player, ItemEffect effect) {
        return Stream.concat(player.inventory.offHandInventory.stream(), player.inventory.mainInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(CapabilityHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .map(itemStack -> ((ItemModular) itemStack.getItem()).getEffectLevel(itemStack, effect))
                .max(Integer::compare)
                .orElse(0);
    }

    public static double getPlayerEffectEfficiency(PlayerEntity player, ItemEffect effect) {
        return Stream.concat(player.inventory.offHandInventory.stream(), player.inventory.mainInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(CapabilityHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .max(Comparator.comparingInt(itemStack -> ((ItemModular) itemStack.getItem()).getEffectLevel(itemStack, effect)))
                .map(itemStack -> ((ItemModular) itemStack.getItem()).getEffectEfficiency(itemStack, effect))
                .orElse(0d);
    }

    public static int getPlayerCapabilityLevel(PlayerEntity player, Capability capability) {
        return Stream.concat(player.inventory.offHandInventory.stream(), player.inventory.mainInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(CapabilityHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof ICapabilityProvider)
                .map(itemStack -> ((ICapabilityProvider) itemStack.getItem()).getCapabilityLevel(itemStack, capability))
                .max(Integer::compare)
                .orElse(0);
    }

    public static Collection<Capability> getPlayerCapabilities(PlayerEntity player) {
        return Stream.concat(player.inventory.offHandInventory.stream(), player.inventory.mainInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(CapabilityHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof ICapabilityProvider)
                .flatMap(itemStack -> ((ICapabilityProvider) itemStack.getItem()).getCapabilities(itemStack).stream())
                .collect(Collectors.toSet());
    }

    private static ItemStack getReplacement(ItemStack itemStack) {
        ItemStack replacement = ItemUpgradeRegistry.instance.getReplacement(itemStack);
        if (!replacement.isEmpty()) {
            return replacement;
        }
        return itemStack;
    }

    public static int getBlockCapabilityLevel(World world, BlockPos pos, IBlockState blockStateIn, Capability capability) {
        return Optional.of(blockStateIn)
                .filter(blockState -> blockState.getBlock() instanceof ITetraBlock)
                .map(blockState -> (ITetraBlock) blockState.getBlock())
                .map(block -> block.getCapabilityLevel(world, pos, blockStateIn, capability))
                .orElse(-1);
    }

    public static Collection<Capability> getBlockCapabilities(World world, BlockPos pos, IBlockState blockStateIn) {
        return Optional.of(blockStateIn)
                .filter(blockState -> blockState.getBlock() instanceof ITetraBlock)
                .map(blockState -> (ITetraBlock) blockState.getBlock())
                .map(block -> block.getCapabilities(world, pos, blockStateIn))
                .orElse(Collections.emptyList());
    }

    public static int getCombinedCapabilityLevel(PlayerEntity player, World world, BlockPos pos, IBlockState blockStateIn, Capability capability) {
        return Math.max(getPlayerCapabilityLevel(player, capability), getBlockCapabilityLevel(world, pos, blockStateIn, capability));
    }

    public static int[] getCombinedCapabilityLevels(PlayerEntity player, World world, BlockPos pos, IBlockState blockStateIn) {
        return Arrays.stream(Capability.values())
                .mapToInt(capability -> getCombinedCapabilityLevel(player, world, pos, blockStateIn, capability))
                .toArray();
    }

    public static ItemStack getProvidingItemStack(Capability capability, int level, PlayerEntity player) {
        return Stream.concat(Stream.of(player.getHeldItemMainhand(), player.getHeldItemOffhand()), player.inventory.mainInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(CapabilityHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof ICapabilityProvider)
                .filter(itemStack -> ((ICapabilityProvider) itemStack.getItem()).getCapabilityLevel(itemStack, capability) >= level)
                .findAny()
                .orElse(ItemStack.EMPTY);
    }
}
