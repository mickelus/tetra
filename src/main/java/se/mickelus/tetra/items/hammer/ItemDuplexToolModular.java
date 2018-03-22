package se.mickelus.tetra.items.hammer;

import com.google.common.collect.ImmutableList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.blocks.workbench.BlockWorkbench;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.module.SynergyData;
import se.mickelus.tetra.module.schema.BasicSchema;
import se.mickelus.tetra.module.schema.ModuleSlotSchema;
import se.mickelus.tetra.network.PacketPipeline;


public class ItemDuplexToolModular extends ItemModularHandheld {

    public final static String headLeftKey = "duplex/head_left";
    public final static String headRightKey = "duplex/head_right";

    public final static String handleKey = "duplex/handle";
    public final static String bindingKey = "duplex/binding";
    public final static String accessoryKey = "duplex/accessory";

    public final static String leftSuffix = "_left";
    public final static String rightSuffix = "_right";

    private static final String unlocalizedName = "duplex_tool_modular";

    public static DuplexHeadModule basicHammerHeadLeft;
    public static DuplexHeadModule basicHammerHeadRight;

    public ItemDuplexToolModular() {

        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        setMaxStackSize(1);
        setCreativeTab(TetraCreativeTabs.getInstance());

        majorModuleNames = new String[]{"Head §7(left)", "Head §7(right)"};
        majorModuleKeys = new String[]{headLeftKey,headRightKey, };
        minorModuleNames = new String[]{"Binding", "Handle", "Accessory"};
        minorModuleKeys = new String[]{bindingKey, handleKey, accessoryKey};

        synergies = DataHandler.instance.getModuleData("hammer/synergies", SynergyData[].class);

        basicHammerHeadLeft = new DuplexHeadModule(headLeftKey, "hammer/basic_head", leftSuffix);
        basicHammerHeadRight = new DuplexHeadModule(headRightKey, "hammer/basic_head", rightSuffix);
        new BasicHandleModule(handleKey);
    }


    @Override
    public void init(PacketPipeline packetPipeline) {

        new ModuleSlotSchema("basic_head_schema", basicHammerHeadLeft, this);
        new ModuleSlotSchema("basic_head_schema", basicHammerHeadRight, this);
        new BasicSchema("basic_handle_schema", BasicHandleModule.instance, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs creativeTabs, NonNullList<ItemStack> itemList) {
        if (isInCreativeTab(creativeTabs)) {
            itemList.add(createDefaultStack(new ItemStack(Blocks.LOG), new ItemStack(Items.STICK)));
            itemList.add(createDefaultStack(new ItemStack(Blocks.OBSIDIAN), new ItemStack(Items.IRON_INGOT)));
        }
    }

    private ItemStack createDefaultStack(ItemStack headMaterial, ItemStack handleMaterial) {
        ItemStack itemStack = new ItemStack(this);

        basicHammerHeadLeft.addModule(itemStack, new ItemStack[]{headMaterial}, false, null);
        basicHammerHeadRight.addModule(itemStack, new ItemStack[]{headMaterial}, false, null);
        BasicHandleModule.instance.addModule(itemStack, new ItemStack[]{handleMaterial}, false, null);
        return itemStack;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return BlockWorkbench.upgradeWorkbench(player, world, pos, hand, facing);
    }

    @Override
    public ImmutableList<ResourceLocation> getTextures(ItemStack itemStack) {
        return super.getTextures(itemStack);
    }
}


