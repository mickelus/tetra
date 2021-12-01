package se.mickelus.tetra.effect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

    public static void setCooledAttackStrength(Player player, float strength) {
        cooledAttackStrengthCache.put(player.getUUID(), strength);
    }

    public static float getCooledAttackStrength(Player player) {
        try {
            return cooledAttackStrengthCache.get(player.getUUID(), () -> 0f);
        } catch (ExecutionException e) {
            return 0;
        }
    }

    private static final Cache<UUID, Boolean> sprintingCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();

    public static void setSprinting(LivingEntity player, boolean isSprinting) {
        sprintingCache.put(player.getUUID(), isSprinting);
    }

    public static boolean getSprinting(LivingEntity player) {
        try {
            return sprintingCache.get(player.getUUID(), () -> false);
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
    public static boolean breakBlock(Level world, Player breakingPlayer, ItemStack toolStack, BlockPos pos, BlockState blockState,
            boolean harvest) {
        if (!world.isClientSide) {
            ServerLevel serverWorld = (ServerLevel) world;
            ServerPlayer serverPlayer = (ServerPlayer) breakingPlayer;
            GameType gameType = serverPlayer.gameMode.getGameModeForPlayer();

            int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world, gameType, serverPlayer, pos);

            BlockEntity tileEntity = world.getBlockEntity(pos);

            if (exp != -1) {
                boolean canRemove = !toolStack.onBlockStartBreak(pos, breakingPlayer)
                        && !breakingPlayer.blockActionRestricted(world, pos, gameType)
                        && (!harvest || blockState.canHarvestBlock(world, pos, breakingPlayer))
                        && blockState.getBlock().removedByPlayer(blockState, world, pos, breakingPlayer, harvest, world.getFluidState(pos));

                if (canRemove) {
                    blockState.getBlock().destroy(world, pos, blockState);

                    if (harvest) {
                        blockState.getBlock().playerDestroy(world, breakingPlayer, pos, blockState, tileEntity, toolStack);

                        if (exp > 0) {
                            blockState.getBlock().popExperience(serverWorld, pos, exp);
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
    public static void sendEventToPlayer(ServerPlayer player, int type, BlockPos pos, int data) {
        player.connection.send(new ClientboundLevelEventPacket(type, pos, data, false));
    }

    /**
     * Variant on {@link EnchantmentHelper#applyArthropodEnchantments} that allows control over the held itemstack
     * @param itemStack
     * @param target
     * @param attacker
     */
    public static void applyEnchantmentHitEffects(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        EnchantmentHelper.getEnchantments(itemStack).forEach((enchantment, level) -> {
            enchantment.doPostAttack(attacker, target, level);
        });

        if (attacker != null) {
            for (ItemStack equipment: attacker.getAllSlots()) {
                EnchantmentHelper.getEnchantments(equipment).forEach((enchantment, level) -> {
                    enchantment.doPostAttack(attacker, target, level);
                });
            }
        }

        // fire aspect has to be applied separately :o
        int fireAspectLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, itemStack);
        if (fireAspectLevel > 0) {
            target.setSecondsOnFire(fireAspectLevel * 4);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderInventoryEffectTooltip(EffectRenderingInventoryScreen<?> gui, PoseStack mStack, int x, int y, Supplier<Component> tooltip) {
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();

        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();
        int mouseX = (int) (mc.mouseHandler.xpos() * width / window.getScreenWidth());
        int mouseY = (int) (mc.mouseHandler.ypos() * height / window.getScreenHeight());

        if (x < mouseX && mouseX < x + 120 && y < mouseY && mouseY < y + 32) {
            gui.renderTooltip(mStack, tooltip.get(), mouseX, mouseY);
        }
    }
}
