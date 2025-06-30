package se.mickelus.tetra.effect.potion;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import se.mickelus.tetra.TetraDamageTypes;
import se.mickelus.tetra.client.particle.DripParticles;
import se.mickelus.tetra.effect.gui.EffectUnRenderer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class BleedingPotionEffect extends MobEffect {
    public static final String identifier = "bleeding";
    public static BleedingPotionEffect instance;

    public static final TagKey<EntityType<?>> slimebloodTag = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("tetra", "slimeblood"));
    public static final TagKey<EntityType<?>> lavabloodTag = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("tetra", "lavablood"));

    public BleedingPotionEffect() {
        super(MobEffectCategory.HARMFUL, 0x880000);

        instance = this;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        DamageSource source = entity.level().damageSources().source(TetraDamageTypes.bleeding);
        entity.hurt(source, amplifier);
        spawnParticles(entity, 2);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(EffectUnRenderer.INSTANCE);
    }

    public static void spawnParticles(LivingEntity entity, int count) {
        if (!entity.level().isClientSide) {
            RandomSource random = entity.getRandom();
            AABB boundingBox = entity.getBoundingBox().inflate(0.1, 0.1, 0.1);
            Vec3 target = getRandomVec3InAABB(boundingBox, random);
            ((ServerLevel) entity.level()).sendParticles(getBloodParticleType(entity),
                    target.x(), target.y(), target.z(),
                    count, 0f, 0f, 0f, 0.05f);
        }
    }

    private static Vec3 getRandomVec3InAABB(AABB box, RandomSource random) {
        double x = box.minX + random.nextDouble() * (box.maxX - box.minX);
        double y = box.minY + random.nextDouble() * (box.maxY - box.minY);
        double z = box.minZ + random.nextDouble() * (box.maxZ - box.minZ);
        return new Vec3(x, y, z);
    }

    private static SimpleParticleType getBloodParticleType(LivingEntity entity) {
        if (entity.getType().is(slimebloodTag)) {
            return DripParticles.fallingSlime.get();
        } else if (entity.getType().is(lavabloodTag)) {
            return ParticleTypes.FALLING_LAVA;
        } else {
            return DripParticles.fallingBlood.get();
        }
    }
}
