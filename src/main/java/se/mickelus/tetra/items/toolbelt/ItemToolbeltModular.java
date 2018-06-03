package se.mickelus.tetra.items.toolbelt;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.toolbelt.booster.BoosterModule;
import se.mickelus.tetra.items.toolbelt.booster.JumpHandlerBooster;
import se.mickelus.tetra.items.toolbelt.booster.TickHandlerBooster;
import se.mickelus.tetra.items.toolbelt.booster.UpdateBoosterPacket;
import se.mickelus.tetra.items.toolbelt.module.*;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.schema.ModuleSlotSchema;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketHandler;


public class ItemToolbeltModular extends ItemModular {
    public static ItemToolbeltModular instance;
    private final static String unlocalizedName = "toolbelt_modular";

    public final static String slot1Key = "toolbelt/slot1";
    public final static String slot2Key = "toolbelt/slot2";
    public final static String slot3Key = "toolbelt/slot3";
    public final static String beltKey = "toolbelt/belt";

    public final static String slot1Suffix = "_slot1";
    public final static String slot2Suffix = "_slot2";
    public final static String slot3Suffix = "_slot3";

    ItemModule defaultStrap;

    public ItemToolbeltModular() {
        super();

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);

        setMaxStackSize(1);

        setCreativeTab(TetraCreativeTabs.getInstance());


        majorModuleNames = new String[]{"", "", ""};
        majorModuleKeys = new String[]{slot1Key, slot2Key, slot3Key};
        minorModuleNames = new String[]{"Belt"};
        minorModuleKeys = new String[]{beltKey};

        instance = this;

        new BeltModule(beltKey);

        defaultStrap = new QuickAccessModule(slot1Key, "toolbelt/strap", slot1Suffix);
        new QuickAccessModule(slot2Key, "toolbelt/strap", slot2Suffix);
        new QuickAccessModule(slot3Key, "toolbelt/strap", slot3Suffix);

        new PotionStorageModule(slot1Key, "toolbelt/potionStorage", slot1Suffix);
        new PotionStorageModule(slot2Key, "toolbelt/potionStorage", slot2Suffix);
        new PotionStorageModule(slot3Key, "toolbelt/potionStorage", slot3Suffix);

        new StorageModule(slot1Key, "toolbelt/storage", slot1Suffix);
        new StorageModule(slot2Key, "toolbelt/storage", slot2Suffix);
        new StorageModule(slot3Key, "toolbelt/storage", slot3Suffix);

        new QuiverModule(slot1Key, "toolbelt/quiver", slot1Suffix);
        new QuiverModule(slot2Key, "toolbelt/quiver", slot2Suffix);
        new QuiverModule(slot3Key, "toolbelt/quiver", slot3Suffix);

        new BoosterModule(slot1Key, "toolbelt/booster", slot1Suffix);
        new BoosterModule(slot2Key, "toolbelt/booster", slot2Suffix);
        new BoosterModule(slot3Key, "toolbelt/booster", slot3Suffix);
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


        registerConfigSchema("toolbelt/belt");
        registerConfigSchema("toolbelt/strap");
        registerConfigSchema("toolbelt/booster");
        registerConfigSchema("toolbelt/potion_storage");
        registerConfigSchema("toolbelt/storage");
        registerConfigSchema("toolbelt/quiver");
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
        BeltModule.instance.addModule(itemStack, new ItemStack[]{new ItemStack(Items.LEAD)}, false, null);
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
                .filter(module -> module instanceof QuickAccessModule)
                .map(module -> module.getSize(itemStack))
                .reduce(0, Integer::sum);
    }

    public int getNumStorageSlots(ItemStack itemStack) {
        return getAllModules(itemStack).stream()
                .filter(module -> module instanceof StorageModule)
                .map(module -> module.getSize(itemStack))
                .reduce(0, Integer::sum);
    }

    public int getNumPotionSlots(ItemStack itemStack) {
        return getAllModules(itemStack).stream()
                .filter(module -> module instanceof PotionStorageModule)
                .map(module -> module.getSize(itemStack))
                .reduce(0, Integer::sum);
    }

    public int getNumQuiverSlots(ItemStack itemStack) {
        return getAllModules(itemStack).stream()
                .filter(module -> module instanceof QuiverModule)
                .map(module -> module.getSize(itemStack))
                .reduce(0, Integer::sum);
    }
}
