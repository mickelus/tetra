package se.mickelus.tetra.items.modular.impl.holo.gui.scan;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.mgui.gui.*;
import se.mickelus.mgui.gui.animation.AnimationChain;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

import java.util.ArrayList;
import java.util.List;

public class ScannerOverlayGui extends GuiRoot {

    private static final ResourceLocation tag = new ResourceLocation("tetra:scannable");

    private GuiElement scanner;

    List<BlockPos> upHighlights;
    List<BlockPos> midHighlights;
    List<BlockPos> downHighlights;

    int count = 30;

    protected AnimationChain[] upAnimations;
    protected AnimationChain[] upHighlightAnimations;
    protected AnimationChain[] midAnimations;
    protected AnimationChain[] midHighlightAnimations;
    protected AnimationChain[] downAnimations;
    protected AnimationChain[] downHighlightAnimations;

    public ScannerOverlayGui() {
        super(Minecraft.getInstance());
        upHighlights = new ArrayList<>();
        midHighlights = new ArrayList<>();
        downHighlights = new ArrayList<>();
        setup();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
//        if (event.side.isClient() && event.player.world.getGameTime() % 5 == 0) {
//            highlights = BlockPos.getAllInBox(event.player.getPosition().add(-16, -5, -16), event.player.getPosition().add(16, 5, 16))
//                    .filter(pos -> "tetra".equals(event.player.world.getBlockState(pos).getBlock().getRegistryName().getNamespace()))
//                    .map(BlockPos::new)
//                    .collect(Collectors.toList());
//        }

        if (event.side.isClient() && event.player.world.getGameTime() % 2 == 0) {

            int offset = (int) (event.player.world.getGameTime() / 2) % (count * 3);

            if (offset < count * 2) {
                if (offset % 2 == 0) {
                    upHighlights = getPositions(event.player, event.player.world, -30,(-count + offset) * 3);
                    downHighlights = getPositions(event.player, event.player.world, 30,(-count + offset) * 3);

                    if (upHighlights.isEmpty()) {
                        upAnimations[offset / 2].start();
                    } else {
                        upHighlightAnimations[offset / 2].start();
                        event.player.playSound(SoundEvents.BLOCK_PORTAL_AMBIENT, 0.1f, 2f);
                    }


                    if (downHighlights.isEmpty()) {
                        downAnimations[offset / 2].start();
                    } else {
                        downHighlightAnimations[offset / 2].start();
                        event.player.playSound(SoundEvents.BLOCK_PORTAL_AMBIENT, 0.1f, 1.6f);
                    }
                } else if (offset / 2 < count - 1) {
                    midHighlights = getPositions(event.player, event.player.world, 0,(-count + offset) * 3);

                    if (midHighlights.isEmpty()) {
                        midAnimations[offset / 2].start();
                    } else {
                        midHighlightAnimations[offset / 2].start();
                        event.player.playSound(SoundEvents.BLOCK_PORTAL_AMBIENT, 0.1f, 1.8f);
                    }
                }
            }
        }
    }

    private List<BlockPos> getPositions(PlayerEntity player, World world, int pitchOffset, int yawOffset) {
        float distance = 32;
        Vec3d eyePosition = player.getEyePosition(0);
        Vec3d lookVector = getVectorForRotation(player.getPitch(1) + pitchOffset, player.getYaw(1) + yawOffset);
        Vec3d endVector = eyePosition.add(lookVector.x * distance, lookVector.y * distance, lookVector.z * distance);

        ArrayList<BlockPos> result = new ArrayList<>();

        IBlockReader.func_217300_a(new RayTraceContext(eyePosition, endVector, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player), (ctx, blockPos) -> {
            BlockState blockState = world.getBlockState(blockPos);

            if (blockState.getBlock().getTags().contains(tag)) {
                result.add(blockPos.toImmutable());
            }

//            IFluidState ifluidstate = world.getFluidState(blockPos);
//            Vec3d start = ctx.func_222253_b();
//            Vec3d end = ctx.func_222250_a();
//            VoxelShape voxelshape = ctx.getBlockShape(blockstate, world, blockPos);
//            BlockRayTraceResult blockraytraceresult = world.rayTraceBlocks(start, end, blockPos, voxelshape, blockstate);
//            VoxelShape voxelshape1 = ctx.getFluidShape(ifluidstate, world, blockPos);
//            BlockRayTraceResult blockraytraceresult1 = voxelshape1.rayTrace(start, end, blockPos);
//            double d0 = blockraytraceresult == null ? Double.MAX_VALUE : ctx.func_222253_b().squareDistanceTo(blockraytraceresult.getHitVec());
//            double d1 = blockraytraceresult1 == null ? Double.MAX_VALUE : ctx.func_222253_b().squareDistanceTo(blockraytraceresult1.getHitVec());
            return null;
        }, ctx -> null);

        return result;
    }

