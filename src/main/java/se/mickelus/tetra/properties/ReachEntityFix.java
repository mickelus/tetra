package se.mickelus.tetra.properties;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ReachEntityFix {
//    @OnlyIn(Dist.CLIENT)
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void onClientTick(TickEvent.ClientTickEvent event) {
//        Minecraft mc = Minecraft.getInstance();
//        if (event.phase == TickEvent.Phase.END
//                && mc.objectMouseOver != null
//                && mc.objectMouseOver.getType() != RayTraceResult.Type.ENTITY) {
//            Entity entity = mc.getRenderViewEntity();
//            double reach = mc.playerController.getBlockReachDistance();
//            if (reach != (ForgeMod.REACH_DISTANCE.get().getDefaultValue() - 0.5f)) {
//                Vector3d playerPos = entity.getEyePosition(1);
//                Vector3d lookVec = entity.getLook(1);
//                Vector3d targetVec = playerPos.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
//                float f = 1.0F;
//                AxisAlignedBB axisalignedbb = entity.getBoundingBox().expand(lookVec.scale(reach)).grow(1.0D, 1.0D, 1.0D);
//                EntityRayTraceResult rayTraceResult = ProjectileHelper.rayTraceEntities(entity, playerPos, targetVec, axisalignedbb,
//                        hit -> !hit.isSpectator() && hit.canBeCollidedWith(), reach * reach);
//
//                if (rayTraceResult != null) {
//                    mc.objectMouseOver = rayTraceResult;
//                    Entity hitEntity = rayTraceResult.getEntity();
//                    if (hitEntity instanceof LivingEntity || hitEntity instanceof ItemFrameEntity) {
//                        mc.pointedEntity = hitEntity;
//                    }
//                }
//            }
//        }
//    }

    private static void raytraceTarget() {
        Minecraft mc = Minecraft.getInstance();
        double reach = mc.gameMode.getPickRange() - 1.5f; // subtract as default entity reach is 3
        if (mc.hitResult != null
                && mc.hitResult.getType() != RayTraceResult.Type.ENTITY
                && reach > (ForgeMod.REACH_DISTANCE.get().getDefaultValue() - 2f)) {

            Entity entity = mc.getCameraEntity();
            Vector3d eyePos = entity.getEyePosition(1);
            Vector3d lookVec = entity.getViewVector(1);
            Vector3d targetVec = eyePos.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);

            AxisAlignedBB traceBox = entity.getBoundingBox().expandTowards(lookVec.scale(reach)).inflate(1.0D, 1.0D, 1.0D);

            if (mc.hitResult.getType() != RayTraceResult.Type.MISS) {
                reach = mc.hitResult.getLocation().distanceToSqr(eyePos);
            } else {
                reach = reach * reach;
            }

            EntityRayTraceResult rayTraceResult = ProjectileHelper.getEntityHitResult(entity, eyePos, targetVec, traceBox,
                    hit -> !hit.isSpectator() && hit.isPickable(), reach);

            if (rayTraceResult != null) {
                mc.hitResult = rayTraceResult;
                Entity hitEntity = rayTraceResult.getEntity();
                if (hitEntity instanceof LivingEntity || hitEntity instanceof ItemFrameEntity) {
                    mc.crosshairPickEntity = hitEntity;
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            raytraceTarget();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClickInput(InputEvent.ClickInputEvent event) {
        if (event.isAttack()) {
            raytraceTarget();
        }
    }
}
