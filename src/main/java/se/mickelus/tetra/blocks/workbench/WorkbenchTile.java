package se.mickelus.tetra.blocks.workbench;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.ArrayUtils;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.workbench.action.ConfigAction;
import se.mickelus.tetra.blocks.workbench.action.RepairAction;
import se.mickelus.tetra.blocks.workbench.action.WorkbenchAction;
import se.mickelus.tetra.blocks.workbench.action.WorkbenchActionPacket;
import se.mickelus.tetra.craftingeffect.CraftingEffectRegistry;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.tetra.properties.PropertyHelper;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WorkbenchTile extends TileEntity implements INamedContainerProvider {
    public static final String unlocalizedName = "workbench";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static TileEntityType<WorkbenchTile> type;

    @ObjectHolder(TetraMod.MOD_ID + ":" + WorkbenchTile.unlocalizedName)
    public static ContainerType<WorkbenchContainer> containerType;

    private static final String inventoryKey = "inv";
    private static final String currentSlotKey = "current_slot";
    private static final String schematicKey = "schematic";

    public static final int inventorySlots = 4;

    private ItemStack previousTarget = ItemStack.EMPTY;
    private UpgradeSchematic currentSchematic;
    private String currentSlot;

    private Map<String, Runnable> changeListeners;

    private LazyOptional<ItemStackHandler> handler = LazyOptional.of(this::createHandler);

    private static WorkbenchAction[] defaultActions = new WorkbenchAction[] { new RepairAction() };

    private static WorkbenchAction[] actions = new WorkbenchAction[0];
    static {
        DataManager.actionData.onReload(() -> {
            WorkbenchAction[] configActions = DataManager.actionData.getData().values().stream()
                    .flatMap(Arrays::stream).toArray(ConfigAction[]::new);

            actions = ArrayUtils.addAll(WorkbenchTile.defaultActions, configActions);
        });
    }

    private ActionInteraction interaction;

    public WorkbenchTile() {
        super(type);
        changeListeners = new HashMap<>();
    }

    public static void init(PacketHandler packetHandler) {
        packetHandler.registerPacket(WorkbenchPacketUpdate.class, WorkbenchPacketUpdate::new);
        packetHandler.registerPacket(WorkbenchPacketCraft.class, WorkbenchPacketCraft::new);
        packetHandler.registerPacket(WorkbenchActionPacket.class, WorkbenchActionPacket::new);
        packetHandler.registerPacket(WorkbenchPacketTweak.class, WorkbenchPacketTweak::new);
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
                    currentSchematic = null;
                    currentSlot = null;

                    emptyMaterialSlots();
                }

                if (slot == 0) {
                    interaction = ActionInteraction.create(WorkbenchTile.this);
                }

                markDirty();
                world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
            }

            @Override
            public int getSlots() {
                if (currentSchematic != null) {
                    return currentSchematic.getNumMaterialSlots() + 1;
                }
                return 1;
            }
        };
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
                .filter(action -> checkActionTools(player, action, targetStack))
                .ifPresent(action -> {
                    action.getRequiredTools(targetStack).forEach((requiredTool, requiredLevel) -> {
                        // consume player inventory
                        ItemStack providingStack = PropertyHelper.getPlayerProvidingItemStack(requiredTool, requiredLevel, player);
                        if (!providingStack.isEmpty()) {
                            if (providingStack.getItem() instanceof ModularItem) {
                                ((ModularItem) providingStack.getItem()).onActionConsume(providingStack,
                                        targetStack, player, requiredTool, requiredLevel,true);
                            }
                        } else {
                            // consume toolbelt inventory
                            ItemStack toolbeltResult = PropertyHelper.consumeActionToolToolbelt(player, targetStack, requiredTool, requiredLevel,
                                    true);

                            // consume blocks
                            if (toolbeltResult == null) {
                                CastOptional.cast(getBlockState().getBlock(), AbstractWorkbenchBlock.class)
                                        .ifPresent(block -> block.onActionConsumeTool(world, getPos(), blockState, targetStack, player,
                                                requiredTool, requiredLevel, true));
                            }
                        }
                    });

                    action.perform(player, targetStack, this);
                    markDirty();
                    world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
                });
    }

    /**
     * Perform an action that's not triggered by a player
     * @param actionKey
     */
    public void performAction(String actionKey) {
        BlockState blockState = world.getBlockState(getPos());
        ItemStack targetStack = getTargetItemStack();

        Arrays.stream(actions)
                .filter(action -> action.getKey().equals(actionKey))
                .findFirst()
                .filter(action -> action.canPerformOn(null, targetStack))
                .filter(action -> checkActionTools(action, targetStack))
                .ifPresent(action -> {
                    action.getRequiredTools(targetStack).forEach((requiredTool, requiredLevel) -> {
                            CastOptional.cast(getBlockState().getBlock(), AbstractWorkbenchBlock.class)
                                    .ifPresent(block -> block.onActionConsumeTool(world, getPos(), blockState, targetStack, null,
                                            requiredTool, requiredLevel, true));
                    });

                    action.perform(null, targetStack, this);
                    markDirty();
                    world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
                });
    }

    private boolean checkActionTools(PlayerEntity player, WorkbenchAction action, ItemStack itemStack) {
        return action.getRequiredTools(itemStack).entrySet().stream()
                .allMatch(requirement ->
                        PropertyHelper.getCombinedToolLevel(player, getWorld(), getPos(), world.getBlockState(getPos()), requirement.getKey())
                        >= requirement.getValue());
    }

    private boolean checkActionTools(WorkbenchAction action, ItemStack itemStack) {
        return action.getRequiredTools(itemStack).entrySet().stream()
                .allMatch(requirement ->
                        PropertyHelper.getBlockToolLevel(getWorld(), getPos(), world.getBlockState(getPos()), requirement.getKey())
                                >= requirement.getValue());
    }

    public BlockInteraction[] getInteractions() {
        if (interaction != null) {
            return new BlockInteraction[] { interaction };
        }

        return new BlockInteraction[0];
    }

    public UpgradeSchematic getCurrentSchematic() {
        return currentSchematic;
    }

    public void setCurrentSchematic(UpgradeSchematic schematic, String currentSlot) {

        this.currentSchematic = schematic;
        this.currentSlot = currentSlot;

        changeListeners.values().forEach(Runnable::run);
        sync();
    }

    public void clearSchematic() {
        setCurrentSchematic(null, null);
    }

    /**
     * Intended for updating the TE when receiving update packets on the server.
     * @param currentSchematic A schematic, or null if it should be unset
     * @param currentSlot A slot key, or null if it should be unset
     * @param player
     */
    public void update(UpgradeSchematic currentSchematic, String currentSlot, PlayerEntity player) {
        // todo: inventory change hack, better solution?
        if (currentSchematic == null && player != null) {
            emptyMaterialSlots(player);
        }

        this.currentSchematic = currentSchematic;
        this.currentSlot = currentSlot;

        sync();
    }
    public String getCurrentSlot() {
        return currentSlot;
    }

    private void sync() {
        if (world.isRemote) {
            PacketHandler.sendToServer(new WorkbenchPacketUpdate(pos, currentSchematic, currentSlot));
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

    public boolean isTargetPlaceholder() {
        return handler
                .map(handler -> handler.getStackInSlot(0))
                .map(stack -> ItemUpgradeRegistry.instance.getReplacement(stack))
                .map(placeholder -> !placeholder.isEmpty())
                .orElse(false);
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
        ModularItem item = CastOptional.cast(upgradedStack.getItem(), ModularItem.class).orElse(null);

        BlockState blockState = getBlockState();

        Map<ToolType, Integer> availableTools = PropertyHelper.getCombinedToolLevels(player, getWorld(), getPos(), blockState);

        ItemStack[] materials = getMaterials();
        ItemStack[] materialsAltered = Arrays.stream(getMaterials()).map(ItemStack::copy).toArray(ItemStack[]::new);

        if (item != null && currentSchematic != null && currentSchematic.canApplyUpgrade(player, targetStack, materialsAltered, currentSlot, availableTools)) {
            float severity = currentSchematic.getSeverity(targetStack, materialsAltered, currentSlot);
            boolean willReplace = currentSchematic.willReplace(targetStack, materialsAltered, currentSlot);

            // store durability and honing factor so that it can be restored after the schematic is applied
            double durabilityFactor = upgradedStack.isDamageable() ? upgradedStack.getDamage() * 1d / upgradedStack.getMaxDamage() : 0;
            double honingFactor = MathHelper.clamp(item.getHoningProgress(upgradedStack) * 1d / item.getHoningLimit(upgradedStack), 0, 1);

            Map<ToolType, Integer> tools = currentSchematic.getRequiredToolLevels(targetStack, materials);

            upgradedStack = currentSchematic.applyUpgrade(targetStack, materialsAltered, true, currentSlot, player);

            upgradedStack = applyCraftingBonusEffects(upgradedStack, currentSlot, willReplace, player, materials, materialsAltered, tools, world, pos, blockState, true);

            for (Map.Entry<ToolType, Integer> entry : tools.entrySet()) {
                upgradedStack = consumeCraftingToolEffects(upgradedStack, currentSlot, willReplace, entry.getKey(), entry.getValue(), player, world, pos, blockState, true);
            }

            item.assemble(upgradedStack, world, severity);

            // remove or restore honing progression
            if (currentSchematic.isHoning()) {
                ModularItem.removeHoneable(upgradedStack);
            } else if (ConfigHandler.moduleProgression.get() && !ModularItem.isHoneable(upgradedStack)) {
                item.setHoningProgress(upgradedStack, (int) Math.ceil(honingFactor * item.getHoningLimit(upgradedStack)));
            }

            // restore durability damage
            // todo: hacky check if repair schematic
            if (upgradedStack.isDamageable() && !(currentSchematic instanceof RepairSchematic) ) {
                if (durabilityFactor > 0 && willReplace && currentSlot.equals(item.getRepairSlot(upgradedStack))) {
                    item.repair(upgradedStack);
                } else {
                    upgradedStack.setDamage((int) Math.ceil(durabilityFactor * upgradedStack.getMaxDamage()));
                }
            }

            int xpCost = currentSchematic.getExperienceCost(targetStack, materials, currentSlot);
            if (!player.isCreative() && xpCost > 0) {
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

        clearSchematic();
    }

    /**
     * applies crafting tool effects in the following order: inventory, toolbelt, nearby blocks
     */
    public static ItemStack consumeCraftingToolEffects(ItemStack upgradedStack, String slot, boolean isReplacing, ToolType tool, int level,
            PlayerEntity player, World world, BlockPos pos, BlockState blockState, boolean consumeResources) {
        ItemStack providingStack = PropertyHelper.getPlayerProvidingItemStack(tool, level, player);
        if (!providingStack.isEmpty()) {
            if (providingStack.getItem() instanceof ModularItem) {
                upgradedStack = ((ModularItem) providingStack.getItem()).onCraftConsume(providingStack,
                        upgradedStack, player, tool, level,consumeResources);
            }
        } else {
            ItemStack toolbeltResult = PropertyHelper.consumeCraftToolToolbelt(player, upgradedStack, tool, level, consumeResources);
            if (toolbeltResult != null) {
                upgradedStack = toolbeltResult;
            } else {
                ItemStack consumeTarget = upgradedStack; // needs to be effectively final to be used in lambda
                upgradedStack = CastOptional.cast(blockState.getBlock(), AbstractWorkbenchBlock.class)
                        .map(block -> block.onCraftConsumeTool(world, pos, blockState, consumeTarget, slot, isReplacing, player, tool, level, consumeResources))
                        .orElse(upgradedStack);
            }
        }

        return upgradedStack;
    }

    public static ItemStack applyCraftingBonusEffects(ItemStack upgradedStack, String slot, boolean isReplacing, PlayerEntity player,
            ItemStack[] preMaterials, ItemStack[] postMaterials, Map<ToolType, Integer> tools, World world, BlockPos pos, BlockState blockState,
            boolean consumeResources) {
        ItemStack result = upgradedStack.copy();
        ResourceLocation[] unlockedEffects = CastOptional.cast(blockState.getBlock(), AbstractWorkbenchBlock.class)
                .map(block -> block.getCraftingEffects(world, pos, blockState))
                .orElse(new ResourceLocation[0]);
        Arrays.stream(CraftingEffectRegistry.getEffects(unlockedEffects, upgradedStack, slot, isReplacing, player, preMaterials, tools, world, pos, blockState))
                .forEach(craftingEffect -> craftingEffect.applyOutcomes(result, slot, isReplacing, player, preMaterials, postMaterials, tools, world,
                        pos, blockState, consumeResources));

        return result;
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
            CastOptional.cast(tweakedStack.getItem(), ModularItem.class)
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

        if (world != null && world.isRemote) {
            // TODO: this is called several times everytime a change occurs

            changeListeners.values().forEach(Runnable::run);
        }
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(getBlockState(), pkt.getNbtCompound());
    }

    @Override
    public void read(BlockState blockState, CompoundNBT compound) {
        super.read(blockState, compound);

        handler.ifPresent(handler -> handler.deserializeNBT(compound.getCompound(inventoryKey)));

        String schematicKey = compound.getString(WorkbenchTile.schematicKey);
        currentSchematic = SchematicRegistry.getSchematic(schematicKey);

        if (compound.contains(currentSlotKey)) {
            currentSlot = compound.getString(currentSlotKey);
        }

        interaction = ActionInteraction.create(this);

        // todo : due to the null check perhaps this is not the right place to do this
        if (world != null && world.isRemote) {
            changeListeners.values().forEach(Runnable::run);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        handler.ifPresent(handler -> compound.put(inventoryKey, handler.serializeNBT()));

        if (currentSchematic != null) {
            compound.putString(schematicKey, currentSchematic.getKey());
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
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
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
        return new StringTextComponent(unlocalizedName);
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new WorkbenchContainer(windowId, this, playerInventory, playerEntity);
    }
}
