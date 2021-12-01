package se.mickelus.tetra.blocks.workbench;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
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
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.tetra.properties.IToolProvider;
import se.mickelus.tetra.properties.PropertyHelper;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.modular.IModularItem;
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

public class WorkbenchTile extends BlockEntity implements MenuProvider {
    public static final String unlocalizedName = "workbench";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockEntityType<WorkbenchTile> type;

    @ObjectHolder(TetraMod.MOD_ID + ":" + WorkbenchTile.unlocalizedName)
    public static MenuType<WorkbenchContainer> containerType;

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
                if (slot == 0 && (itemStack.isEmpty() || !ItemStack.matches(getTargetItemStack(), itemStack))) {
                    currentSchematic = null;
                    currentSlot = null;

                    emptyMaterialSlots();
                }

                if (slot == 0) {
                    interaction = ActionInteraction.create(WorkbenchTile.this);
                }

                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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

    public WorkbenchAction[] getAvailableActions(Player player) {
        ItemStack itemStack = getTargetItemStack();
        return Arrays.stream(actions)
                .filter(action -> action.canPerformOn(player, this, itemStack))
                .toArray(WorkbenchAction[]::new);
    }

    public void performAction(Player player, String actionKey) {
        if (level.isClientSide) {
            TetraMod.packetHandler.sendToServer(new WorkbenchActionPacket(worldPosition, actionKey));
            return;
        }

        BlockState blockState = level.getBlockState(getBlockPos());
        ItemStack targetStack = getTargetItemStack();

        Arrays.stream(actions)
                .filter(action -> action.getKey().equals(actionKey))
                .findFirst()
                .filter(action -> action.canPerformOn(player, this, targetStack))
                .filter(action -> checkActionTools(player, action, targetStack))
                .ifPresent(action -> {
                    action.getRequiredTools(targetStack).forEach((requiredTool, requiredLevel) -> {
                        // consume player inventory
                        ItemStack providingStack = PropertyHelper.getPlayerProvidingItemStack(requiredTool, requiredLevel, player);
                        if (!providingStack.isEmpty()) {
                            if (providingStack.getItem() instanceof IToolProvider) {
                                ((IToolProvider) providingStack.getItem()).onActionConsume(providingStack,
                                        targetStack, player, requiredTool, requiredLevel,true);
                            }
                        } else {
                            // consume toolbelt inventory
                            ItemStack toolbeltResult = PropertyHelper.consumeActionToolToolbelt(player, targetStack, requiredTool, requiredLevel,
                                    true);

                            // consume blocks
                            if (toolbeltResult == null) {
                                CastOptional.cast(getBlockState().getBlock(), AbstractWorkbenchBlock.class)
                                        .ifPresent(block -> block.onActionConsumeTool(level, getBlockPos(), blockState, targetStack, player,
                                                requiredTool, requiredLevel, true));
                            }
                        }
                    });

                    action.perform(player, targetStack, this);
                    setChanged();
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                });
    }

    /**
     * Perform an action that's not triggered by a player
     * @param actionKey
     */
    public void performAction(String actionKey) {
        BlockState blockState = level.getBlockState(getBlockPos());
        ItemStack targetStack = getTargetItemStack();

        Arrays.stream(actions)
                .filter(action -> action.getKey().equals(actionKey))
                .findFirst()
                .filter(action -> action.canPerformOn(null, this, targetStack))
                .filter(action -> checkActionTools(action, targetStack))
                .ifPresent(action -> {
                    action.getRequiredTools(targetStack).forEach((requiredTool, requiredLevel) -> {
                            CastOptional.cast(getBlockState().getBlock(), AbstractWorkbenchBlock.class)
                                    .ifPresent(block -> block.onActionConsumeTool(level, getBlockPos(), blockState, targetStack, null,
                                            requiredTool, requiredLevel, true));
                    });

                    action.perform(null, targetStack, this);
                    setChanged();
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                });
    }

    private boolean checkActionTools(Player player, WorkbenchAction action, ItemStack itemStack) {
        return action.getRequiredTools(itemStack).entrySet().stream()
                .allMatch(requirement ->
                        PropertyHelper.getCombinedToolLevel(player, getLevel(), getBlockPos(), level.getBlockState(getBlockPos()), requirement.getKey())
                        >= requirement.getValue());
    }

