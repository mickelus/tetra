package se.mickelus.tetra.effect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class EffectHelper {
    private static final Cache<UUID, Float> cooledAttackStrengthCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    public static void setCooledAttackStrength(PlayerEntity player, float strength) {
        cooledAttackStrengthCache.put(player.getUniqueID(), strength);
    }

    public static float getCooledAttackStrength(PlayerEntity player) {
        try {
            return cooledAttackStrengthCache.get(player.getUniqueID(), () -> 0f);
        } catch (ExecutionException e) {
            return 0;
        }
    }

    private static final Cache<UUID, Boolean> sprintingCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();

    public static void setSprinting(LivingEntity player, boolean isSprinting) {
        sprintingCache.put(player.getUniqueID(), isSprinting);
    }

    public static boolean getSprinting(LivingEntity player) {
        try {
            return sprintingCache.get(player.getUniqueID(), () -> false);
        } catch (ExecutionException e) {
            return false;
        }
    }


    public static int getEffectLevel(ItemStack itemStack, ItemEffect effect) {
        IModularItem item = (IModularItem) itemStack.getItem();
        return item.getEffectLevel(itemStack, effect);
    }

    public static double getEffectEfficiency(ItemStack itemStack, ItemEffect effect) {
        IModularItem item = (IModularItem) itemStack.getItem();
        return item.getEffectEfficiency(itemStack, effect);
    }

    /**
     * Break a block in the world, as a player.
     * Based on how players break blocks in vanilla {@link net.minecraft.server.management.PlayerInteractionManager#tryHarvestBlock}, but allows
     * control over the used itemstack and without causing damage and honing progression for the used itemstack
     *
     * @param world the world in which to break blocks
     * @param breakingPlayer the player which is breaking the blocks
     * @param toolStack the itemstack used to break the blocks
     * @param pos the position which to break blocks around
     * @param blockState the state of the block that is to broken
     * @param harvest true if the player is ment to harvest the block, false if it should just magically disappear
     * @return True if the player was allowed to break the block, otherwise false
     */
    public static boolean breakBlock(World world, PlayerEntity breakingPlayer, ItemStack toolStack, BlockPos pos, BlockState blockState,
            boolean harvest) {
        if (!world.isRemote) {
            ServerWorld serverWorld = (ServerWorld) world;
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) breakingPlayer;
            GameType gameType = serverPlayer.interactionManager.getGameType();

            int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world, gameType, serverPlayer, pos);

            TileEntity tileEntity = world.getTileEntity(pos);

            if (exp != -1) {
                boolean canRemove = !toolStack.onBlockStartBreak(pos, breakingPlayer)
                        && !breakingPlayer.blockActionRestricted(world, pos, gameType)
                        && (!harvest || blockState.canHarvestBlock(world, pos, breakingPlayer))
                        && blockState.getBlock().removedByPlayer(blockState, world, pos, breakingPlayer, harvest, world.getFluidState(pos));

                if (canRemove) {
                    blockState.getBlock().onPlayerDestroy(world, pos, blockState);

                    if (harvest) {
                        blockState.getBlock().harvestBlock(world, breakingPlayer, pos, blockState, tileEntity, toolStack);

                        if (exp > 0) {
                            blockState.getBlock().dropXpOnBlockBreak(serverWorld, pos, exp);
                        }
                    }
                }
                return canRemove;
            }

            return false;
        } else {
            return blockState.getBlock().removedByPlayer(blockState, world, pos, breakingPlayer, harvest,
                    world.getFluidState(pos));
        }
    }

    /**
     * Sends an event to a specific player.
     * @param player the player the event will be sent to
     * @param type an integer representation of the event
     * @param pos the position in which the event takes place
     * @param data an integer representation of event data (e.g. Block.getStateId)
     */
    public static void sendEventToPlayer(ServerPlayerEntity player, int type, BlockPos pos, int data) {
        player.connection.sendPacket(new SPlaySoundEventPacket(type, pos, data, false));
    }

    /**
     * Variant on {@link EnchantmentHelper#applyArthropodEnchantments} that allows control over the held itemstack
     * @param itemStack
     * @param target
     * @param attacker
     */
    public static void applyEnchantmentHitEffects(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        EnchantmentHelper.getEnchantments(itemStack).forEach((enchantment, level) -> {
            enchantment.onEntityDamaged(attacker, target, level);
        });

        if (attacker != null) {
            for (ItemStack equipment: attacker.getEquipmentAndArmor()) {
                EnchantmentHelper.getEnchantments(equipment).forEach((enchantment, level) -> {
                    enchantment.onEntityDamaged(attacker, target, level);
                });
            }
        }

        // fire aspect has to be applied separately :o
        int fireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, itemStack);
        if (fireAspectLevel > 0) {
            target.setFire(fireAspectLevel * 4);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderInventoryEffectTooltip(DisplayEffectsScreen<?> gui, MatrixStack mStack, int x, int y, Supplier<ITextComponent> tooltip) {
        Minecraft mc = Minecraft.getInstance();
        MainWindow window = mc.getMainWindow();

        int width = window.getScaledWidth();
        int height = window.getScaledHeight();
        int mouseX = (int) (mc.mouseHelper.getMouseX() * width / window.getWidth());
        int mouseY = (int) (mc.mouseHelper.getMouseY() * height / window.getHeight());

        if (x < mouseX && mouseX < x + 120 && y < mouseY && mouseY < y + 32) {
            gui.renderTooltip(mStack, tooltip.get(), mouseX, mouseY);
        }
    }
}
