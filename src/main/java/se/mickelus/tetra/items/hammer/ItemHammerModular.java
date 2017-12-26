package se.mickelus.tetra.items.hammer;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.blocks.workbench.BlockWorkbench;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.ICapabilityProvider;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.module.BasicSchema;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.RepairSchema;
import se.mickelus.tetra.network.PacketPipeline;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class ItemHammerModular extends ItemModularHandheld {

    public final static String headKey = "head";

    public final static String handleKey = "handle";
    public final static String bindingKey = "binding";
    public final static String accessoryKey = "accessory";

    private static final String unlocalizedName = "hammer_modular";

    public ItemHammerModular() {

        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        setMaxStackSize(1);

        majorModuleNames = new String[]{"Head", "Handle"};
        majorModuleKeys = new String[]{headKey, handleKey};
        minorModuleNames = new String[]{"Binding", "Accessory"};
        minorModuleKeys = new String[]{bindingKey, accessoryKey};

        new BasicHeadModule(headKey);
        new BasicHandleModule(handleKey);
    }


    @Override
    public void init(PacketPipeline packetPipeline) {

        new BasicSchema("basic_head_schema", BasicHeadModule.instance, this);
        new BasicSchema("basic_handle_schema", BasicHandleModule.instance, this);

        new RepairSchema(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs creativeTabs, NonNullList<ItemStack> itemList) {
        if (isInCreativeTab(creativeTabs)) {
            itemList.add(createDefaultStack(null));
        }
    }

    private ItemStack createDefaultStack(ItemStack originalStack) {
        ItemStack itemStack = new ItemStack(this);

        if (originalStack != null) {
            itemStack.setItemDamage(originalStack.getItemDamage());
        }
        BasicHeadModule.instance.addModule(itemStack, new ItemStack[]{new ItemStack(Blocks.LOG)}, false, null);
        BasicHandleModule.instance.addModule(itemStack, new ItemStack[]{new ItemStack(Items.STICK)}, false, null);
        return itemStack;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return BlockWorkbench.upgradeWorkbench(player, world, pos, hand, facing);
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable World playerIn, List<String> tooltip, ITooltipFlag advanced) {
        super.addInformation(itemStack, playerIn, tooltip, advanced);
    }
}


