package se.mickelus.tetra.items.rocketBoots;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.network.PacketPipeline;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRocketBoots extends ItemArmor implements ITetraItem {

    private static ItemRocketBoots instance;
    private final String unlocalizedName = "rocket_boots";



    public ItemRocketBoots() {
        super(ArmorMaterial.LEATHER, 1, EntityEquipmentSlot.FEET);

        maxStackSize = 1;

        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());

        instance = this;
    }

    public static ItemRocketBoots getInstance() {
        return instance;
    }

    @Override
    public void clientPreInit() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
        MinecraftForge.EVENT_BUS.register(new JumpHandlerRocketBoots(Minecraft.getMinecraft()));
    }

    @Override
    public void init(PacketPipeline packetPipeline) {
        packetPipeline.registerPacket(UpdateBoostPacket.class);
        MinecraftForge.EVENT_BUS.register(new TickHandlerRocketBoots());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("§8WIP");
    }
}
