package se.mickelus.tetra.effect.potion;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
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
        if (!entity.level().isClientSide()) {
            if (entity.level().getRandom().nextInt(4) == 0) {
                MobEffectInstance current = entity.getEffect(instance);
                if (current.getDuration() >= splinterTreshhold) {
                    RandomSource random = entity.level().getRandom();
                    findSplinterPosition(entity.level(), entity.blockPosition()).ifPresent(blockPos -> {
                        // offset duration drain by 1 or the tick handler applies twice
                        if (current.getAmplifier() > 0 && random.nextInt(4) == 0) {
                            addOrUpdate(entity, -1, -1);
                        } else {
                            addOrUpdate(entity, -splinterTreshhold - 1, 0);
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
        return duration % (80) == 0;
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new EffectTooltipRenderer(effect -> I18n.get("effect.tetra.unstable_power.tooltip",
                (effect.getAmplifier() + 1) * 10)));
    }
}
