package se.mickelus.tetra.items.modular.impl.holo;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.scan.ScannerOverlayGui;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;
import se.mickelus.tetra.module.schematic.RemoveSchematic;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.properties.TetraAttributes;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
@ParametersAreNonnullByDefault
public class ModularHolosphereItem extends ModularItem {
    private static final String unlocalizedName = "holo";

    public final static String coreKey = "holo/core";
    public final static String frameKey = "holo/frame";
    public final static String attachmentAKey = "holo/attachment_0";
    public final static String attachmentBKey = "holo/attachment_1";

    private static final GuiModuleOffsets majorOffsets = new GuiModuleOffsets(-14, 0, -14, 18, 4, 0, 4, 18);

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ModularHolosphereItem instance;

    public ModularHolosphereItem() {
        super(new Properties()
                .stacksTo(1)
                .tab(TetraItemGroup.instance)
                .fireResistant());

        canHone = false;

        setRegistryName(unlocalizedName);

        majorModuleKeys = new String[] { coreKey, frameKey, attachmentAKey, attachmentBKey };
        minorModuleKeys = new String[0];

        requiredModules = new String[] { coreKey, frameKey };
    }

    @Override
    public void init(PacketHandler packetHandler) {
        super.init(packetHandler);

        DataManager.instance.synergyData.onReload(() -> synergies = DataManager.instance.getSynergyData("holo/"));
        RemoveSchematic.registerRemoveSchematics(this);
    }

    @Override
    public void clientInit() {
        super.clientInit();

        MinecraftForge.EVENT_BUS.register(new ScannerOverlayGui());
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (allowdedIn(group)) {
            ItemStack itemStack = new ItemStack(this);

            IModularItem.putModuleInSlot(itemStack, coreKey, "holo/core", "frame/dim");
            IModularItem.putModuleInSlot(itemStack, frameKey, "holo/frame", "core/ancient");

            items.add(itemStack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(new TranslatableComponent("item.tetra.holo.tooltip1").withStyle(ChatFormatting.GRAY));
        tooltip.add(new TextComponent(" "));

        if (ScannerOverlayGui.instance != null && ScannerOverlayGui.instance.isAvailable()) {
            tooltip.add(new TranslatableComponent("tetra.holo.scan.status", ScannerOverlayGui.instance.getStatus())
                    .withStyle(ChatFormatting.GRAY));

            tooltip.add(new TextComponent(" "));
            tooltip.add(new TranslatableComponent("tetra.holo.scan.snooze"));
        }

        tooltip.add(new TranslatableComponent("item.tetra.holo.tooltip2"));

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (world.isClientSide) {
            if (player.isCrouching() && ScannerOverlayGui.instance.isAvailable()) {
                ScannerOverlayGui.instance.toggleSnooze();
            } else {
                showGui();
            }
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @OnlyIn(Dist.CLIENT)
    private void showGui() {
        HoloGui gui = HoloGui.getInstance();

        Minecraft.getInstance().setScreen(gui);
        gui.onShow();
    }

    @Override
    public GuiModuleOffsets getMajorGuiOffsets() {
        return majorOffsets;
    }

    public double getCooldownBase(ItemStack itemStack) {
        return Math.max(0, getAttributeValue(itemStack, TetraAttributes.abilityCooldown.get()));
    }

    public static ItemStack findHolosphere(Player player) {
        return Stream.of(
                player.getInventory().offhand.stream(),
                player.getInventory().items.stream(),
                ToolbeltHelper.getToolbeltItems(player).stream())
                .flatMap(Function.identity())
                .filter(stack -> stack.getItem() instanceof ModularHolosphereItem)
                .findFirst()
                .orElse(ItemStack.EMPTY);
    }
}
