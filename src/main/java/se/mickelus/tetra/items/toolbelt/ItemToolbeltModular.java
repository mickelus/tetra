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
import se.mickelus.tetra.module.schema.ModuleSlotSchema;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketPipeline;


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

    private QuickAccessModule[] slot1StrapModules;
    private QuickAccessModule[] slot2StrapModules;
    private QuickAccessModule[] slot3StrapModules;

    private PotionStorageModule slot1PotionStorageModule;
    private PotionStorageModule slot2PotionStorageModule;
    private PotionStorageModule slot3PotionStorageModule;

    private StorageModule slot1StorageModule;
    private StorageModule slot2StorageModule;
    private StorageModule slot3StorageModule;

    private QuiverModule slot1QuiverModule;
    private QuiverModule slot2QuiverModule;
    private QuiverModule slot3QuiverModule;

    private BoosterModule slot1BoosterModule;
    private BoosterModule slot2BoosterModule;
    private BoosterModule slot3BoosterModule;

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

        slot1StrapModules = new QuickAccessModule[4];
        slot2StrapModules = new QuickAccessModule[4];
        slot3StrapModules = new QuickAccessModule[4];
        for (int i = 0; i < slot1StrapModules.length; i++) {
            slot1StrapModules[i] = new QuickAccessModule(slot1Key, "toolbelt/strap" + (i+1), slot1Suffix);
            slot2StrapModules[i] = new QuickAccessModule(slot2Key, "toolbelt/strap" + (i+1), slot2Suffix);
            slot3StrapModules[i] = new QuickAccessModule(slot3Key, "toolbelt/strap" + (i+1), slot3Suffix);
        }

        slot1PotionStorageModule = new PotionStorageModule(slot1Key, "toolbelt/potionStorage", slot1Suffix);
        slot2PotionStorageModule = new PotionStorageModule(slot2Key, "toolbelt/potionStorage", slot2Suffix);
        slot3PotionStorageModule = new PotionStorageModule(slot3Key, "toolbelt/potionStorage", slot3Suffix);

        slot1StorageModule = new StorageModule(slot1Key, "toolbelt/storage", slot1Suffix);
        slot2StorageModule = new StorageModule(slot2Key, "toolbelt/storage", slot2Suffix);
        slot3StorageModule = new StorageModule(slot3Key, "toolbelt/storage", slot3Suffix);

        slot1QuiverModule = new QuiverModule(slot1Key, "toolbelt/quiver", slot1Suffix);
        slot2QuiverModule = new QuiverModule(slot2Key, "toolbelt/quiver", slot2Suffix);
        slot3QuiverModule = new QuiverModule(slot3Key, "toolbelt/quiver", slot3Suffix);

        slot1BoosterModule = new BoosterModule(slot1Key, "toolbelt/booster", slot1Suffix);
        slot2BoosterModule = new BoosterModule(slot2Key, "toolbelt/booster", slot2Suffix);
        slot3BoosterModule = new BoosterModule(slot3Key, "toolbelt/booster", slot3Suffix);
    }

    @Override
    public void clientPreInit() {
        super.clientPreInit();
        MinecraftForge.EVENT_BUS.register(new JumpHandlerBooster(Minecraft.getMinecraft()));
    }

    @Override
    public void init(PacketPipeline packetPipeline) {
        GuiHandlerRegistry.instance.registerHandler(GuiHandlerToolbelt.GUI_TOOLBELT_ID, new GuiHandlerToolbelt());

        packetPipeline.registerPacket(EquipToolbeltItemPacket.class);
        packetPipeline.registerPacket(UpdateBoosterPacket.class);
        MinecraftForge.EVENT_BUS.register(new TickHandlerBooster());

        BeltModule.instance.registerUpgradeSchemas();

        for (int i = 0; i < slot1StrapModules.length; i++) {
            new ModuleSlotSchema("strap_schema" + (i+1), slot1StrapModules[i], this);
            new ModuleSlotSchema("strap_schema" + (i+1), slot2StrapModules[i], this);
            new ModuleSlotSchema("strap_schema" + (i+1), slot3StrapModules[i], this);
        }

        new ModuleSlotSchema("potion_storage_schema", slot1PotionStorageModule, this);
        new ModuleSlotSchema("potion_storage_schema", slot2PotionStorageModule, this);
        new ModuleSlotSchema("potion_storage_schema", slot3PotionStorageModule, this);

        new ModuleSlotSchema("storage_schema", slot1StorageModule, this);
        new ModuleSlotSchema("storage_schema", slot2StorageModule, this);
        new ModuleSlotSchema("storage_schema", slot3StorageModule, this);

        new ModuleSlotSchema("quiver_schema", slot1QuiverModule, this);
        new ModuleSlotSchema("quiver_schema", slot2QuiverModule, this);
        new ModuleSlotSchema("quiver_schema", slot3QuiverModule, this);

        new ModuleSlotSchema("booster_schema", slot1BoosterModule, this);
        new ModuleSlotSchema("booster_schema", slot2BoosterModule, this);
        new ModuleSlotSchema("booster_schema", slot3BoosterModule, this);
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
        slot1StrapModules[0].addModule(itemStack, new ItemStack[]{new ItemStack(Items.LEATHER)}, false, null);
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
