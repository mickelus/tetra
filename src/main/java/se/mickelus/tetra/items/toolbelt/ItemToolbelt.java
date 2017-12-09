package se.mickelus.tetra.items.toolbelt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketPipeline;

public class ItemToolbelt extends ItemModular {
    public static ItemToolbelt instance;
    private final static String unlocalizedName = "toolbelt_modular";

    public final static String slotKey = "toolbelt:slot1";
    public final static String beltKey = "toolbelt:belt";

    public ItemToolbelt() {
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
