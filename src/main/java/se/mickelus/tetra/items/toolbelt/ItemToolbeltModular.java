package se.mickelus.tetra.items.toolbelt;

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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.BasicModule;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.toolbelt.booster.JumpHandlerBooster;
import se.mickelus.tetra.items.toolbelt.booster.OverlayBooster;
import se.mickelus.tetra.items.toolbelt.booster.TickHandlerBooster;
import se.mickelus.tetra.items.toolbelt.booster.UpdateBoosterPacket;
import se.mickelus.tetra.items.toolbelt.gui.ToolbeltGui;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryToolbelt;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.schema.RemoveSchema;
import se.mickelus.tetra.network.PacketHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ItemToolbeltModular extends ItemModular implements INamedContainerProvider {
    public final static String unlocalizedName = "toolbelt_modular";

    public final static String slot1Key = "toolbelt/slot1";
    public final static String slot2Key = "toolbelt/slot2";
    public final static String slot3Key = "toolbelt/slot3";
    public final static String beltKey = "toolbelt/belt";

    public final static String slot1Suffix = "_slot1";
    public final static String slot2Suffix = "_slot2";
    public final static String slot3Suffix = "_slot3";

    private ItemModule defaultBelt;
    private ItemModule defaultStrap;

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemToolbeltModular instance;

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ContainerType<ToolbeltContainer> containerType;

    public ItemToolbeltModular() {
        super(new Properties().maxStackSize(1).group(TetraItemGroup.instance));

        setRegistryName(unlocalizedName);

        majorModuleKeys = new String[] { slot1Key, slot2Key, slot3Key };
        minorModuleKeys = new String[] { beltKey };

        requiredModules = new String[] { beltKey };

        defaultBelt = new BasicModule(beltKey, beltKey);

        defaultStrap = new ToolbeltModule(slot1Key, "toolbelt/strap", slot1Suffix, "toolbelt/strap");
        new ToolbeltModule(slot2Key, "toolbelt/strap", slot2Suffix, "toolbelt/strap");
        new ToolbeltModule(slot3Key, "toolbelt/strap", slot3Suffix, "toolbelt/strap");

        new ToolbeltModule(slot1Key, "toolbelt/potion_storage", slot1Suffix, "toolbelt/potion_storage");
        new ToolbeltModule(slot2Key, "toolbelt/potion_storage", slot2Suffix, "toolbelt/potion_storage");
        new ToolbeltModule(slot3Key, "toolbelt/potion_storage", slot3Suffix, "toolbelt/potion_storage");

        new ToolbeltModule(slot1Key, "toolbelt/storage", slot1Suffix, "toolbelt/storage");
        new ToolbeltModule(slot2Key, "toolbelt/storage", slot2Suffix, "toolbelt/storage");
        new ToolbeltModule(slot3Key, "toolbelt/storage", slot3Suffix, "toolbelt/storage");

        new ToolbeltModule(slot1Key, "toolbelt/quiver", slot1Suffix, "toolbelt/quiver");
        new ToolbeltModule(slot2Key, "toolbelt/quiver", slot2Suffix, "toolbelt/quiver");
        new ToolbeltModule(slot3Key, "toolbelt/quiver", slot3Suffix, "toolbelt/quiver");

        new ToolbeltModule(slot1Key, "toolbelt/booster", slot1Suffix, "toolbelt/booster");
        new ToolbeltModule(slot2Key, "toolbelt/booster", slot2Suffix, "toolbelt/booster");
        new ToolbeltModule(slot3Key, "toolbelt/booster", slot3Suffix, "toolbelt/booster");
    }

    @Override
    public void init(PacketHandler packetHandler) {
        packetHandler.registerPacket(EquipToolbeltItemPacket.class, EquipToolbeltItemPacket::new);
        packetHandler.registerPacket(UpdateBoosterPacket.class, UpdateBoosterPacket::new);
        MinecraftForge.EVENT_BUS.register(new TickHandlerBooster());


        InventoryToolbelt.initializePredicates();

        RemoveSchema.registerRemoveSchemas(this);
    }

    @Override
    public void clientInit() {
        super.clientInit();
        MinecraftForge.EVENT_BUS.register(new JumpHandlerBooster(Minecraft.getInstance()));
        MinecraftForge.EVENT_BUS.register(new OverlayToolbelt(Minecraft.getInstance()));
        MinecraftForge.EVENT_BUS.register(new OverlayBooster(Minecraft.getInstance()));
        ScreenManager.registerFactory(ItemToolbeltModular.containerType, ToolbeltGui::new);
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
        defaultBelt.addModule(itemStack, beltMaterial, null);
        defaultStrap.addModule(itemStack, "strap1/leather", null);
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
                .filter(module -> module.getEffects(itemStack).contains(slotType.effect))
                .map(module -> {
                    EnumMap<ItemEffect, Integer> effectLevelMap = new EnumMap<>(ItemEffect.class);
                    ((Collection<ItemEffect>) module.getEffects(itemStack)).stream()
                            .filter(itemEffect -> !itemEffect.equals(slotType.effect))
                            .forEach(itemEffect -> effectLevelMap.put(itemEffect, module.getEffectLevel(itemStack, itemEffect)));


                    int slotCount = module.getEffectLevel(itemStack, slotType.effect);
                    Collection<Collection<ItemEffect>> result = new ArrayList<>(slotCount);
                    for (int i = 0; i < slotCount; i++) {
                        ArrayList<ItemEffect> slotEffects = new ArrayList<>();
                        for (Map.Entry<ItemEffect, Integer> entry: effectLevelMap.entrySet()) {
                            if (entry.getValue() > i) {
                                slotEffects.add(entry.getKey());
                            }
                        }
                        result.add(slotEffects);
                    }

                    return result;
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
