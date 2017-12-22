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
import se.mickelus.tetra.module.BasicSchema;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketPipeline;

public class ItemToolbeltModular extends ItemModular {
    public static ItemToolbeltModular instance;
    private final static String unlocalizedName = "toolbelt_modular";

    public final static String slotKey = "toolbelt:slot1";
    public final static String beltKey = "toolbelt:belt";

    private StrapModule[] strapModules;

    public ItemToolbeltModular() {
        super();

        maxStackSize = 1;

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());


        majorModuleNames = new String[]{""};
        majorModuleKeys = new String[]{slotKey};
        minorModuleNames = new String[]{"Belt"};
        minorModuleKeys = new String[]{beltKey};

        instance = this;

        new BeltModule(beltKey);

        strapModules = new StrapModule[4];
        for (int i = 0; i < strapModules.length; i++) {
            strapModules[i] = new StrapModule(slotKey, "toolbelt/strap_" + (i+1));
        }
    }

    @Override
    public void init(PacketPipeline packetPipeline) {
        GuiHandlerRegistry.instance.registerHandler(GuiHandlerToolbelt.GUI_TOOLBELT_ID, new GuiHandlerToolbelt());

        packetPipeline.registerPacket(EquipToolbeltItemPacket.class);
        MinecraftForge.EVENT_BUS.register(new TickHandlerToolbelt());

        BeltModule.instance.registerUpgradeSchemas();

        for (int i = 0; i < strapModules.length; i++) {
            new BasicSchema("strap_schema_" + (i+1), strapModules[i], this);
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
        BeltModule.instance.addModule(itemStack, new ItemStack[]{new ItemStack(Items.LEAD)}, false);
        strapModules[0].addModule(itemStack, new ItemStack[]{new ItemStack(Items.LEATHER)}, false);
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
