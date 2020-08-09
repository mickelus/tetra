package se.mickelus.tetra.items.modular.impl.holo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.scan.ScannerOverlayGui;
import se.mickelus.tetra.module.schematic.RemoveSchematic;
import se.mickelus.tetra.network.PacketHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class ModularHolosphereItem extends ModularItem {
    // todo 1.16: rename
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
                .maxStackSize(1)
                .group(TetraItemGroup.instance));

        canHone = false;

        setRegistryName(unlocalizedName);

        majorModuleKeys = new String[] { coreKey, frameKey, attachmentAKey, attachmentBKey };
        minorModuleKeys = new String[0];

        requiredModules = new String[] { coreKey, frameKey };
    }

    @Override
    public void init(PacketHandler packetHandler) {
        super.init(packetHandler);

        DataManager.synergyData.onReload(() -> synergies = DataManager.instance.getSynergyData("holo"));
        RemoveSchematic.registerRemoveSchematics(this);
    }

    @Override
    public void clientInit() {
        super.clientInit();

//        MinecraftForge.EVENT_BUS.register(new BlockHighlightRenderer());
        MinecraftForge.EVENT_BUS.register(new ScannerOverlayGui());
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (isInGroup(group)) {
            ItemStack itemStack = new ItemStack(this);

            putModuleInSlot(itemStack, coreKey, "holo/frame", "frame/ancient");
            putModuleInSlot(itemStack, frameKey, "holo/core", "core/dim");

            items.add(itemStack);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent("item.tetra.holo.tooltip1").mergeStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(" "));
        tooltip.add(new TranslationTextComponent("item.tetra.holo.tooltip2"));

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (world.isRemote) {
            showGui();
        }

        return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
    }

    @OnlyIn(Dist.CLIENT)
    private void showGui() {
        HoloGui gui = HoloGui.getInstance();

        Minecraft.getInstance().displayGuiScreen(gui);
        gui.onShow();
    }

    @Override
    public GuiModuleOffsets getMajorGuiOffsets() {
        return majorOffsets;
    }

    public double getCooldownBase(ItemStack itemStack) {
        double speedModifier = getAllModules(itemStack).stream()
                .map(itemModule -> itemModule.getSpeedModifier(itemStack))
                .reduce(0d, Double::sum);

        speedModifier = Arrays.stream(getSynergyData(itemStack))
                .mapToDouble(synergyData -> synergyData.attackSpeed)
                .reduce(speedModifier, Double::sum);

        speedModifier = Arrays.stream(getSynergyData(itemStack))
                .mapToDouble(synergyData -> synergyData.attackSpeedMultiplier)
                .reduce(speedModifier, (a, b) -> a * b);

        speedModifier = getAllModules(itemStack).stream()
                .map(itemModule -> itemModule.getSpeedMultiplierModifier(itemStack))
                .reduce(speedModifier, (a, b) -> a * b);

        return Math.max(0, speedModifier);
    }
}
