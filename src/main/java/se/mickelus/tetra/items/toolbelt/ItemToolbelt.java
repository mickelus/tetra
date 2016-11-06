package se.mickelus.tetra.items.toolbelt;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import se.mickelus.tetra.TetraCreativeTabs;
import se.mickelus.tetra.TetraMod;

public class ItemToolbelt extends Item {
    public static ItemToolbelt instance;
    private final static String unlocalizedName = "toolbelt";

    public ItemToolbelt() {
        super();

        maxStackSize = 1;

        setRegistryName(unlocalizedName);
        setUnlocalizedName(TetraMod.MOD_ID + "." + unlocalizedName);
        GameRegistry.register(this);
        setCreativeTab(TetraCreativeTabs.getInstance());

        instance = this;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStack, World world, EntityPlayer player, EnumHand hand) {
        player.openGui(TetraMod.instance, GuiHandlerToolbelt.GUI_TOOLBELT_ID, world, hand.ordinal(), 0, 0);

        return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
    }
}
