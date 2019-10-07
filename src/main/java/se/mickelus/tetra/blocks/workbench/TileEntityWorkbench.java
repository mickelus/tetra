package se.mickelus.tetra.blocks.workbench;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.ArrayUtils;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.blocks.workbench.action.RepairAction;
import se.mickelus.tetra.blocks.workbench.action.WorkbenchAction;
import se.mickelus.tetra.blocks.workbench.action.WorkbenchActionPacket;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.UpgradeSchema;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TileEntityWorkbench extends TileEntity implements IInventory {

    @ObjectHolder("tetra:workbench")
    public static TileEntityType<TileEntityWorkbench> type;

    private static final String STACKS_KEY = "stacks";
    private static final String SLOT_KEY = "slot";
    private static final String CURRENT_SLOT_KEY = "current_slot";
    private static final String SCHEMA_KEY = "schema";

    public static final int MATERIAL_SLOT_COUNT = 4;

    private NonNullList<ItemStack> stacks;

    private ItemStack previousTarget = ItemStack.EMPTY;
    private UpgradeSchema currentSchema;
    private String currentSlot;

    private Map<String, Runnable> changeListeners;

    private static WorkbenchAction[] actions = new WorkbenchAction[] {
            new RepairAction()
    };


    public TileEntityWorkbench() {
        super(type);
        stacks = NonNullList.withSize(MATERIAL_SLOT_COUNT, ItemStack.EMPTY);
        changeListeners = new HashMap<>();
    }

    public static void initConfigActions(WorkbenchAction[] actions) {
        TileEntityWorkbench.actions = ArrayUtils.addAll(TileEntityWorkbench.actions, actions);
    }

    public WorkbenchAction[] getAvailableActions(PlayerEntity player) {
        ItemStack itemStack = getTargetItemStack();
        return Arrays.stream(actions)
                .filter(action -> action.canPerformOn(player, itemStack))
                .toArray(WorkbenchAction[]::new);
    }

    public void performAction(PlayerEntity player, String actionKey) {
        if (world.isRemote) {
            PacketHandler.sendToServer(new WorkbenchActionPacket(pos, actionKey));
            return;
        }

        BlockState blockState = world.getBlockState(getPos());
        ItemStack targetStack = getTargetItemStack();

        Arrays.stream(actions)
                .filter(action -> action.getKey().equals(actionKey))
                .findFirst()
                .filter(action -> action.canPerformOn(player, targetStack))
                .filter(action -> checkActionCapabilities(player, action, targetStack))
                .ifPresent(action -> {
                    for (Capability capability : action.getRequiredCapabilitiesFor(targetStack)) {
                        int requiredLevel = action.getCapabilityLevel(targetStack, capability);
                        ItemStack providingStack = CapabilityHelper.getProvidingItemStack(capability, requiredLevel, player);
                        if (!providingStack.isEmpty()) {
                            if (providingStack.getItem() instanceof ItemModular) {
                                ((ItemModular) providingStack.getItem()).onActionConsumeCapability(providingStack,
                                        targetStack, player, capability, requiredLevel,true);
                            }
                        } else {
                            CastOptional.cast(getBlockState().getBlock(), BlockWorkbench.class)
                                    .ifPresent(block -> block.onActionConsumeCapability(world, getPos(), blockState, targetStack,
                                            player, true));
                        }
                    }

                    action.perform(player, targetStack, this);
                });
    }

    private boolean checkActionCapabilities(PlayerEntity player, WorkbenchAction action, ItemStack itemStack) {
        return Arrays.stream(action.getRequiredCapabilitiesFor(itemStack))
                .allMatch(capability ->
                        CapabilityHelper.getCombinedCapabilityLevel(player, getWorld(), getPos(), world.getBlockState(getPos()), capability)
                        >= action.getCapabilityLevel(itemStack, capability));
    }

    public UpgradeSchema getCurrentSchema() {
        return currentSchema;
    }

    public void setCurrentSchema(UpgradeSchema schema, String currentSlot) {

        this.currentSchema = schema;
        this.currentSlot = currentSlot;

        changeListeners.values().forEach(Runnable::run);
        sync();
    }

    public void clearSchema() {
        setCurrentSchema(null, null);
    }

    /**
     * Intended for updating the TE when receiving update packets on the server.
     * @param currentSchema A schema, or null if it should be unset
     * @param currentSlot A slot key, or null if it should be unset
     * @param player
     */
    public void update(UpgradeSchema currentSchema, String currentSlot, PlayerEntity player) {
        // todo: inventory change hack, better solution?
        if (currentSchema == null && player != null) {
            emptyMaterialSlots(player);
        }

        this.currentSchema = currentSchema;
        this.currentSlot = currentSlot;

        sync();
    }
    public String getCurrentSlot() {
        return currentSlot;
    }

    private void sync() {
        if (world.isRemote) {
            PacketHandler.sendToServer(new UpdateWorkbenchPacket(pos, currentSchema, currentSlot));
        } else {
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
            markDirty();
        }
    }

    public ItemStack getTargetItemStack() {
        ItemStack stack = getStackInSlot(0);

        if (stack == null) {
            return ItemStack.EMPTY;
        }

        ItemStack placeholder = ItemUpgradeRegistry.instance.getReplacement(stack);
        if (!placeholder.isEmpty()) {
            return placeholder;
        }

        return stack;
    }

    public ItemStack[] getMaterials() {
        return stacks.subList(1, 4).toArray(new ItemStack[3]);
    }

    public void initiateCrafting(PlayerEntity player) {
        if (world.isRemote) {
            PacketHandler.sendToServer(new CraftWorkbenchPacket(pos));
        }

        craft(player);

        sync();
    }

    public void craft(PlayerEntity player) {
        ItemStack targetStack = getTargetItemStack();
        ItemStack upgradedStack = ItemStack.EMPTY;

        BlockState blockState = world.getBlockState(getPos());

        int[] availableCapabilities = CapabilityHelper.getCombinedCapabilityLevels(player, getWorld(), getPos(), blockState);

        // used when calculating crafting effects (from tools etc) as the upgrade may have consumed all materials
        ItemStack[] materialsCopy = Arrays.stream(getMaterials()).map(ItemStack::copy).toArray(ItemStack[]::new);

        if (currentSchema != null && currentSchema.canApplyUpgrade(player, targetStack, getMaterials(), currentSlot, availableCapabilities)) {
            upgradedStack = currentSchema.applyUpgrade(targetStack, getMaterials(), true, currentSlot, player);

            if (upgradedStack.getItem() instanceof ItemModular) {
                ((ItemModular) upgradedStack.getItem()).assemble(upgradedStack);
            }

            for (Capability capability : currentSchema.getRequiredCapabilities(targetStack, materialsCopy)) {
                int requiredLevel = currentSchema.getRequiredCapabilityLevel(targetStack, materialsCopy, capability);
                ItemStack providingStack = CapabilityHelper.getProvidingItemStack(capability, requiredLevel, player);
                if (!providingStack.isEmpty()) {
                    if (providingStack.getItem() instanceof ItemModular) {
                        upgradedStack = ((ItemModular) providingStack.getItem()).onCraftConsumeCapability(providingStack,
                                upgradedStack, player, capability, requiredLevel,true);
                    }
                } else {
                    ItemStack consumeTarget = upgradedStack;
                    CastOptional.cast(getBlockState().getBlock(), BlockWorkbench.class)
                            .ifPresent(block -> block.onActionConsumeCapability(world, getPos(), blockState, consumeTarget,
                                    player, true));
                }
            }

            int xpCost = currentSchema.getExperienceCost(targetStack, materialsCopy);
            if (xpCost > 0) {
                player.addExperienceLevel(-xpCost);
            }
        }

        emptyMaterialSlots(player);

        setInventorySlotContents(0, upgradedStack);
    }

    public void applyTweaks(PlayerEntity player, String slot, Map<String, Integer> tweaks) {
        if (world.isRemote) {
            PacketHandler.sendToServer(new TweakWorkbenchPacket(pos, slot, tweaks));
        }

        tweak(player, slot, tweaks);

        sync();
    }

    public void tweak(PlayerEntity player, String slot, Map<String, Integer> tweaks) {
        ItemStack tweakedStack = getTargetItemStack().copy();
        CastOptional.cast(tweakedStack.getItem(), ItemModular.class)
                .ifPresent(item -> item.tweak(tweakedStack, slot, tweaks));

        setInventorySlotContents(0, tweakedStack);
    }

    public void addChangeListener(String key, Runnable runnable) {
        changeListeners.put(key, runnable);
    }

    public void removeChangeListener(String key) {
        changeListeners.remove(key);
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.world.isRemote) {
            // TODO: this is called several times everytime a change occurs

            changeListeners.values().forEach(Runnable::run);
        }
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(pkt.getNbtCompound());
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        NBTHelper.readItemStacks(compound, stacks);

        String schemaKey = compound.getString(SCHEMA_KEY);
        currentSchema = ItemUpgradeRegistry.instance.getSchema(schemaKey);

        if (compound.contains(CURRENT_SLOT_KEY)) {
            currentSlot = compound.getString(CURRENT_SLOT_KEY);
        }

        // todo : due to the null check perhaps this is not the right place to do this
        if (this.world != null && this.world.isRemote) {
            changeListeners.values().forEach(Runnable::run);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        NBTHelper.writeItemStacks(stacks, compound);

        if (currentSchema != null) {
            compound.putString(SCHEMA_KEY, currentSchema.getKey());
        }

        if (currentSlot != null) {
            compound.putString(CURRENT_SLOT_KEY, currentSlot);
        }

        return compound;
    }

    /**
     * Empties all material slots into the given players inventory.
     * @param player
     */
    private void emptyMaterialSlots(PlayerEntity player) {
        for (int i = 1; i < stacks.size(); i++) {
            transferStackToPlayer(player, i);
        }
        markDirty();
    }

    /**
     * Empties all material slots into the world. Make sure to call on both sides.
     */
    private void emptyMaterialSlots() {
        if (!world.isRemote) {
            for (int i = 1; i < stacks.size(); i++) {
                ItemStack materialStack = removeStackFromSlot(i);
                if (!materialStack.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + 0.5, (double)pos.getY() + 1.1, (double)pos.getZ() + 0.5, materialStack);
                    itemEntity.setDefaultPickupDelay();
                    world.addEntity(itemEntity);
                }
            }
        } else {
            for (int i = 1; i < stacks.size(); i++) {
                removeStackFromSlot(i);
            }
        }
    }

    private void transferStackToPlayer(PlayerEntity player, int index) {
        ItemStack itemStack = getStackInSlot(index);
        if (!itemStack.isEmpty()) {
            if (!player.inventory.addItemStackToInventory(itemStack)) {
                player.dropItem(itemStack, false);
            }
        }
    }


    @Override
    public int getSizeInventory() {
        if (currentSchema != null) {
            return currentSchema.getNumMaterialSlots() + 1;
        }
        return 1;
    }


    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.stacks) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    @Override
    public ItemStack getStackInSlot(int index) {
        return this.stacks.get(index);
    }

    @Nullable
    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.stacks, index, count);

        if (!itemstack.isEmpty()) {
            markDirty();
        }

        return itemstack;
    }

    @Nonnull
    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack itemstack = ItemStackHelper.getAndRemove(this.stacks, index);

        if (!itemstack.isEmpty()) {
            markDirty();
        }

        return itemstack;
    }

    @Override
    public void setInventorySlotContents(int index, @Nullable ItemStack itemStack) {
        // todo: figure out something less hacky
        if (index == 0 && (itemStack.isEmpty() || !ItemStack.areItemStacksEqual(getTargetItemStack(), itemStack))) {

            currentSchema = null;
            currentSlot = null;

            emptyMaterialSlots();
        }

        this.stacks.set(index, itemStack);

        if (!itemStack.isEmpty() && itemStack.getCount() > this.getInventoryStackLimit()) {
            itemStack.setCount(this.getInventoryStackLimit());
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }

    @Override
    public void openInventory(PlayerEntity player) {

    }

    @Override
    public void closeInventory(PlayerEntity player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == 0) {
            return true;
        } else if (currentSchema != null) {
            return currentSchema.acceptsMaterial(getTargetItemStack(), index - 1, stack);
        }
        return false;
    }

    @Override
    public void clear() {
        this.stacks.clear();
    }
}
