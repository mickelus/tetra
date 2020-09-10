package se.mickelus.tetra.items.modular.impl.toolbelt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.modular.impl.toolbelt.booster.JumpHandlerBooster;
import se.mickelus.tetra.items.modular.impl.toolbelt.booster.OverlayBooster;
import se.mickelus.tetra.items.modular.impl.toolbelt.booster.TickHandlerBooster;
import se.mickelus.tetra.items.modular.impl.toolbelt.booster.UpdateBoosterPacket;
import se.mickelus.tetra.items.modular.impl.toolbelt.gui.ToolbeltGui;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.ToolbeltInventory;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.module.data.TierData;
import se.mickelus.tetra.module.schematic.RemoveSchematic;
import se.mickelus.tetra.network.PacketHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModularToolbeltItem extends ModularItem implements INamedContainerProvider {
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
    public static ContainerType<ToolbeltContainer> containerType;

    public ModularToolbeltItem() {
        super(new Properties().maxStackSize(1).group(TetraItemGroup.instance));

        canHone = false;

        setRegistryName(unlocalizedName);

        majorModuleKeys = new String[] { slot1Key, slot2Key, slot3Key };
        minorModuleKeys = new String[] { beltKey };

        requiredModules = new String[] { beltKey };
    }

    @Override
    public void init(PacketHandler packetHandler) {
        packetHandler.registerPacket(EquipToolbeltItemPacket.class, EquipToolbeltItemPacket::new);
        packetHandler.registerPacket(UpdateBoosterPacket.class, UpdateBoosterPacket::new);
        MinecraftForge.EVENT_BUS.register(new TickHandlerBooster());


        ToolbeltInventory.initializePredicates();

        RemoveSchematic.registerRemoveSchematics(this);
    }

    @Override
    public void clientInit() {
        super.clientInit();
        MinecraftForge.EVENT_BUS.register(new JumpHandlerBooster(Minecraft.getInstance()));
        MinecraftForge.EVENT_BUS.register(new OverlayToolbelt(Minecraft.getInstance()));
        MinecraftForge.EVENT_BUS.register(new OverlayBooster(Minecraft.getInstance()));
        ScreenManager.registerFactory(ModularToolbeltItem.containerType, ToolbeltGui::new);
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (isInGroup(group)) {
            items.add(createStack("belt/rope"));
            items.add(createStack("belt/inlaid"));
        }
    }

    private ItemStack createStack(String beltMaterial) {
        ItemStack itemStack = new ItemStack(this);
        putModuleInSlot(itemStack, beltKey, "toolbelt/belt", "toolbelt/belt_material", beltMaterial);
        putModuleInSlot(itemStack, slot1Key, "toolbelt/strap_slot1", "toolbelt/strap_slot1_material", "strap1/leather");
        return itemStack;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (!world.isRemote) {
            NetworkHooks.openGui((ServerPlayerEntity) player, this);
        }

        return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(getRegistryName().getPath());
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
        ItemStack itemStack = player.getHeldItemMainhand();
        if (!this.equals(itemStack.getItem())) {
            itemStack = player.getHeldItemOffhand();
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
