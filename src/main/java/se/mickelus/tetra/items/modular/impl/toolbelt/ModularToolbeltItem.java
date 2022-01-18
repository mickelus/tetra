package se.mickelus.tetra.items.modular.impl.toolbelt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.booster.JumpHandlerBooster;
import se.mickelus.tetra.items.modular.impl.toolbelt.booster.OverlayBooster;
import se.mickelus.tetra.items.modular.impl.toolbelt.booster.TickHandlerBooster;
import se.mickelus.tetra.items.modular.impl.toolbelt.booster.UpdateBoosterPacket;
import se.mickelus.tetra.items.modular.impl.toolbelt.gui.ToolbeltScreen;
import se.mickelus.tetra.items.modular.impl.toolbelt.suspend.JumpHandlerSuspend;
import se.mickelus.tetra.items.modular.impl.toolbelt.suspend.ToggleSuspendPacket;
import se.mickelus.tetra.module.schematic.RemoveSchematic;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class ModularToolbeltItem extends ModularItem implements MenuProvider {
    public final static String unlocalizedName = "modular_toolbelt";

    public final static String slot1Key = "toolbelt/slot1";
    public final static String slot2Key = "toolbelt/slot2";
    public final static String slot3Key = "toolbelt/slot3";
    public final static String beltKey = "toolbelt/belt";

    public final static String slot1Suffix = "_slot1";
    public final static String slot2Suffix = "_slot2";
    public final static String slot3Suffix = "_slot3";

    private static final GuiModuleOffsets majorOffsets = new GuiModuleOffsets(-14, 18, 4, 0, 4, 18);
    private static final GuiModuleOffsets minorOffsets = new GuiModuleOffsets(-13, 0);

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ModularToolbeltItem instance;

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static MenuType<ToolbeltContainer> containerType;

    public ModularToolbeltItem() {
        super(new Properties()
                .stacksTo(1)
                .tab(TetraItemGroup.instance)
                .fireResistant());

        canHone = false;

        setRegistryName(unlocalizedName);

        majorModuleKeys = new String[]{slot1Key, slot2Key, slot3Key};
        minorModuleKeys = new String[]{beltKey};

        requiredModules = new String[]{beltKey};
    }

    @Override
    public void init(PacketHandler packetHandler) {
        packetHandler.registerPacket(EquipToolbeltItemPacket.class, EquipToolbeltItemPacket::new);
        packetHandler.registerPacket(OpenToolbeltItemPacket.class, OpenToolbeltItemPacket::new);
        packetHandler.registerPacket(UpdateBoosterPacket.class, UpdateBoosterPacket::new);
        packetHandler.registerPacket(ToggleSuspendPacket.class, ToggleSuspendPacket::new);
        MinecraftForge.EVENT_BUS.register(new TickHandlerBooster());

        RemoveSchematic.registerRemoveSchematics(this);

        DataManager.instance.synergyData.onReload(() -> synergies = DataManager.instance.getSynergyData("toolbelt/"));
    }

    @Override
    public void clientInit() {
        super.clientInit();
        MinecraftForge.EVENT_BUS.register(new JumpHandlerBooster(Minecraft.getInstance()));
        MinecraftForge.EVENT_BUS.register(new JumpHandlerSuspend(Minecraft.getInstance()));
        MinecraftForge.EVENT_BUS.register(new OverlayToolbelt(Minecraft.getInstance()));
        MinecraftForge.EVENT_BUS.register(new OverlayBooster(Minecraft.getInstance()));
        MenuScreens.register(ModularToolbeltItem.containerType, ToolbeltScreen::new);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (allowdedIn(group)) {
            items.add(createStack("belt/rope"));
            items.add(createStack("belt/inlaid"));
        }
    }

    private ItemStack createStack(String beltMaterial) {
        ItemStack itemStack = new ItemStack(this);
        IModularItem.putModuleInSlot(itemStack, beltKey, "toolbelt/belt", "toolbelt/belt_material", beltMaterial);
        IModularItem.putModuleInSlot(itemStack, slot1Key, "toolbelt/strap_slot1", "toolbelt/strap_slot1_material", "strap1/leather");
        return itemStack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            NetworkHooks.openGui((ServerPlayer) player, this);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent(getRegistryName().getPath());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        ItemStack itemStack = player.getMainHandItem();
        if (!this.equals(itemStack.getItem())) {
            itemStack = player.getOffhandItem();
        }

        if (!this.equals(itemStack.getItem())) {
            itemStack = ToolbeltHelper.findToolbelt(player);
        }

        return new ToolbeltContainer(windowId, inventory, itemStack, player);
    }

    public int getNumSlots(ItemStack itemStack, SlotType slotType) {
        return getAllModules(itemStack).stream()
                .map(module -> module.getEffectLevel(itemStack, slotType.effect))
                .reduce(0, Integer::sum);
    }

    public List<Collection<ItemEffect>> getSlotEffects(ItemStack itemStack, SlotType slotType) {
        return getAllModules(itemStack).stream()
                .map(module -> module.getEffectData(itemStack))
                .filter(Objects::nonNull)
                .filter(effects -> effects.contains(slotType.effect))
                .map(effects -> {
                    Map<ItemEffect, Integer> effectLevels = effects.getLevelMap();

                    int slotCount = effectLevels.get(slotType.effect);
                    Collection<Collection<ItemEffect>> result = new ArrayList<>(slotCount);
                    for (int i = 0; i < slotCount; i++) {
                        int index = i;
                        result.add(effectLevels.entrySet().stream()
                                .filter(entry -> !entry.getKey().equals(slotType.effect))
                                .filter(entry -> entry.getValue() > index)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList()));
                    }

                    return result;
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMajorGuiOffsets() {
        return majorOffsets;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMinorGuiOffsets() {
        return minorOffsets;
    }
}
