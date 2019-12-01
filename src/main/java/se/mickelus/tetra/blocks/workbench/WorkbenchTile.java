package se.mickelus.tetra.blocks.workbench;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.ArrayUtils;
import se.mickelus.tetra.TetraMod;
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

public class WorkbenchTile extends TileEntity implements INamedContainerProvider {

    @ObjectHolder(TetraMod.MOD_ID + ":" + WorkbenchBlock.unlocalizedName)
    public static TileEntityType<WorkbenchTile> type;

    private static final String inventoryKey = "inv";
    private static final String currentSlotKey = "current_slot";
    private static final String schemaKey = "schema";

    public static final int inventorySlots = 4;

    private ItemStack previousTarget = ItemStack.EMPTY;
    private UpgradeSchema currentSchema;
    private String currentSlot;

    private Map<String, Runnable> changeListeners;

    private LazyOptional<ItemStackHandler> handler = LazyOptional.of(this::createHandler);

    private static WorkbenchAction[] defaultActions = new WorkbenchAction[] { new RepairAction() };

    private static WorkbenchAction[] actions = new WorkbenchAction[0];

    public WorkbenchTile() {
        super(type);
        changeListeners = new HashMap<>();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(inventorySlots) {

            @Override
            protected void onContentsChanged(int slot) {
                ItemStack itemStack = getStackInSlot(slot);
                if (slot == 0 && (itemStack.isEmpty() || !ItemStack.areItemStacksEqual(getTargetItemStack(), itemStack))) {
                    currentSchema = null;
                    currentSlot = null;

                    emptyMaterialSlots();
                }

                markDirty();
            }

            @Override
            public int getSlots() {
                if (currentSchema != null) {
                    return currentSchema.getNumMaterialSlots() + 1;
                }
                return 1;
            }
        };
    }

