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

    private static final String activeKey = "active";
    private static final String chargedKey = "charged";
    private static final String fuelKey = "fuel";
    private static final String cooldownKey = "cooldown";

    private static final int fuelCapacity = 100;
    private static final int fuelCost = 3;
    private static final int fuelCostCharged = 20;
    private static final int fuelRecharge = 1;
    private static final int cooldownTicks = 80;

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
    public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound tag = stack.getTagCompound();
        boolean charged = tag.getBoolean(chargedKey);
        if (isActive(tag) && hasFuel(tag, charged)) {
            if (charged) {
                boostPlayerCharged(player, tag);
            } else {
                boostPlayer(player, tag);
            }

            consumeFuel(tag, charged);
        } else {
            rechargeFuel(tag);
        }

        if (charged) {
            tag.setBoolean(chargedKey, false);
        }
    }

    private static boolean hasFuel(NBTTagCompound tag, boolean charged) {
        if (charged) {
            return tag.getInteger(fuelKey) >= fuelCostCharged;
        }
        return tag.getInteger(fuelKey) >= fuelCost;
    }

    public static int getFuel(NBTTagCompound tag) {
        return tag.getInteger(fuelKey);
    }

    public static float getFuelPercent(NBTTagCompound tag) {
        return tag.getInteger(fuelKey) * 1F / fuelCapacity;
    }

    private static void boostPlayer(EntityPlayer player, NBTTagCompound tag) {
        player.addVelocity(0, 0.1, 0);

        if (player.motionY > -0.1) {
            player.fallDistance = 0;
        }
    }

    private static void boostPlayerCharged(EntityPlayer player, NBTTagCompound tag) {
        System.out.println("boosting");

        Vec3d lookVector = player.getLookVec();
        //player.addVelocity(0, 5, 0);
        player.addVelocity(lookVector.xCoord, Math.max(lookVector.yCoord * 0.06, 0.2), lookVector.zCoord);
    }

    private static void consumeFuel(NBTTagCompound tag, boolean charged) {
        if (charged) {
            tag.setInteger(fuelKey, tag.getInteger(fuelKey) - fuelCostCharged);
        } else {
            tag.setInteger(fuelKey, tag.getInteger(fuelKey) - fuelCost);
        }
        tag.setInteger(cooldownKey, cooldownTicks);
    }

    private static void rechargeFuel(NBTTagCompound tag) {
        int fuel = tag.getInteger(fuelKey);
        int cooldown = tag.getInteger(cooldownKey);
        if (cooldown > 0) {
            tag.setInteger(cooldownKey, cooldown - 1);
        } else if (fuel + fuelRecharge < fuelCapacity) {
            tag.setInteger(fuelKey, fuel + fuelRecharge);
        }
    }

    private static boolean isActive(NBTTagCompound tag) {
        return tag.getBoolean(activeKey);
    }

    public static void setActive(NBTTagCompound tag, boolean active, boolean charged) {
        tag.setBoolean(activeKey, active);
        if (charged) {
            tag.setBoolean(chargedKey, charged);
        }
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
