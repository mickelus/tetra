package se.mickelus.tetra.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
public class SweepingStrikeParticle extends TextureSheetParticle {
    @OnlyIn(Dist.CLIENT)
    ParticleRenderType renderType = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            RenderSystem.disableCull(); // needs custom render type for this
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return "PARTICLE_SHEET_LIT";
        }

        @Override
        public boolean isTranslucent() {
            return false;
        }
    };

    private static final Vector3f ROTATION_VECTOR = new Vector3f(0.5F, 0.5F, 0.5F).normalize();
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
    private final boolean reverse;
    private final SpriteSet sprites;

    private final float pitch;
    private final float yaw;

    protected SweepingStrikeParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet, int lifetime, boolean reverse, float pitch, float yaw) {
        super(level, x, y, z, 0, 0, 0);

        float shade = 0.6f + random.nextFloat() * 0.4f;
        this.rCol = shade;
        this.gCol = shade;
        this.bCol = shade;
        this.quadSize = 1.0F;

        this.pitch = pitch / 180F * Mth.PI;
        this.yaw = yaw / 180F * Mth.PI;

        this.sprites = spriteSet;
        this.lifetime = lifetime;
        this.reverse = reverse;


        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return renderType;
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTicks) {
        this.renderRotatedParticle(consumer, camera, partialTicks, quaternion -> {
            quaternion.mul(Axis.YP.rotation(-yaw));
            quaternion.mul(Axis.XP.rotation(pitch + Mth.PI / 3f));
        });
    }

    private void renderRotatedParticle(VertexConsumer consumer, Camera camera, float partialTicks, Consumer<Quaternionf> transformApplier) {
        Vec3 vec3 = camera.getPosition();
        float x = (float) (this.x - vec3.x());
        float y = (float) (this.y - vec3.y());
        float z = (float) (this.z - vec3.z());
        Quaternionf quaternion = new Quaternionf().setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());
        ;
        transformApplier.accept(quaternion);
        quaternion.transform(TRANSFORM_VECTOR);
        Vector3f[] avector3f = new Vector3f[] {
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float size = this.getQuadSize(partialTicks);

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            quaternion.transform(vector3f);
            vector3f.mul(size);
            vector3f.add(x, y, z);
        }

        int light = this.getLightColor(partialTicks);
        this.makeCornerVertex(consumer, avector3f[0], this.getU1(), this.getV1(), light);
        this.makeCornerVertex(consumer, avector3f[1], this.getU1(), this.getV0(), light);
        this.makeCornerVertex(consumer, avector3f[2], this.getU0(), this.getV0(), light);
        this.makeCornerVertex(consumer, avector3f[3], this.getU0(), this.getV1(), light);
    }

    private void makeCornerVertex(VertexConsumer consumer, Vector3f pos, float u, float v, int light) {
        consumer.addVertex(pos.x(), pos.y(), pos.z())
                .setUv(u, v)
                .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
                .setLight(light);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.sprites);
        }
    }

    @Override
    protected float getU0() {
        return reverse ? super.getU1() : super.getU0();
    }

    @Override
    protected float getU1() {
        return reverse ? super.getU0() : super.getU1();
    }

    @OnlyIn(Dist.CLIENT)
    @ParametersAreNonnullByDefault
    public static class Provider implements ParticleProvider<SweepingStrikeParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SweepingStrikeParticleOption option, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new SweepingStrikeParticle(level, x, y, z, this.sprites, option.duration(), option.reverse(), option.pitch(), option.yaw());
        }
    }
}
