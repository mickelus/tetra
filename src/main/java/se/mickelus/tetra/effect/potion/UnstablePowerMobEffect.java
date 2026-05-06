package se.mickelus.tetra.effect.potion;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import se.mickelus.mutil.effect.EffectTooltipRenderer;
import se.mickelus.tetra.blocks.ArcaneFireBlock;
import se.mickelus.tetra.client.particle.Particles;
import se.mickelus.tetra.util.StreamHelper;
import se.mickelus.tetra.util.StringHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
@ParametersAreNonnullByDefault
public class UnstablePowerMobEffect extends MobEffect {
    public static final String identifier = "unstable_power";
    public static UnstablePowerMobEffect instance;
    private static final int splinterTreshhold = 16 * 20;

    public static final float bonusMultiplier = 0.05f;

    public UnstablePowerMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xeeeeee);

        instance = this;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            if (entity.level().getGameTime() % 10 == 0) {
                MobEffectInstance current = entity.getEffect(se.mickelus.tetra.effect.EffectHelper.effectHolder(instance));
                if (current != null) {
                    splinterAmplifier(current, entity);
                    splinterDuration(current, entity);
                }
            }
            if (entity.level().getGameTime() % 20 == 0 && entity.level().getRandom().nextFloat() < 0.25) {
                Particles.addSputteringPower((ServerLevel) entity.level(), entity.getX(), entity.getY(0.5), entity.getZ(), entity);
            }
        }
        return true;
    }

    private void splinterAmplifier(MobEffectInstance current, LivingEntity entity) {
        if (current.getAmplifier() > 0) {
            int interval = 10 * (int) Mth.clampedMap(current.getAmplifier(), 1, 16, 16, 1);
            float chance = Mth.clampedMap(current.getAmplifier(), 1, 32, 0.1f, 0.9f);
            if (entity.level().getGameTime() % interval == 0 && entity.level().getRandom().nextFloat() < chance) {
                addOrUpdate(entity, 0, -1);
                findSplinterPosition(entity.level(), entity.blockPosition()).ifPresent(blockPos ->
                        ArcaneFireBlock.spawnDelayed((ServerLevel) entity.level(), blockPos, entity.blockPosition().getCenter()));
            }
        }
    }

    private void splinterDuration(MobEffectInstance current, LivingEntity entity) {
        if (current.getDuration() > splinterTreshhold) {
            int interval = 20 * (int) Mth.clampedMap(current.getDuration(), splinterTreshhold, 3600, 8, 1);
            float chance = Mth.clampedMap(current.getDuration(), 1200, 7200, 0.05f, 0.9f);
            if (entity.level().getGameTime() % interval == 0 && entity.level().getRandom().nextFloat() < chance) {
                addOrUpdate(entity, -splinterTreshhold, 0);
                findSplinterPosition(entity.level(), entity.blockPosition()).ifPresent(blockPos ->
                        ArcaneFireBlock.spawnDelayed((ServerLevel) entity.level(), blockPos, entity.blockPosition().getCenter()));
            }
        }
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
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    public static void addOrUpdate(LivingEntity entity, int duration, int amplifier) {
        var effect = se.mickelus.tetra.effect.EffectHelper.effectHolder(instance);
        MobEffectInstance current = entity.getEffect(effect);
        int currentAmplifier = Optional.ofNullable(current)
                .map(MobEffectInstance::getAmplifier)
                .orElse(0);
        int updatedAmplifier = Math.min(Byte.MAX_VALUE, currentAmplifier + amplifier);

        int currentDuration = Optional.ofNullable(current)
                .map(MobEffectInstance::getDuration)
                .orElse(0);
        int updatedDuration = currentDuration + duration;

        if (updatedDuration < currentDuration || updatedAmplifier < currentAmplifier) {
            entity.removeEffect(effect);
        }

        if (updatedDuration > 0 && updatedAmplifier >= 0) {
            entity.addEffect(new MobEffectInstance(effect, updatedDuration, updatedAmplifier, false, false, true));
        }
    }

    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        var effect = se.mickelus.tetra.effect.EffectHelper.effectHolder(instance);
        if (event.getEntity().hasEffect(effect)) {
            event.setNewSpeed(event.getNewSpeed() * (1 + bonusMultiplier + event.getEntity().getEffect(effect).getAmplifier() * bonusMultiplier));
        }
    }

    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        var effect = se.mickelus.tetra.effect.EffectHelper.effectHolder(instance);
        if (event.getSource().getEntity() instanceof LivingEntity livingEntity && livingEntity.hasEffect(effect)) {
            event.setNewDamage(event.getNewDamage() * (1 + bonusMultiplier + livingEntity.getEffect(effect).getAmplifier() * bonusMultiplier));
        }
    }

    public static void onLivingDeath(Entity killedEntity, Entity killer) {
        if (killedEntity instanceof LivingEntity livingKilledEntity) {
            var effect = se.mickelus.tetra.effect.EffectHelper.effectHolder(instance);
            MobEffectInstance effectInstance = livingKilledEntity.getEffect(effect);
            if (effectInstance != null && killer instanceof LivingEntity livingKiller) {
                UnstablePowerMobEffect.addOrUpdate(livingKiller, effectInstance.getDuration(), effectInstance.getAmplifier());
            }

        }
    }

    public static class ClientRenderer extends EffectTooltipRenderer {
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
            var effect = se.mickelus.tetra.effect.EffectHelper.effectHolder(UnstablePowerMobEffect.instance);
            int duration = Optional.ofNullable(player.getEffect(effect))
                    .map(MobEffectInstance::getDuration)
                    .orElse(0);
            int amplifier = Optional.ofNullable(player.getEffect(effect))
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

            renderIcon(guiGraphics, x, y, duration, effect);

            renderAmplifierLabel(guiGraphics, x, y, amplifier);

            return true;
        }

        private static void renderIcon(GuiGraphics guiGraphics, int x, int y, int duration, net.minecraft.core.Holder<MobEffect> effect) {
            float iconAlpha = 1;
            if (duration < 200) {
                int l = 10 - duration / 20;
                iconAlpha =
                        Mth.clamp((float) duration / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + Mth.cos((float) duration * (float) Math.PI / 5.0F) * Mth.clamp((float) l / 10.0F * 0.25F,
                                0.0F, 0.25F);
            }
            RenderSystem.enableBlend();
            TextureAtlasSprite textureatlassprite = Minecraft.getInstance().getMobEffectTextures().get(effect);
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

        private static void renderAmplifierLabel(GuiGraphics guiGraphics, int x, int y, int amplifier) {
            Font font = Minecraft.getInstance().font;
            String amplifierText = StringHelper.toRoman(amplifier + 1);
            int xo = 12 - font.width(amplifierText) / 2;
            guiGraphics.drawString(font, amplifierText, x + xo + 1, y + 18, 0x212121, false);
            guiGraphics.drawString(font, amplifierText, x + xo - 1, y + 18, 0x212121, false);
            guiGraphics.drawString(font, amplifierText, x + xo, y + 19, 0x212121, false);
            guiGraphics.drawString(font, amplifierText, x + xo, y + 17, 0x212121, false);
            guiGraphics.drawString(font, amplifierText, x + xo, y + 18, 0xffffff, false);
        }
    }
}
