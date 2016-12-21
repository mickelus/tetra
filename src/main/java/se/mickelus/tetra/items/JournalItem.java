package se.mickelus.tetra.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class JournalItem extends TetraItem {

    private static JournalItem instance;
    private static String unlocalizedName = "journal";

    public JournalItem() {
        super();
        
        maxStackSize = 1;

        setRegistryName(unlocalizedName);
        //GameRegistry.register(this);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());
        
        instance = this;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStack, World world, EntityPlayer player, EnumHand hand) {
        openBook(player, world);
        return super.onItemRightClick(itemStack, world, player, hand);
    }
    
    private void openBook(EntityPlayer player, World world) {
        if(world.isRemote) {
//            GuiToolbelt.getInstance().SetPlayer(player);
        }
    }
    
    public static JournalItem getInstance() {
        return instance;
    }
    
}