    public static void setupConfigActions(WorkbenchAction[] actions) {
        WorkbenchTile.actions = ArrayUtils.addAll(WorkbenchTile.defaultActions, actions);
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
                            CastOptional.cast(getBlockState().getBlock(), WorkbenchBlock.class)
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
            PacketHandler.sendToServer(new WorkbenchPacketUpdate(pos, currentSchema, currentSlot));
        } else {
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
            markDirty();
        }
    }

    public ItemStack getTargetItemStack() {
        return handler.map(handler -> {
            ItemStack stack = handler.getStackInSlot(0);

            ItemStack placeholder = ItemUpgradeRegistry.instance.getReplacement(stack);
            if (!placeholder.isEmpty()) {
                return placeholder;
            }

            return stack;
        })
                .orElse(ItemStack.EMPTY);
    }

    public ItemStack[] getMaterials() {
        return handler.map(handler -> {
            ItemStack[] result = new ItemStack[inventorySlots - 1];
            for (int i = 0; i < result.length; i++) {
                result[i] = handler.getStackInSlot(i + 1).copy();
            }
            return result;
        })
                .orElse(new ItemStack[0]);
    }

    public void initiateCrafting(PlayerEntity player) {
        if (world.isRemote) {
            PacketHandler.sendToServer(new WorkbenchPacketCraft(pos));
        }

        craft(player);

        sync();
    }

    public void craft(PlayerEntity player) {
        ItemStack targetStack = getTargetItemStack();
        ItemStack upgradedStack = targetStack;

        BlockState blockState = world.getBlockState(getPos());

        int[] availableCapabilities = CapabilityHelper.getCombinedCapabilityLevels(player, getWorld(), getPos(), blockState);

        ItemStack[] materials = getMaterials();
        ItemStack[] materialsAltered = Arrays.stream(getMaterials()).map(ItemStack::copy).toArray(ItemStack[]::new);

        if (currentSchema != null && currentSchema.canApplyUpgrade(player, targetStack, materialsAltered, currentSlot, availableCapabilities)) {
            upgradedStack = currentSchema.applyUpgrade(targetStack, materialsAltered, true, currentSlot, player);

            if (upgradedStack.getItem() instanceof ItemModular) {
                ((ItemModular) upgradedStack.getItem()).assemble(upgradedStack, world);
            }

            for (Capability capability : currentSchema.getRequiredCapabilities(targetStack, materials)) {
                int requiredLevel = currentSchema.getRequiredCapabilityLevel(targetStack, materials, capability);
                ItemStack providingStack = CapabilityHelper.getProvidingItemStack(capability, requiredLevel, player);
                if (!providingStack.isEmpty()) {
                    if (providingStack.getItem() instanceof ItemModular) {
                        upgradedStack = ((ItemModular) providingStack.getItem()).onCraftConsumeCapability(providingStack,
                                upgradedStack, player, capability, requiredLevel,true);
                    }
                } else {
                    ItemStack consumeTarget = upgradedStack;
                    CastOptional.cast(getBlockState().getBlock(), WorkbenchBlock.class)
                            .ifPresent(block -> block.onActionConsumeCapability(world, getPos(), blockState, consumeTarget,
                                    player, true));
                }
            }

            int xpCost = currentSchema.getExperienceCost(targetStack, materials, currentSlot);
            if (xpCost > 0) {
                player.addExperienceLevel(-xpCost);
            }
        }

        ItemStack tempStack = upgradedStack;
        handler.ifPresent(handler -> {
            for (int i = 0; i < materialsAltered.length; i++) {
                handler.setStackInSlot(i + 1, materialsAltered[i]);
            }

            emptyMaterialSlots(player);
            handler.setStackInSlot(0, tempStack);
        });

    }

    public void applyTweaks(PlayerEntity player, String slot, Map<String, Integer> tweaks) {
        if (world.isRemote) {
            PacketHandler.sendToServer(new WorkbenchPacketTweak(pos, slot, tweaks));
        }

        tweak(player, slot, tweaks);

        sync();
    }

    public void tweak(PlayerEntity player, String slot, Map<String, Integer> tweaks) {
        handler.ifPresent(handler -> {
            ItemStack tweakedStack = getTargetItemStack().copy();
            CastOptional.cast(tweakedStack.getItem(), ItemModular.class)
                    .ifPresent(item -> item.tweak(tweakedStack, slot, tweaks));

            handler.setStackInSlot(0, tweakedStack);
        });
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

        handler.ifPresent(handler -> handler.deserializeNBT(compound.getCompound(inventoryKey)));

        String schemaKey = compound.getString(WorkbenchTile.schemaKey);
        currentSchema = ItemUpgradeRegistry.instance.getSchema(schemaKey);

        if (compound.contains(currentSlotKey)) {
            currentSlot = compound.getString(currentSlotKey);
        }

        // todo : due to the null check perhaps this is not the right place to do this
        if (this.world != null && this.world.isRemote) {
            changeListeners.values().forEach(Runnable::run);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        handler.ifPresent(handler -> compound.put(inventoryKey, handler.serializeNBT()));

        if (currentSchema != null) {
            compound.putString(schemaKey, currentSchema.getKey());
        }

        if (currentSlot != null) {
            compound.putString(currentSlotKey, currentSlot);
        }

        return compound;
    }

    /**
     * Empties all material slots into the given players inventory.
     * @param player
     */
    private void emptyMaterialSlots(PlayerEntity player) {
        handler.ifPresent(handler -> {
            for (int i = 1; i < handler.getSlots(); i++) {
                transferStackToPlayer(player, i);
            }
            markDirty();
        });
    }

    /**
     * Empties all material slots into the world. Make sure to call on both sides.
     */
    private void emptyMaterialSlots() {
        handler.ifPresent(handler -> {
            if (!world.isRemote) {
                for (int i = 1; i < inventorySlots; i++) {
                    ItemStack materialStack = handler.extractItem(i, handler.getSlotLimit(i), false);
                    if (!materialStack.isEmpty()) {
                        ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + 0.5, (double)pos.getY() + 1.1, (double)pos.getZ() + 0.5, materialStack);
                        itemEntity.setDefaultPickupDelay();
                        world.addEntity(itemEntity);
                    }
                }
            } else {
                for (int i = 1; i < inventorySlots; i++) {
                    handler.extractItem(i, handler.getSlotLimit(i), false);
                }
            }
        });
    }

    private void transferStackToPlayer(PlayerEntity player, int index) {
        handler.ifPresent(handler -> {
            ItemStack itemStack = handler.extractItem(index, handler.getSlotLimit(index), false);
            if (!itemStack.isEmpty()) {
                if (!player.inventory.addItemStackToInventory(itemStack)) {
                    player.dropItem(itemStack, false);
                }
            }
        });
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(WorkbenchBlock.unlocalizedName);
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new WorkbenchContainer(windowId, this, playerInventory, playerEntity);
    }
}
