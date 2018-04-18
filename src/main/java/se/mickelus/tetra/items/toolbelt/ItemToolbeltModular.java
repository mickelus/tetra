package se.mickelus.tetra.items.toolbelt;

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
import se.mickelus.tetra.items.toolbelt.module.BeltModule;
import se.mickelus.tetra.items.toolbelt.module.StrapModule;
import se.mickelus.tetra.module.schema.ModuleSlotSchema;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketPipeline;

public class ItemToolbeltModular extends ItemModular {
    public static ItemToolbeltModular instance;
    private final static String unlocalizedName = "toolbelt_modular";

    public final static String slot1Key = "toolbelt/slot1";
    public final static String slot2Key = "toolbelt/slot2";
    public final static String beltKey = "toolbelt/belt";

    public final static String slot1Suffix = "_slot1";
    public final static String slot2Suffix = "_slot2";

    private StrapModule[] slot1StrapModules;
    private StrapModule[] slot2StrapModules;

    public ItemToolbeltModular() {
        super();

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);

        setMaxStackSize(1);

        setCreativeTab(TetraCreativeTabs.getInstance());


        majorModuleNames = new String[]{"", ""};
        majorModuleKeys = new String[]{slot1Key, slot2Key};
        minorModuleNames = new String[]{"Belt"};
        minorModuleKeys = new String[]{beltKey};

        instance = this;

        new BeltModule(beltKey);

        slot1StrapModules = new StrapModule[4];
        slot2StrapModules = new StrapModule[4];
        for (int i = 0; i < slot1StrapModules.length; i++) {
            slot1StrapModules[i] = new StrapModule(slot1Key, "toolbelt/strap" + (i+1), slot1Suffix);
            slot2StrapModules[i] = new StrapModule(slot2Key, "toolbelt/strap" + (i+1), slot2Suffix);
        }
    }

    @Override
    public void init(PacketPipeline packetPipeline) {
        GuiHandlerRegistry.instance.registerHandler(GuiHandlerToolbelt.GUI_TOOLBELT_ID, new GuiHandlerToolbelt());

        packetPipeline.registerPacket(EquipToolbeltItemPacket.class);

        BeltModule.instance.registerUpgradeSchemas();

        for (int i = 0; i < slot1StrapModules.length; i++) {
            new ModuleSlotSchema("strap_schema" + (i+1), slot1StrapModules[i], this);
            new ModuleSlotSchema("strap_schema" + (i+1), slot2StrapModules[i], this);
        }
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


    public int getNumSlots(ItemStack itemStack) {
        return getAllModules(itemStack).stream()
            .map(module -> module.getSize(itemStack))
            .reduce(0, Integer::sum);
    }
}
