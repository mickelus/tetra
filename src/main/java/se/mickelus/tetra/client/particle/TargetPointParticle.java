package se.mickelus.tetra.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TargetPointParticle extends TextureSheetParticle {
    final SpriteSet sprites;
    double tarX;
    double tarY;
    double tarZ;
    int delay;

    List<Vector3f> colors;

    TargetPointParticle(ClientLevel level, double x, double y, double z, double tarX, double tarY, double tarZ, float friction, float gravity,
            int delay, SpriteSet spriteSet) {
        super(level, x, y, z);
        this.gravity = gravity;
        this.friction = friction;
        this.sprites = spriteSet;

        this.delay = delay;

        this.tarX = tarX;
        this.tarY = tarY;
        this.tarZ = tarZ;

        this.xd = 0;
        this.yd = 0.075 + Math.random() * 0.05;
        this.zd = 0;

        this.quadSize = 1 / 8f;
        this.lifetime = 150 + this.random.nextInt(30);
        this.hasPhysics = true;

        setSpriteFromAge(this.sprites);
    }

    public TargetPointParticle withInitialSpeed(double xd, double yd, double zd) {
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        return this;
    }

    public TargetPointParticle withYD(double yd) {
        this.yd = yd;
        return this;
    }

    public TargetPointParticle withColor(int... colors) {
        if (colors.length > 1) {
            this.colors = Arrays.stream(colors)
                    .mapToObj(color -> new Vector3f((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F))
                    .collect(Collectors.toList());
        } else {
            this.rCol = (colors[0] >> 16 & 255) / 255.0F;
            this.gCol = (colors[0] >> 8 & 255) / 255.0F;
            this.bCol = (colors[0] & 255) / 255.0F;
        }

        return this;
    }

    private void updateColors() {
        if (this.colors != null && this.colors.size() > 1) {
            int size = this.colors.size();
            float progress = Math.min(1f * age / lifetime * (size - 1), size - 1.00001f);
            int index = Math.min((int) progress, size - 2);
            lerpColors(progress % 1, colors.get(index), colors.get(index + 1));
        }

        if (age > lifetime - 5) {
            alpha = Math.max((lifetime - age) / 5f, 0);
        }
    }

    private void lerpColors(float factor, Vector3f fromColor, Vector3f toColor) {
        Vector3f vector3f = new Vector3f(fromColor);
        vector3f.lerp(toColor, factor);
        this.rCol = vector3f.x();
        this.gCol = vector3f.y();
        this.bCol = vector3f.z();
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(this.sprites);
        updateColors();

        if (age > this.delay) {
//            Vec2 n = new Vec2((float) (tarX - x), (float) (tarZ - z)).normalized().scale(0.1f + 1.2f * (1 - 1f * age / lifetime));
            Vec2 n = new Vec2((float) (tarX - x), (float) (tarZ - z)).scale(getApproxNormScale((float) (tarX - x), (float) (tarZ - z)));
            n = n.scale(getApproxNormScale(n.x, n.y) * 0.5f);
            if (age < lifetime - 10) {
                n = new Vec2((float) (tarX - x - n.y), (float) (tarZ - z + n.x));
                n = n.scale(getApproxNormScale(n.x, n.y));
            }
            xd = xd * 0.85 + n.x * 0.04;
            zd = zd * 0.85 + n.y * 0.04;

            yd = yd * 0.9 + (tarY - y + Math.sin(age / 10f + lifetime) * 0.2) * 0.005;
        } else {
            xd = xd * 0.95;
            yd = yd * 0.95;
            zd = zd * 0.95;
        }
    }

    /**
     * Cheap approximation of vector normalization, based on example provided by h3xed.com
     *
     * @param x
     * @param y
     * @return
     */
    private float getApproxNormScale(float x, float y) {
        x = Math.abs(x);
        y = Math.abs(y);
        float ratio = 1 / Math.max(x, y);
        ratio = ratio * (1.29289f - (x + y) * ratio * 0.29289f);
        return ratio;
    }

    public int getLightColor(float p_234080_) {
        return 240; // super.getLightColor(p_234080_);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
