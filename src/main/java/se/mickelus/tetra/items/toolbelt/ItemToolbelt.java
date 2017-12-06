package se.mickelus.tetra.items.toolbelt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketPipeline;

public class ItemToolbelt extends TetraItem {
    public static ItemToolbelt instance;
    private final static String unlocalizedName = "toolbelt";

    public ItemToolbelt() {
        super();

        maxStackSize = 1;

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());

        instance = this;
    }

    @SubscribeEvent
    public void registerItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(this);
        System.out.println("BELTZ" + getRegistryName());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        player.openGui(TetraMod.instance, GuiHandlerToolbelt.GUI_TOOLBELT_ID, world, hand.ordinal(), 0, 0);

        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public void init(PacketPipeline packetPipeline) {
        GuiHandlerRegistry.instance.registerHandler(GuiHandlerToolbelt.GUI_TOOLBELT_ID, new GuiHandlerToolbelt());

        packetPipeline.registerPacket(EquipToolbeltItemPacket.class);
        MinecraftForge.EVENT_BUS.register(new TickHandlerToolbelt());
    }
}
