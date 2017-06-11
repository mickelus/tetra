package se.mickelus.tetra.items.rocketBoots;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.network.PacketPipeline;

import java.util.List;

public class ItemRocketBoots extends ItemArmor implements ITetraItem {

    private static ItemRocketBoots instance;
    private final String unlocalizedName = "rocket_boots";



    public ItemRocketBoots() {
        super(ArmorMaterial.LEATHER, 1, EntityEquipmentSlot.FEET);

        maxStackSize = 1;

        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        GameRegistry.register(this);
        setCreativeTab(TetraCreativeTabs.getInstance());

        instance = this;
    }

    public static ItemRocketBoots getInstance() {
        return instance;
    }

    @Override
    public void clientPreInit() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
//        for (int i = 0; i < 14; i++) {
//            ItemStack stack = new ItemStack(itemIn, 1, i);
//            ItemArmor itemArmor = (ItemArmor)stack.getItem();
//            subItems.add(stack);
//            if (i != 0) itemArmor.setColor(stack, dyeints[i]);
//            ItemRenderRegister.reg(itemArmor, i);
//
//        }
//    }

    @Override
    public void init(PacketPipeline packetPipeline) {
        packetPipeline.registerPacket(UpdateBoostPacket.class);
        MinecraftForge.EVENT_BUS.register(new JumpHandlerRocketBoots(Minecraft.getMinecraft()));
        MinecraftForge.EVENT_BUS.register(new TickHandlerRocketBoots());
    }

    @Override
    public int getColor(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();

        if (nbt != null) {
            NBTTagCompound nbtDisplay = nbt.getCompoundTag("display");

            if (nbtDisplay.hasKey("color", 3)) {
                return nbtDisplay.getInteger("color");
            }
        }

        return 10511680;
    }

    @Override
    public void setColor(ItemStack stack, int color) {
        NBTTagCompound nbttagcompound = stack.getTagCompound();

        if (nbttagcompound == null) {
            nbttagcompound = new NBTTagCompound();
            stack.setTagCompound(nbttagcompound);
        }

        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");

        if (!nbttagcompound.hasKey("display", 10)) {
            nbttagcompound.setTag("display", nbttagcompound1);
        }

        nbttagcompound1.setInteger("color", color);
    }
}
