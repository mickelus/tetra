package se.mickelus.tetra.effect.potion;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.joml.Vector3f;
import se.mickelus.mutil.effect.EffectTooltipRenderer;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.ArcaneFireBlock;
import se.mickelus.tetra.client.particle.SpawnParticlesPacket;
import se.mickelus.tetra.client.particle.SplinteredPowerParticle;
import se.mickelus.tetra.util.StreamHelper;
import se.mickelus.tetra.util.StringHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class UnstablePowerMobEffect extends MobEffect {
    public static final String identifier = "unstable_power";
    public static UnstablePowerMobEffect instance;
    private static final int splinterTreshhold = 16 * 20;

    public UnstablePowerMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xeeeeee);

        instance = this;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide() && entity.level().getGameTime() % 80 == 0 && entity.level().getRandom().nextInt(4) == 0) {
            MobEffectInstance current = entity.getEffect(instance);
            if (current.getDuration() >= splinterTreshhold) {
                RandomSource random = entity.level().getRandom();
                findSplinterPosition(entity.level(), entity.blockPosition()).ifPresent(blockPos -> {
                    if (current.getAmplifier() > 0 && random.nextInt(4) == 0) {
                        addOrUpdate(entity, 0, -1);
                    } else {
                        addOrUpdate(entity, -splinterTreshhold, 0);
                    }
                    TetraMod.packetHandler.sendToAllPlayersNear(new SpawnParticlesPacket(
                            entity.getX(), entity.getY(0.5f), entity.getZ(),
                            blockPos.getX() + 0.5f, blockPos.getY(), blockPos.getZ() + 0.5f, false, 8,
                            SplinteredPowerParticle.instance.get()),
                        entity.blockPosition(), 64, entity.level().dimension());

                    ServerScheduler.schedule(40, () -> spawnDust(entity.level(), blockPos, 0.5f));
                    ServerScheduler.schedule(60, () -> spawnDust(entity.level(), blockPos, 0.2f));

                    entity.level().playSound(null, blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8f, 1.2f);

                    ServerScheduler.schedule(80, () -> entity.level().setBlock(blockPos, ArcaneFireBlock.instance.get().defaultBlockState(),
                        Block.UPDATE_ALL));
                });
            }
        }
    }

    private void spawnDust(Level level, BlockPos pos, float spread) {
        RandomSource random = level.getRandom();
        ((ServerLevel) level).sendParticles(
            new DustColorTransitionOptions(new Vector3f(1, 0.5f, 0.725f), new Vector3f(1, 0.738f, 0.578f), 0.8f + random.nextFloat() * 0.4f),
            pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f,
            8, spread, spread, spread, 0.1);
    }

    private static Optional<BlockPos> findSplinterPosition(Level level, BlockPos origin) {
        BlockState blockState = ArcaneFireBlock.instance.get().defaultBlockState();
        return BlockPos.withinManhattanStream(origin, 6, 2, 6)
            .map(BlockPos::new)
            .filter(blockPos -> origin.distManhattan(blockPos) > 2)
            .collect(StreamHelper.toShuffledList())
            .stream()
            .filter(level::isEmptyBlock)
            .filter(blockPos -> blockState.canSurvive(level, blockPos))
            .findFirst();
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    public static void addOrUpdate(LivingEntity entity, int duration, int amplifier) {
        MobEffectInstance current = entity.getEffect(instance);
        int currentAmplifier = Optional.ofNullable(current)
            .map(MobEffectInstance::getAmplifier)
            .orElse(0);
        int updatedAmplifier = Math.min(Byte.MAX_VALUE, currentAmplifier + amplifier);

        int currentDuration = Optional.ofNullable(current)
            .map(MobEffectInstance::getDuration)
            .orElse(0);
        int updatedDuration = currentDuration + duration;

        if (updatedDuration < currentDuration || updatedAmplifier < currentAmplifier) {
            entity.removeEffect(instance);
        }

        if (updatedDuration > 0 && updatedAmplifier >= 0) {
            entity.addEffect(new MobEffectInstance(instance, updatedDuration, updatedAmplifier, false, false, true));
        }
    }

    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().hasEffect(instance)) {
            event.setNewSpeed(event.getNewSpeed() * (1.1f + event.getEntity().getEffect(instance).getAmplifier() * 0.1f));
        }
    }

    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity livingEntity && livingEntity.hasEffect(instance)) {
            event.setAmount(event.getAmount() * (1.1f + livingEntity.getEffect(instance).getAmplifier() * 0.1f));
        }
    }

    public static void onLivingDeath(Entity killedEntity, Entity killer) {
        if (killedEntity instanceof LivingEntity livingKilledEntity) {
            MobEffectInstance effectInstance = livingKilledEntity.getEffect(instance);
            if (effectInstance != null && killer instanceof LivingEntity livingKiller) {
                UnstablePowerMobEffect.addOrUpdate(livingKiller, effectInstance.getDuration(), effectInstance.getAmplifier());
            }

        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new ClientRenderer());
    }

    static class ClientRenderer extends EffectTooltipRenderer {
        private static final int fullBarColor = 0x88ffb584;
        private static final int shadowBarColor = 0x55ff8e9b;
        int maxDuration = 0;

        float delayedDuration = 0;
        long lastUpdate = 0;

        public ClientRenderer() {
            super(effect -> I18n.get("effect.tetra.unstable_power.tooltip",
                (effect.getAmplifier() + 1) * 10));
        }

        @Override
        public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphics guiGraphics, int x, int y, float z, float alpha) {
            Player player = Minecraft.getInstance().player;
            int duration = Optional.ofNullable(player.getEffect(UnstablePowerMobEffect.instance))
                .map(MobEffectInstance::getDuration)
                .orElse(0);
            int amplifier = Optional.ofNullable(player.getEffect(UnstablePowerMobEffect.instance))
                .map(MobEffectInstance::getAmplifier)
                .orElse(0);
            if (duration > maxDuration || duration == 0) {
                maxDuration = duration;
            }
            long time = System.currentTimeMillis();
            if (lastUpdate == 0) {
                lastUpdate = time;
            }
            long timeDelta = time - lastUpdate;
            float durationDelta = duration - delayedDuration;

            if (durationDelta > 1 || durationDelta < -1) {
                delayedDuration += timeDelta * durationDelta * 0.002f;
            } else {
                delayedDuration = duration;
            }

            lastUpdate = time;

            float ratio = Mth.clampedMap(duration, 0, maxDuration, 0, 1);
            float laggedRatio = Mth.clampedMap(delayedDuration, 0, maxDuration, 0, 1);
            renderBar(guiGraphics, x, y, Math.min(ratio, laggedRatio), fullBarColor);
            renderBar(guiGraphics, x, y, Math.max(ratio, laggedRatio), shadowBarColor);

            renderIcon(guiGraphics, x, y, duration);


            Font font = Minecraft.getInstance().font;
            String amplifierText = StringHelper.toRoman(amplifier + 1);
            int xo = 12 - font.width(amplifierText) / 2;
            guiGraphics.drawString(font, amplifierText, x + xo + 1, y + 18, 0x212121, false);
            guiGraphics.drawString(font, amplifierText, x + xo - 1, y + 18, 0x212121, false);
            guiGraphics.drawString(font, amplifierText, x + xo, y + 19, 0x212121, false);
            guiGraphics.drawString(font, amplifierText, x + xo, y + 17, 0x212121, false);
            guiGraphics.drawString(font, amplifierText, x + xo, y + 18, 0xffffff, false);

            return true;
        }

        private static void renderIcon(GuiGraphics guiGraphics, int x, int y, int duration) {
            float iconAlpha = 1;
            if (duration < 200) {
                int l = 10 - duration / 20;
                iconAlpha =
                    Mth.clamp((float) duration / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + Mth.cos((float) duration * (float) Math.PI / 5.0F) * Mth.clamp((float) l / 10.0F * 0.25F,
                        0.0F, 0.25F);
            }
            RenderSystem.enableBlend();
            TextureAtlasSprite textureatlassprite = Minecraft.getInstance().getMobEffectTextures().get(UnstablePowerMobEffect.instance);
            guiGraphics.setColor(1f, 1f, 1f, iconAlpha);
            guiGraphics.blit(x + 3, y + 3, 0, 18, 18, textureatlassprite);
            guiGraphics.setColor(1f, 1f, 1f, 1f);
        }

        private static void renderBar(GuiGraphics guiGraphics, int x, int y, float ratio, int color) {
            if (ratio > 0) {
                fill(guiGraphics, x + 10, y + 21, (int) Mth.clampedMap(ratio, 0f, 1 / 8f, 0, -7), 1, color);
                if (ratio > 1 / 8f) {
                    fill(guiGraphics, x + 2, y + 21, 1, (int) Mth.clampedMap(ratio, 1 / 8f, 3 / 8f, 0, -18), color);
                }
                if (ratio > 3 / 8f) {
                    fill(guiGraphics, x + 3, y + 2, (int) Mth.clampedMap(ratio, 3 / 8f, 5 / 8f, 0, 18), 1, color);
                }
                if (ratio > 5 / 8f) {
                    fill(guiGraphics, x + 21, y + 3, 1, (int) Mth.clampedMap(ratio, 5 / 8f, 7 / 8f, 0, 18), color);
                }
                if (ratio > 7 / 8f) {
                    fill(guiGraphics, x + 21, y + 21, (int) Mth.clampedMap(ratio, 7 / 8f, 1, 0, -7), 1, color);
                }
            }
        }

        private static void fill(GuiGraphics graphics, int x, int y, int width, int height, int color) {
            graphics.fill(x + Math.min(width, 0), y + Math.min(height, 0), x + Math.max(width, 0), y + Math.max(height, 0),
                color);
        }
    }
}