    private Vec3d getVectorForRotation(float pitch, float yaw) {
        float f = pitch * ((float)Math.PI / 180F);
        float f1 = -yaw * ((float)Math.PI / 180F);
        float f2 = MathHelper.cos(f1);
        float f3 = MathHelper.sin(f1);
        float f4 = MathHelper.cos(f);
        float f5 = MathHelper.sin(f);
        return new Vec3d(f3 * f4, -f5, f2 * f4);
    }

    private void setup() {
        upAnimations = new AnimationChain[count];
        midAnimations = new AnimationChain[count];
        downAnimations = new AnimationChain[count];
        upHighlightAnimations = new AnimationChain[count];
        midHighlightAnimations = new AnimationChain[count];
        downHighlightAnimations = new AnimationChain[count];

        scanner = new GuiElement(1, 12, count * 6, 9);
        scanner.setAttachment(GuiAttachment.topCenter);
        addChild(scanner);

        scanner.addChild(new GuiRect(-3, -2, scanner.getWidth() + 3, scanner.getHeight() + 2, 0).setOpacity(0.5f));

        scanner.addChild(new GuiTexture(-2, -1, 2, 2, 1, 1, GuiTextures.hud).setOpacity(0.8f).setAttachment(GuiAttachment.topLeft));
        scanner.addChild(new GuiTexture(-2, -1, 2, 2, 1, 0, GuiTextures.hud).setOpacity(0.8f).setAttachment(GuiAttachment.bottomLeft));

        scanner.addChild(new GuiTexture(-1, -1, 2, 2, 0, 1, GuiTextures.hud).setOpacity(0.8f).setAttachment(GuiAttachment.topRight));
        scanner.addChild(new GuiTexture(-1, -1, 2, 2, 0, 0, GuiTextures.hud).setOpacity(0.8f).setAttachment(GuiAttachment.bottomRight));


        scanner.addChild(new GuiTexture(-2, -1, 3, 2, 0, 1, GuiTextures.hud).setOpacity(0.8f).setAttachment(GuiAttachment.topCenter));
        scanner.addChild(new GuiTexture(-2, -1, 3, 2, 0, 0, GuiTextures.hud).setOpacity(0.8f).setAttachment(GuiAttachment.bottomCenter));

        for (int i = 0; i < count; i++) {
            GuiElement up = new GuiTexture(i * 6, 0, 3, 3, GuiTextures.hud).setOpacity(0.3f);
            scanner.addChild(up);
            upAnimations[i] = new AnimationChain(
                    new KeyframeAnimation(100, up).applyTo(new Applier.Opacity(0.7f)),
                    new KeyframeAnimation(600, up).applyTo(new Applier.Opacity(0.3f)));


            GuiElement upHighlight = new GuiTexture(i * 6, 0, 3, 3, GuiTextures.hud)
                    .setColor(GuiColors.scanner)
                    .setOpacity(0);
            scanner.addChild(upHighlight);
            upHighlightAnimations[i] = new AnimationChain(
                    new KeyframeAnimation(100, upHighlight).applyTo(new Applier.Opacity(0.9f)),
                    new KeyframeAnimation(1000, upHighlight).applyTo(new Applier.Opacity(0)));

            GuiElement down = new GuiTexture(i * 6, 4, 3, 3, GuiTextures.hud).setOpacity(0.3f);
            scanner.addChild(down);
            downAnimations[i] = new AnimationChain(
                    new KeyframeAnimation(100, down).applyTo(new Applier.Opacity(0.7f)),
                    new KeyframeAnimation(600, down).applyTo(new Applier.Opacity(0.3f)));


            GuiElement downHighlight = new GuiTexture(i * 6, 4, 3, 3, GuiTextures.hud)
                    .setColor(GuiColors.scanner)
                    .setOpacity(0);
            scanner.addChild(downHighlight);
            downHighlightAnimations[i] = new AnimationChain(
                    new KeyframeAnimation(100, downHighlight).applyTo(new Applier.Opacity(0.9f)),
                    new KeyframeAnimation(1000, downHighlight).applyTo(new Applier.Opacity(0)));
        }

        for (int i = 0; i < count - 1; i++) {
            GuiElement center = new GuiTexture(i * 6 + 3, 2, 3, 3, GuiTextures.hud).setOpacity(0.3f);
            scanner.addChild(center);
            midAnimations[i] = new AnimationChain(
                    new KeyframeAnimation(100, center).applyTo(new Applier.Opacity(0.7f)),
                    new KeyframeAnimation(600, center).applyTo(new Applier.Opacity(0.3f)));

            GuiElement centerHighlight = new GuiTexture(i * 6 + 3, 2, 3, 3, GuiTextures.hud)
                    .setColor(GuiColors.scanner)
                    .setOpacity(0);
            scanner.addChild(centerHighlight);
            midHighlightAnimations[i] = new AnimationChain(
                    new KeyframeAnimation(100, centerHighlight).applyTo(new Applier.Opacity(0.9f)),
                    new KeyframeAnimation(1000, centerHighlight).applyTo(new Applier.Opacity(0)));
        }

//        addChild(new GuiRect(0, 0, 2, 2, GuiColors.hover).setAttachment(GuiAttachment.topLeft));
//        addChild(new GuiRect(0, 0, 2, 2, GuiColors.hover).setAttachment(GuiAttachment.topCenter));
//        addChild(new GuiRect(0, 0, 2, 2, GuiColors.hover).setAttachment(GuiAttachment.topRight));
//
//        addChild(new GuiRect(0, 0, 2, 2, GuiColors.hover).setAttachment(GuiAttachment.middleLeft));
//        addChild(new GuiRect(0, 0, 2, 2, GuiColors.hover).setAttachment(GuiAttachment.middleCenter));
//        addChild(new GuiRect(0, 0, 2, 2, GuiColors.hover).setAttachment(GuiAttachment.middleRight));
//
//        addChild(new GuiRect(0, 0, 2, 2, GuiColors.hover).setAttachment(GuiAttachment.bottomLeft));
//        addChild(new GuiRect(0, 0, 2, 2, GuiColors.hover).setAttachment(GuiAttachment.bottomCenter));
//        addChild(new GuiRect(0, 0, 2, 2, GuiColors.hover).setAttachment(GuiAttachment.bottomRight));

    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            draw();
        }
    }

    @Override
    public void draw() {
        if (isVisible()) {
            MainWindow window = mc.getMainWindow();
            width = window.getScaledWidth();
            height = window.getScaledHeight();

            int mouseX = (int) (mc.mouseHelper.getMouseX() * width / window.getWidth());
            int mouseY = (int) (mc.mouseHelper.getMouseY() * height / window.getHeight());

            this.drawChildren(new MatrixStack(), 0, 0, width, height, mouseX, mouseY, 1.0F);
        }
    }


    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderWorld(RenderWorldLastEvent event) {
        WorldRenderer worldRenderer = event.getContext();
        MatrixStack matrixStack = event.getMatrixStack();
//        Tessellator tessellator = Tessellator.getInstance();
//        IVertexBuilder vertexBuilder = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().getBuffer(RenderType.getLines());
//        Vec3d eyePos = Minecraft.getInstance().player.getEyePosition(event.getPartialTicks());
//
//        GlStateManager.lineWidth(3);
//        upHighlights.forEach(pos -> drawDebugBox(pos, eyePos, matrixStack, vertexBuilder, 1, 0, 0, 0.5f));
//        midHighlights.forEach(pos -> drawDebugBox(pos, eyePos, matrixStack, vertexBuilder, 0, 1, 0, 0.5f));
//        downHighlights.forEach(pos -> drawDebugBox(pos, eyePos, matrixStack, vertexBuilder, 0, 0, 1, 0.5f));
//        GlStateManager.lineWidth(1.0F);
    }

    private void drawDebugBox(BlockPos blockPos, Vec3d eyePos, MatrixStack matrixStack, IVertexBuilder vertexBuilder, float red, float green, float blue, float alpha) {
        Vec3d pos = new Vec3d(blockPos).subtract(eyePos);
        AxisAlignedBB aabb = new AxisAlignedBB(pos, pos.add(1, 1, 1));

        // draw center box
        WorldRenderer.drawBoundingBox(matrixStack, vertexBuilder, aabb.grow(0.0030000000949949026D), red, green, blue, alpha);
    }
}
