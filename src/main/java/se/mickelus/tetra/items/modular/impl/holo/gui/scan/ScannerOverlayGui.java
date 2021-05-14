package se.mickelus.tetra.items.modular.impl.holo.gui.scan;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiRoot;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.holo.ModularHolosphereItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;
import se.mickelus.tetra.effect.ItemEffect;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ScannerOverlayGui extends GuiRoot {
    private static final ResourceLocation tag = new ResourceLocation("tetra:scannable");

    public static ScannerOverlayGui instance;

    private ScannerBarGui scanner;

    BlockPos upHighlight;
    BlockPos midHighlight;
    BlockPos downHighlight;

    float widthRatio = 1;

    ScannerSound sound;

    private int ticks;

    private int snooze = -1;
    private static final int snoozeLength = 6000; // 5 min

    // stats
    boolean available;
    int horizontalSpread = 44;
    int verticalSpread = 3;
    float cooldown = 1.2f;
    int range = 32;

    public ScannerOverlayGui() {
        super(Minecraft.getInstance());

        scanner = new ScannerBarGui(2, 16, horizontalSpread);
        scanner.setAttachment(GuiAttachment.topCenter);
        scanner.setOpacity(0);
        scanner.setVisible(false);
        addChild(scanner);

        sound = new ScannerSound(mc);

        if (ConfigHandler.development.get()) {
            MinecraftForge.EVENT_BUS.register(new ScannerDebugRenderer(this));
        }

        instance = this;
    }

    public boolean isAvailable() {
        return available;
    }

    public void toggleSnooze() {
        if (isSnoozed()) {
            snooze = -1;
            mc.getSoundHandler().play(SimpleSound.master(SoundEvents.BLOCK_GRINDSTONE_USE, 2f, 0.3f));
        } else {
            snooze = ticks + snoozeLength;
            mc.getSoundHandler().play(SimpleSound.master(SoundEvents.BLOCK_GRINDSTONE_USE, 1.6f, 0.3f));
        }
    }

    public boolean isSnoozed() {
        return ticks < snooze;
    }

    public String getStatus() {
        if (isSnoozed()) {
            int seconds = Math.round((snooze - ticks) / 20f);

            if (seconds > 60) {
                return I18n.format("tetra.holo.scan.snoozed", String.format("%02d", seconds / 60), String.format("%02d", seconds % 60));
            }

            return I18n.format("tetra.holo.scan.snoozed", String.format("%02d", seconds / 60), String.format("%02d", seconds % 60));
        } else {
            return I18n.format("tetra.holo.scan.active");
        }
    }

    private void updateStats() {
        ItemStack itemStack = Stream.of(
                mc.player.inventory.offHandInventory.stream(),
                mc.player.inventory.mainInventory.stream(),
                ToolbeltHelper.getToolbeltItems(mc.player).stream())
                .flatMap(Function.identity())
                .filter(stack -> stack.getItem() instanceof ModularHolosphereItem)
                .findFirst()
                .orElse(ItemStack.EMPTY);

        if (!itemStack.isEmpty()) {
            ModularHolosphereItem item = (ModularHolosphereItem) itemStack.getItem();
            horizontalSpread = 2 * item.getEffectLevel(itemStack, ItemEffect.scannerHorizontalSpread);
            verticalSpread = item.getEffectLevel(itemStack, ItemEffect.scannerVerticalSpread);
            range = item.getEffectLevel(itemStack, ItemEffect.scannerRange);

            cooldown = Math.max((float) item.getCooldownBase(itemStack), 1);

            scanner.setHorizontalSpread(horizontalSpread);

            available = range > 0;
        } else {
            available = false;
        }
    }

    private void updateGuiVisibility() {
        int scannerRange = Stream.of(mc.player.getHeldItemMainhand(), mc.player.getHeldItemOffhand())
                .filter(stack -> stack.getItem() instanceof ModularHolosphereItem)
                .map(stack -> ((IModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.scannerRange))
                .findFirst()
                .orElse(0);

        if (!scanner.isVisible() && scannerRange > 0) {
            updateStats();
        }

        if (scannerRange > 0) {
            scanner.show();
        } else {
            scanner.hide();
        }
    }


    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        mc.getSoundHandler().stop(sound);
        sound = new ScannerSound(mc);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        mc.getSoundHandler().stop(sound);
        sound = new ScannerSound(mc);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        World world = mc.world;
        PlayerEntity player = mc.player;

        if (world == null || player == null || TickEvent.Phase.START != event.phase) {
            return;
        }

        updateGuiVisibility();
        ticks++;

        if (ticks % 200 == 0) {
            updateStats();
        }

        if (available && ticks % 20 == 0) {
            if (isSnoozed()) {
                scanner.setStatus(getStatus());
            } else {
                scanner.setStatus(null);
            }
        }

        if (available && ticks % 2 == 0 && !isSnoozed()) {
            int offset = (int) (ticks / 2) % (int) (horizontalSpread * 2 * cooldown);
            if (offset < horizontalSpread * 2) {
                int yawOffset = (int) ((-horizontalSpread + offset) * ScannerBarGui.getDegreesPerUnit());
                if (offset % 2 == 0) {
                    if (verticalSpread > 0) {
                        upHighlight = IntStream.range(0, verticalSpread)
                                .map(i -> i * -5 - 25)
                                .mapToObj(pitch -> getPositions(player, world, pitch, yawOffset))
                                .filter(Objects::nonNull)
                                .findAny()
                                .orElse(null);
                        scanner.highlightUp(offset / 2, upHighlight != null);
                        if (upHighlight != null) {
                            sound.activate();
                        }

                        downHighlight = IntStream.range(0, verticalSpread)
                                .map(i -> i * 5 + 25)
                                .mapToObj(pitch -> getPositions(player, world, pitch, yawOffset))
                                .filter(Objects::nonNull)
                                .findAny()
                                .orElse(null);
                        scanner.highlightDown(offset / 2, downHighlight != null);
                        if (downHighlight != null) {
                            sound.activate();
                        }
                    }
                } else if (offset / 2 < horizontalSpread - 1) {
                    midHighlight = IntStream.range(-1, 2)
                            .map(i -> i * 10)
                            .mapToObj(pitch -> getPositions(player, world, pitch, yawOffset))
                            .filter(Objects::nonNull)
                            .findAny()
                            .orElse(null);

                    scanner.highlightMid(offset / 2, midHighlight != null);
                    if (midHighlight != null) {
                        sound.activate();
                    }

                }
            }
        }
    }

    @Nullable
    private BlockPos getPositions(PlayerEntity player, World world, int pitchOffset, int yawOffset) {
        Vector3d eyePosition = player.getEyePosition(0);
        Vector3d lookVector = getVectorForRotation(player.getPitch(1) + pitchOffset, player.getYaw(1) + yawOffset);
        Vector3d endVector = eyePosition.add(lookVector.x * range, lookVector.y * range, lookVector.z * range);

        return IBlockReader.doRayTrace(new RayTraceContext(eyePosition, endVector, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player), (ctx, blockPos) -> {
            BlockState blockState = world.getBlockState(blockPos);

            if (blockState.getBlock().getTags().contains(tag)) {
                return blockPos.toImmutable();
            }
            return null;
        }, ctx -> null);
    }

    private Vector3d getVectorForRotation(float pitch, float yaw) {
        float f = pitch * ((float)Math.PI / 180F);
        float f1 = -yaw * ((float)Math.PI / 180F);
        float f2 = MathHelper.cos(f1);
        float f3 = MathHelper.sin(f1);
        float f4 = MathHelper.cos(f);
        float f5 = MathHelper.sin(f);
        return new Vector3d(f3 * f4, -f5, f2 * f4);
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

            widthRatio = scanner.getWidth() * 1f / width;
        }
    }
}
