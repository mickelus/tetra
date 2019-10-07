package se.mickelus.tetra.items.toolbelt;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.IntegrationHelper;
import se.mickelus.tetra.items.BasicModule;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.toolbelt.booster.JumpHandlerBooster;
import se.mickelus.tetra.items.toolbelt.booster.TickHandlerBooster;
import se.mickelus.tetra.items.toolbelt.booster.UpdateBoosterPacket;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryToolbelt;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.RemoveSchema;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketHandler;

import java.util.*;
import java.util.stream.Collectors;


@Optional.Interface(modid = IntegrationHelper.baublesModId, iface = IntegrationHelper.baublesApiClass)
public class ItemToolbeltModular extends ItemModular implements IBauble {
    private final static String unlocalizedName = "toolbelt_modular";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemToolbeltModular instance;

    public final static String slot1Key = "toolbelt/slot1";
    public final static String slot2Key = "toolbelt/slot2";
    public final static String slot3Key = "toolbelt/slot3";
    public final static String beltKey = "toolbelt/belt";

    public final static String slot1Suffix = "_slot1";
    public final static String slot2Suffix = "_slot2";
    public final static String slot3Suffix = "_slot3";

    private ItemModule defaultBelt;
    private ItemModule defaultStrap;

    public ItemToolbeltModular() {
        super();

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);

        setMaxStackSize(1);

        setCreativeTab(TetraItemGroup.getInstance());

        majorModuleKeys = new String[] { slot1Key, slot2Key, slot3Key };
        minorModuleKeys = new String[] { beltKey };

        requiredModules = new String[] { beltKey };

        defaultBelt = new BasicModule(beltKey, beltKey);

        defaultStrap = new ToolbeltModule(slot1Key, "strap", slot1Suffix);
        new ToolbeltModule(slot2Key, "strap", slot2Suffix);
        new ToolbeltModule(slot3Key, "strap", slot3Suffix);

        new ToolbeltModule(slot1Key, "potion_storage", slot1Suffix);
        new ToolbeltModule(slot2Key, "potion_storage", slot2Suffix);
        new ToolbeltModule(slot3Key, "potion_storage", slot3Suffix);

        new ToolbeltModule(slot1Key, "storage", slot1Suffix);
        new ToolbeltModule(slot2Key, "storage", slot2Suffix);
        new ToolbeltModule(slot3Key, "storage", slot3Suffix);

        new ToolbeltModule(slot1Key, "quiver", slot1Suffix);
        new ToolbeltModule(slot2Key, "quiver", slot2Suffix);
        new ToolbeltModule(slot3Key, "quiver", slot3Suffix);

        new ToolbeltModule(slot1Key, "booster", slot1Suffix);
        new ToolbeltModule(slot2Key, "booster", slot2Suffix);
        new ToolbeltModule(slot3Key, "booster", slot3Suffix);
    }

    @Override
    public void clientPreInit() {
        super.clientPreInit();
        MinecraftForge.EVENT_BUS.register(new JumpHandlerBooster(Minecraft.getInstance()));
    }

    @Override
    public void init(PacketHandler packetHandler) {
        GuiHandlerRegistry.instance.registerHandler(GuiHandlerToolbelt.toolbeltId, new GuiHandlerToolbelt());

        packetHandler.registerPacket(EquipToolbeltItemPacket.class, Side.SERVER);
        packetHandler.registerPacket(UpdateBoosterPacket.class, Side.SERVER);
        MinecraftForge.EVENT_BUS.register(new TickHandlerBooster());

        InventoryToolbelt.initializePredicates();

        ItemUpgradeRegistry.instance.registerConfigSchema("toolbelt/belt");
        ItemUpgradeRegistry.instance.registerConfigSchema("toolbelt/strap");
        ItemUpgradeRegistry.instance.registerConfigSchema("toolbelt/strap_improvements");
        ItemUpgradeRegistry.instance.registerConfigSchema("toolbelt/booster");
        ItemUpgradeRegistry.instance.registerConfigSchema("toolbelt/potion_storage");
        ItemUpgradeRegistry.instance.registerConfigSchema("toolbelt/storage");
        ItemUpgradeRegistry.instance.registerConfigSchema("toolbelt/storage_improvements");
        ItemUpgradeRegistry.instance.registerConfigSchema("toolbelt/quiver");
        ItemUpgradeRegistry.instance.registerConfigSchema("toolbelt/quiver_improvements");

        RemoveSchema.registerRemoveSchemas(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getSubItems(CreativeTabs creativeTabs, NonNullList<ItemStack> itemList) {
        if (isInCreativeTab(creativeTabs)) {
            itemList.add(createDefaultStack());
        }
    }

    private ItemStack createDefaultStack() {
        ItemStack itemStack = new ItemStack(this);
        defaultBelt.addModule(itemStack, "belt/rope", null);
        defaultStrap.addModule(itemStack, "strap1/leather", null);
        return itemStack;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        player.openGui(TetraMod.instance, GuiHandlerToolbelt.toolbeltId, world, hand.ordinal(), 0, 0);

        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
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

    /**
     * Tells baubles which slot this item can go into. Implements a method in the IBauble interface.
     * @param itemstack The itemstack
     * @return
     */
    @Optional.Method(modid = IntegrationHelper.baublesModId)
    public BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.BELT;
    }
}
