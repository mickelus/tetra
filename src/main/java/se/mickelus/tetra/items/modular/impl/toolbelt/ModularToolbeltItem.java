package se.mickelus.tetra.items.modular.impl.toolbelt;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import java.util.function.Supplier;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.booster.JumpHandlerBooster;
import se.mickelus.tetra.items.modular.impl.toolbelt.booster.TickHandlerBooster;
import se.mickelus.tetra.items.modular.impl.toolbelt.booster.UpdateBoosterPacket;
import se.mickelus.tetra.items.modular.impl.toolbelt.suspend.JumpHandlerSuspend;
import se.mickelus.tetra.items.modular.impl.toolbelt.suspend.ToggleSuspendPacket;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class ModularToolbeltItem extends ModularItem implements MenuProvider {
    public final static String identifier = "modular_toolbelt";

    public final static String slot1Key = "toolbelt/slot1";
    public final static String slot2Key = "toolbelt/slot2";
    public final static String slot3Key = "toolbelt/slot3";
    public final static String beltKey = "toolbelt/belt";

    public final static String slot1Suffix = "_slot1";
    public final static String slot2Suffix = "_slot2";
    public final static String slot3Suffix = "_slot3";

    private static final GuiModuleOffsets majorOffsets = new GuiModuleOffsets(-14, 18, 4, 0, 4, 18);
    private static final GuiModuleOffsets minorOffsets = new GuiModuleOffsets(-13, 0);

    public static Supplier<ModularToolbeltItem> instance;

    public ModularToolbeltItem() {
        super(new Properties()
                .stacksTo(1)

                .fireResistant());

        canHone = false;

        majorModuleKeys = new String[] { slot1Key, slot2Key, slot3Key };
        minorModuleKeys = new String[] { beltKey };

        requiredModules = new String[] { beltKey };
    }

    @Override
    public void registerPackets(PacketHandler packetHandler) {
        packetHandler.registerServerBoundPacket(EquipToolbeltItemPacket.class, EquipToolbeltItemPacket::new);
        packetHandler.registerServerBoundPacket(StoreToolbeltItemPacket.class, StoreToolbeltItemPacket::new);
        packetHandler.registerServerBoundPacket(OpenToolbeltItemPacket.class, OpenToolbeltItemPacket::new);
        packetHandler.registerServerBoundPacket(UpdateBoosterPacket.class, UpdateBoosterPacket::new);
        packetHandler.registerServerBoundPacket(ToggleSuspendPacket.class, ToggleSuspendPacket::new);
    }

    @Override
    public void commonInit(PacketHandler packetHandler) {
        NeoForge.EVENT_BUS.register(new TickHandlerBooster());

        DataManager.instance.synergyData.onReload(() -> synergies = DataManager.instance.synergyData.getOrdered("toolbelt/"));
    }

    @Override
    public void clientInit() {
        super.clientInit();
        NeoForge.EVENT_BUS.register(new JumpHandlerBooster(Minecraft.getInstance()));
        NeoForge.EVENT_BUS.register(new JumpHandlerSuspend(Minecraft.getInstance()));
    }

    public static Collection<ItemStack> getCreativeTabItemStacks() {
        return Lists.newArrayList(
                createStack("belt/rope"),
                createStack("belt/inlaid")
        );
    }

    private static ItemStack createStack(String beltMaterial) {
        ItemStack itemStack = new ItemStack(instance.get());
        IModularItem.putModuleInSlot(itemStack, beltKey, "toolbelt/belt", "toolbelt/belt_material", beltMaterial);
        IModularItem.putModuleInSlot(itemStack, slot1Key, "toolbelt/strap_slot1", "toolbelt/strap_slot1_material", "strap1/leather");
        IModularItem.updateIdentifier(itemStack);
        return itemStack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            ((ServerPlayer) player).openMenu(this);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    public Component getDisplayName() {
        return Component.literal(toString());
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
    public GuiModuleOffsets getMajorGuiOffsets(ItemStack itemStack) {
        return majorOffsets;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMinorGuiOffsets(ItemStack itemStack) {
        return minorOffsets;
    }
}
