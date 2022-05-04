package se.mickelus.tetra.items.modular.impl.holo.gui.scan;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiRoot;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.holo.ModularHolosphereItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class ScannerOverlayGui extends GuiRoot {
    public static final TagKey<Block> tag = BlockTags.create(new ResourceLocation("tetra", "scannable"));
    private static final int snoozeLength = 6000; // 5 min
    public static ScannerOverlayGui instance;
    private final ScannerBarGui scanner;
    BlockPos upHighlight;
    BlockPos midHighlight;
    BlockPos downHighlight;
    float widthRatio = 1;
    ScannerSound sound;
    // stats
    boolean available;
    int horizontalSpread = 44;
    int verticalSpread = 3;
    float cooldown = 1.2f;
    int range = 32;
    private int ticks;
    private int snooze = -1;

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
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GRINDSTONE_USE, 2f, 0.3f));
        } else {
            snooze = ticks + snoozeLength;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GRINDSTONE_USE, 1.6f, 0.3f));
        }
    }

    public boolean isSnoozed() {
        return ticks < snooze;
    }

    public String getStatus() {
        if (isSnoozed()) {
            int seconds = Math.round((snooze - ticks) / 20f);

            if (seconds > 60) {
                return I18n.get("tetra.holo.scan.snoozed", String.format("%02d", seconds / 60), String.format("%02d", seconds % 60));
            }

            return I18n.get("tetra.holo.scan.snoozed", String.format("%02d", seconds / 60), String.format("%02d", seconds % 60));
        } else {
            return I18n.get("tetra.holo.scan.active");
        }
    }

    private void updateStats() {
        ItemStack itemStack = ModularHolosphereItem.findHolosphere(mc.player);

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
        int scannerRange = Stream.of(mc.player.getMainHandItem(), mc.player.getOffhandItem())
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
        mc.getSoundManager().stop(sound);
        sound = new ScannerSound(mc);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        mc.getSoundManager().stop(sound);
        sound = new ScannerSound(mc);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Level world = mc.level;
        Player player = mc.player;

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
            int offset = (ticks / 2) % (int) (horizontalSpread * 2 * cooldown);
            if (offset < horizontalSpread * 2) {
                int yawOffset = (int) ((-horizontalSpread + offset) * ScannerBarGui.getDegreesPerUnit());
                if (offset % 2 == 0) {
                    if (verticalSpread > 0) {
                        upHighlight = IntStream.range(0, verticalSpread)
                                .map(i -> i * -5 - 25)
                                .mapToObj(pitch -> getPositions(player, world, pitch, yawOffset))
                                .filter(result -> result.getType() != HitResult.Type.MISS)
                                .map(BlockHitResult::getBlockPos)
                                .findAny()
                                .orElse(null);
                        scanner.highlightUp(offset / 2, upHighlight != null);
                        if (upHighlight != null) {
                            sound.activate();
                        }

                        downHighlight = IntStream.range(0, verticalSpread)
                                .map(i -> i * 5 + 25)
                                .mapToObj(pitch -> getPositions(player, world, pitch, yawOffset))
                                .filter(result -> result.getType() != HitResult.Type.MISS)
                                .map(BlockHitResult::getBlockPos)
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
                            .filter(result -> result.getType() != HitResult.Type.MISS)
                            .map(BlockHitResult::getBlockPos)
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

    private BlockHitResult getPositions(Player player, Level world, int pitchOffset, int yawOffset) {
        Vec3 eyePosition = player.getEyePosition(0);
        Vec3 lookVector = getVectorForRotation(player.getViewXRot(1) + pitchOffset, player.getViewYRot(1) + yawOffset);
        Vec3 endVector = eyePosition.add(lookVector.x * range, lookVector.y * range, lookVector.z * range);

        return world.isBlockInLine(new ClipBlockStateContext(eyePosition, endVector,
                blockState -> blockState.is(tag)));
    }

    private Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = pitch * ((float) Math.PI / 180F);
        float f1 = -yaw * ((float) Math.PI / 180F);
        float f2 = Mth.cos(f1);
        float f3 = Mth.sin(f1);
        float f4 = Mth.cos(f);
        float f5 = Mth.sin(f);
        return new Vec3(f3 * f4, -f5, f2 * f4);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            draw();
        }
    }

    @Override
    public void draw() {
        if (isVisible()) {
            Window window = mc.getWindow();
            width = window.getGuiScaledWidth();
            height = window.getGuiScaledHeight();

            int mouseX = (int) (mc.mouseHandler.xpos() * width / window.getScreenWidth());
            int mouseY = (int) (mc.mouseHandler.ypos() * height / window.getScreenHeight());

            this.drawChildren(new PoseStack(), 0, 0, width, height, mouseX, mouseY, 1.0F);

            widthRatio = scanner.getWidth() * 1f / width;
        }
    }
}
