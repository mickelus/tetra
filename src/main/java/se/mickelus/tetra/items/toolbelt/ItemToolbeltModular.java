package se.mickelus.tetra.items.toolbelt;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.IntegrationHelper;
import se.mickelus.tetra.items.BasicModule;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.toolbelt.booster.JumpHandlerBooster;
import se.mickelus.tetra.items.toolbelt.booster.TickHandlerBooster;
import se.mickelus.tetra.items.toolbelt.booster.UpdateBoosterPacket;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.RemoveSchema;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketHandler;


@Optional.Interface(modid = IntegrationHelper.baublesModId, iface = IntegrationHelper.baublesApiClass)
public class ItemToolbeltModular extends ItemModular implements IBauble {
    public static ItemToolbeltModular instance;
    private final static String unlocalizedName = "toolbelt_modular";

    public final static String slot1Key = "toolbelt/slot1";
    public final static String slot2Key = "toolbelt/slot2";
    public final static String slot3Key = "toolbelt/slot3";
    public final static String beltKey = "toolbelt/belt";

    public final static String slot1Suffix = "_slot1";
    public final static String slot2Suffix = "_slot2";
    public final static String slot3Suffix = "_slot3";

    ItemModule defaultBelt;
    ItemModule defaultStrap;

    public ItemToolbeltModular() {
        super();

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);

        setMaxStackSize(1);

        setCreativeTab(TetraCreativeTabs.getInstance());

        majorModuleKeys = new String[]{slot1Key, slot2Key, slot3Key};
        minorModuleKeys = new String[]{beltKey};

        requiredModules = new String[]{beltKey};

        instance = this;

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
        MinecraftForge.EVENT_BUS.register(new JumpHandlerBooster(Minecraft.getMinecraft()));
    }

    @Override
    public void init(PacketHandler packetHandler) {
        GuiHandlerRegistry.instance.registerHandler(GuiHandlerToolbelt.GUI_TOOLBELT_ID, new GuiHandlerToolbelt());

        packetHandler.registerPacket(EquipToolbeltItemPacket.class, Side.SERVER);
        packetHandler.registerPacket(UpdateBoosterPacket.class, Side.SERVER);
        MinecraftForge.EVENT_BUS.register(new TickHandlerBooster());


        ItemUpgradeRegistry.instance.registerConfigSchema("toolbelt/belt");
        ItemUpgradeRegistry.instance.registerConfigSchema("toolbelt/strap");
        ItemUpgradeRegistry.instance.registerConfigSchema("toolbelt/booster");
        ItemUpgradeRegistry.instance.registerConfigSchema("toolbelt/potion_storage");
        ItemUpgradeRegistry.instance.registerConfigSchema("toolbelt/storage");
        ItemUpgradeRegistry.instance.registerConfigSchema("toolbelt/quiver");

        RemoveSchema.registerRemoveSchemas(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
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
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        player.openGui(TetraMod.instance, GuiHandlerToolbelt.GUI_TOOLBELT_ID, world, hand.ordinal(), 0, 0);

        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    public int getNumQuickslots(ItemStack itemStack) {
        return getAllModules(itemStack).stream()
                .map(module -> module.getEffectLevel(itemStack, ItemEffect.quickSlot))
                .reduce(0, Integer::sum);
    }

    public int getNumStorageSlots(ItemStack itemStack) {
        return getAllModules(itemStack).stream()
                .map(module -> module.getEffectLevel(itemStack, ItemEffect.storageSlot))
                .reduce(0, Integer::sum);
    }

    public int getNumPotionSlots(ItemStack itemStack) {
        return getAllModules(itemStack).stream()
                .map(module -> module.getEffectLevel(itemStack, ItemEffect.potionSlot))
                .reduce(0, Integer::sum);
    }

    public int getNumQuiverSlots(ItemStack itemStack) {
        return getAllModules(itemStack).stream()
                .map(module -> module.getEffectLevel(itemStack, ItemEffect.quiverSlot))
                .reduce(0, Integer::sum);
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