    private boolean checkActionTools(WorkbenchAction action, ItemStack itemStack) {
        return action.getRequiredTools(itemStack).entrySet().stream()
                .allMatch(requirement ->
                        PropertyHelper.getBlockToolLevel(getLevel(), getBlockPos(), level.getBlockState(getBlockPos()), requirement.getKey())
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
    public void update(UpgradeSchematic currentSchematic, String currentSlot, Player player) {
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
        if (level.isClientSide) {
            TetraMod.packetHandler.sendToServer(new WorkbenchPacketUpdate(worldPosition, currentSchematic, currentSlot));
        } else {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            setChanged();
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

    public void initiateCrafting(Player player) {
        if (level.isClientSide) {
            TetraMod.packetHandler.sendToServer(new WorkbenchPacketCraft(worldPosition));
        }

        craft(player);

        sync();
    }

    public void craft(Player player) {
        ItemStack targetStack = getTargetItemStack();
        ItemStack upgradedStack = targetStack;
        IModularItem item = CastOptional.cast(upgradedStack.getItem(), IModularItem.class).orElse(null);

        BlockState blockState = getBlockState();

        Map<ToolType, Integer> availableTools = PropertyHelper.getCombinedToolLevels(player, getLevel(), getBlockPos(), blockState);

        ItemStack[] materials = getMaterials();
        ItemStack[] materialsAltered = Arrays.stream(getMaterials()).map(ItemStack::copy).toArray(ItemStack[]::new);

        if (item != null && currentSchematic != null && currentSchematic.canApplyUpgrade(player, targetStack, materialsAltered, currentSlot, availableTools)) {
            float severity = currentSchematic.getSeverity(targetStack, materialsAltered, currentSlot);
            boolean willReplace = currentSchematic.willReplace(targetStack, materialsAltered, currentSlot);

            // store durability and honing factor so that it can be restored after the schematic is applied
            double durabilityFactor = upgradedStack.isDamageableItem() ? upgradedStack.getDamageValue() * 1d / upgradedStack.getMaxDamage() : 0;
            double honingFactor = Mth.clamp(item.getHoningProgress(upgradedStack) * 1d / item.getHoningLimit(upgradedStack), 0, 1);

            Map<ToolType, Integer> tools = currentSchematic.getRequiredToolLevels(targetStack, materials);

            upgradedStack = currentSchematic.applyUpgrade(targetStack, materialsAltered, true, currentSlot, player);

            upgradedStack = applyCraftingBonusEffects(upgradedStack, currentSlot, willReplace, player, materials, materialsAltered, tools, level, worldPosition, blockState, true);

            for (Map.Entry<ToolType, Integer> entry : tools.entrySet()) {
                upgradedStack = consumeCraftingToolEffects(upgradedStack, currentSlot, willReplace, entry.getKey(), entry.getValue(), player, level, worldPosition, blockState, true);
            }

            item.assemble(upgradedStack, level, severity);

            // remove or restore honing progression
            if (currentSchematic.isHoning()) {
                IModularItem.removeHoneable(upgradedStack);
            } else if (ConfigHandler.moduleProgression.get() && !IModularItem.isHoneable(upgradedStack)) {
                item.setHoningProgress(upgradedStack, (int) Math.ceil(honingFactor * item.getHoningLimit(upgradedStack)));
            }

            // restore durability damage
            // todo: hacky check if repair schematic
            if (upgradedStack.isDamageableItem() && !(currentSchematic instanceof RepairSchematic) ) {
                if (durabilityFactor > 0 && willReplace && currentSlot.equals(item.getRepairSlot(upgradedStack))) {
                    item.repair(upgradedStack);
                } else {
                    upgradedStack.setDamageValue((int) Math.ceil(durabilityFactor * upgradedStack.getMaxDamage()));
                }
            }

            int xpCost = currentSchematic.getExperienceCost(targetStack, materials, currentSlot);
            if (!player.isCreative() && xpCost > 0) {
                player.giveExperienceLevels(-xpCost);
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
            Player player, Level world, BlockPos pos, BlockState blockState, boolean consumeResources) {
        ItemStack providingStack = PropertyHelper.getPlayerProvidingItemStack(tool, level, player);
        if (!providingStack.isEmpty()) {
            if (providingStack.getItem() instanceof IToolProvider) {
                upgradedStack = ((IToolProvider) providingStack.getItem()).onCraftConsume(providingStack,
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

    public static ItemStack applyCraftingBonusEffects(ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] preMaterials, ItemStack[] postMaterials, Map<ToolType, Integer> tools, Level world, BlockPos pos, BlockState blockState,
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

    public ResourceLocation[] getUnlockedSchematics() {
        return CastOptional.cast(getBlockState().getBlock(), AbstractWorkbenchBlock.class)
                .map(block -> block.getSchematics(level, worldPosition, getBlockState()))
                .orElse(new ResourceLocation[0]);
    }

    public void applyTweaks(Player player, String slot, Map<String, Integer> tweaks) {
        if (level.isClientSide) {
            TetraMod.packetHandler.sendToServer(new WorkbenchPacketTweak(worldPosition, slot, tweaks));
        }

        tweak(player, slot, tweaks);

        sync();
    }

    public void tweak(Player player, String slot, Map<String, Integer> tweaks) {
        handler.ifPresent(handler -> {
            ItemStack tweakedStack = getTargetItemStack().copy();
            CastOptional.cast(tweakedStack.getItem(), IModularItem.class)
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
    public void setChanged() {
        super.setChanged();

        if (level != null && level.isClientSide) {
            // TODO: this is called several times everytime a change occurs

            changeListeners.values().forEach(Runnable::run);
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(worldPosition, 0, getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(getBlockState(), pkt.getTag());
    }

    @Override
    public void load(BlockState blockState, CompoundTag compound) {
        super.load(blockState, compound);

        handler.ifPresent(handler -> handler.deserializeNBT(compound.getCompound(inventoryKey)));

        String schematicKey = compound.getString(WorkbenchTile.schematicKey);
        currentSchematic = SchematicRegistry.getSchematic(schematicKey);

        if (compound.contains(currentSlotKey)) {
            currentSlot = compound.getString(currentSlotKey);
        }

        interaction = ActionInteraction.create(this);

        // todo : due to the null check perhaps this is not the right place to do this
        if (level != null && level.isClientSide) {
            changeListeners.values().forEach(Runnable::run);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);

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
    private void emptyMaterialSlots(Player player) {
        handler.ifPresent(handler -> {
            for (int i = 1; i < handler.getSlots(); i++) {
                transferStackToPlayer(player, i);
            }
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        });
    }

    /**
     * Empties all material slots into the world. Make sure to call on both sides.
     */
    private void emptyMaterialSlots() {
        handler.ifPresent(handler -> {
            if (!level.isClientSide) {
                for (int i = 1; i < inventorySlots; i++) {
                    ItemStack materialStack = handler.extractItem(i, handler.getSlotLimit(i), false);
                    if (!materialStack.isEmpty()) {
                        ItemEntity itemEntity = new ItemEntity(level, (double)worldPosition.getX() + 0.5, (double)worldPosition.getY() + 1.1, (double)worldPosition.getZ() + 0.5, materialStack);
                        itemEntity.setDefaultPickUpDelay();
                        level.addFreshEntity(itemEntity);
                    }
                }
            } else {
                for (int i = 1; i < inventorySlots; i++) {
                    handler.extractItem(i, handler.getSlotLimit(i), false);
                }
            }
        });
    }

    private void transferStackToPlayer(Player player, int index) {
        handler.ifPresent(handler -> {
            ItemStack itemStack = handler.extractItem(index, handler.getSlotLimit(index), false);
            if (!itemStack.isEmpty()) {
                if (!player.inventory.add(itemStack)) {
                    player.drop(itemStack, false);
                }
            }
        });
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent(unlocalizedName);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
        return new WorkbenchContainer(windowId, this, playerInventory, playerEntity);
    }
}
