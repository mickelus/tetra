package se.mickelus.tetra.items.modular.impl.crossbow;

import com.google.common.collect.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.blocks.forged.chthonic.ChthonicExtractorBlock;
import se.mickelus.tetra.blocks.forged.chthonic.ExtractorProjectileEntity;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.ThrownModularItemEntity;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.data.ModuleModel;
import se.mickelus.tetra.module.schematic.RemoveSchematic;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.properties.TetraAttributes;
import se.mickelus.tetra.util.CastOptional;
import se.mickelus.tetra.util.NBTHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ModularCrossbowItem extends ModularItem {
    public final static String staveKey = "crossbow/stave";
    public final static String stockKey = "crossbow/stock";
    public final static String stringKey = "crossbow/string";

    public final static String attachmentAKey = "crossbow/attachment_0";
    public final static String attachmentBKey = "crossbow/attachment_1";

    public static final String unlocalizedName = "modular_crossbow";

    protected ModuleModel arrowModel = new ModuleModel("item", new ResourceLocation(TetraMod.MOD_ID, "items/module/crossbow/arrow"));
    protected ModuleModel extractorModel = new ModuleModel("item", new ResourceLocation(TetraMod.MOD_ID, "items/module/crossbow/extractor"));
    protected ModuleModel fireworkModel = new ModuleModel("item", new ResourceLocation(TetraMod.MOD_ID, "items/module/crossbow/firework"));

    private static final GuiModuleOffsets majorOffsets = new GuiModuleOffsets(-13, 0, -13, 18);
    private static final GuiModuleOffsets minorOffsets = new GuiModuleOffsets(4, -1, 13, 12, 4, 25);

    public static final double velocityFactor = 1 / 8d;

    // used to pick projectiles from the player inventory
    protected ItemStack shootableDummy;

    // todo: based on vanilla, uses bool in singleton to keep track of which sound to play. Would break if multiple entities use this simultaneously
    private boolean isLoadingStart = false;
    private boolean isLoadingMiddle = false;

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ModularCrossbowItem instance;

    public ModularCrossbowItem() {
        super(new Properties().maxStackSize(1).isImmuneToFire());
        setRegistryName(unlocalizedName);

        majorModuleKeys = new String[] { staveKey, stockKey };
        minorModuleKeys = new String[] { attachmentAKey, stringKey, attachmentBKey };

        requiredModules = new String[] { stringKey, stockKey, staveKey };

        shootableDummy = new ItemStack(new ShootableDummyItem());

        updateConfig(ConfigHandler.honeCrossbowBase.get(), ConfigHandler.honeCrossbowIntegrityMultiplier.get());

        SchematicRegistry.instance.registerSchematic(new RepairSchematic(this));
        RemoveSchematic.registerRemoveSchematics(this);
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }

    @Override
    public void clientInit() {
        super.clientInit();

        // todo: add item model property for transform overrides here, update overridelist and look at shield for props, or perhaps there's an arm rendering hook?

        MinecraftForge.EVENT_BUS.register(new CrossbowOverlay(Minecraft.getInstance()));
    }

    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        List<ItemStack> list = getProjectiles(stack);
        if (isLoaded(stack) && !list.isEmpty()) {
            ItemStack itemstack = list.get(0);
            tooltip.add((new TranslationTextComponent("item.minecraft.crossbow.projectile")).appendString(" ").append(itemstack.getTextComponent()));
            if (flagIn.isAdvanced() && itemstack.getItem() == Items.FIREWORK_ROCKET) {
                List<ITextComponent> list1 = Lists.newArrayList();
                Items.FIREWORK_ROCKET.addInformation(itemstack, worldIn, list1, flagIn);
                if (!list1.isEmpty()) {
                    for(int i = 0; i < list1.size(); ++i) {
                        list1.set(i, (new StringTextComponent("  ")).append(list1.get(i)).mergeStyle(TextFormatting.GRAY));
                    }

                    tooltip.addAll(list1);
                }
            }
        }

        super.addInformation(stack, worldIn, tooltip, flagIn);

        if (Screen.hasShiftDown()) {
            tooltip.add(new StringTextComponent(" "));
            tooltip.add(new TranslationTextComponent("item.tetra.crossbow.wip").mergeStyle(TextFormatting.GRAY));
            tooltip.add(new StringTextComponent(" "));
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack itemStack) {
        if (isBroken(itemStack)) {
            return AttributeHelper.emptyMap;
        }

        if (slot == EquipmentSlotType.MAINHAND) {
            return getAttributeModifiersCached(itemStack);
        }

        if (slot == EquipmentSlotType.OFFHAND) {
            return getAttributeModifiersCached(itemStack).entries().stream()
                    .filter(entry -> !(entry.getKey().equals(Attributes.ATTACK_DAMAGE) || entry.getKey().equals(Attributes.ATTACK_DAMAGE)))
                    .collect(Multimaps.toMultimap(Map.Entry::getKey, Map.Entry::getValue, ArrayListMultimap::create));
        }

        return AttributeHelper.emptyMap;
    }

    /**
     * Continously called while the item is "active"
     */
    @Override
    public void onUse(World world, LivingEntity entity, ItemStack itemStack, int count) {
        if (!world.isRemote) {
            int drawDuration = getReloadDuration(itemStack);
            float f = getProgress(itemStack, entity);

            if (f < 0.2F) {
                isLoadingStart = false;
                isLoadingMiddle = false;
            }

            if (f >= 0.2F && !isLoadingStart) {
                isLoadingStart = true;
                world.playSound(null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), getSoundEvent(drawDuration), SoundCategory.PLAYERS, 0.5F, 1.0F);
            }

            if (f >= 0.5F && drawDuration <= 28 && !isLoadingMiddle) {
                isLoadingMiddle = true;
                if (drawDuration > 21) {
                    world.playSound(null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE,
                            SoundCategory.PLAYERS, 0.5F, 1.0F);
                }
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        // todo: crossbows don't fire the nock event when loading arrows so needs some way to load ammo from quiver
//        ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(bowStack, world, player, hand, hasAmmo);
        ItemStack itemstack = player.getHeldItem(hand);
        if (isLoaded(itemstack)) {
            fireProjectiles(itemstack, world, player);
            setLoaded(itemstack, false);
            return ActionResult.resultConsume(itemstack);
        } else if (!findAmmo(player).isEmpty()) {
            if (!isLoaded(itemstack)) {
                this.isLoadingStart = false;
                this.isLoadingMiddle = false;
                player.setActiveHand(hand);
            }

            return ActionResult.resultConsume(itemstack);
        } else {
            return ActionResult.resultFail(itemstack);
        }
    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    @Override
    public void onPlayerStoppedUsing(ItemStack itemStack, World world, LivingEntity entity, int timeLeft) {
        float progress = getProgress(itemStack, entity);
        if (progress >= 1.0F && !isLoaded(itemStack)) {
            boolean gotLoaded = reload(entity, itemStack);
            if (gotLoaded) {
                setLoaded(itemStack, true);
                SoundCategory soundcategory = entity instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE;
                world.playSound(null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), SoundEvents.ITEM_CROSSBOW_LOADING_END, soundcategory,
                        1.0f, 1.0f / (random.nextFloat() * 0.5f + 1.0f) + 0.2f);
            }
        }

    }

    protected void fireProjectiles(ItemStack itemStack, World world, LivingEntity entity) {
        if (entity instanceof PlayerEntity && !world.isRemote) {
            PlayerEntity player = (PlayerEntity) entity;
            int count = Math.max(getEffectLevel(itemStack, ItemEffect.multishot), 1);
            List<ItemStack> list = takeProjectiles(itemStack, count);

            if (!list.isEmpty()) {
                double spread = getEffectEfficiency(itemStack, ItemEffect.multishot);

                for (int i = 0; i < list.size(); i++) {
                    ItemStack ammoStack = list.get(i);
                    double yaw = player.rotationYaw - spread * (count - 1) / 2f + spread * i;
                    fireProjectile(world, itemStack, ammoStack, player, yaw);
                }

                // todo: needs to apply 3 points of damage if it's firework
                itemStack.damageItem(1, player, p -> p.sendBreakAnimation(p.getActiveHand()));
                applyUsageEffects(entity, itemStack, 1);

                world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(),
                        SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1, 1);

                if (player instanceof ServerPlayerEntity) {
                    CriteriaTriggers.SHOT_CROSSBOW.test((ServerPlayerEntity) player, itemStack);

                    player.addStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    protected void fireProjectile(World world, ItemStack crossbowStack, ItemStack ammoStack, PlayerEntity player, double yaw) {
        double strength = getAttributeValue(crossbowStack, TetraAttributes.drawStrength.get());
        float velocityBonus = getEffectLevel(crossbowStack, ItemEffect.velocity) / 100f;
        float projectileVelocity = getProjectileVelocity(strength, velocityBonus);

        if (ChthonicExtractorBlock.item.equals(ammoStack.getItem()) || ChthonicExtractorBlock.usedItem.equals(ammoStack.getItem())) {
            ExtractorProjectileEntity projectileEntity = new ExtractorProjectileEntity(world, player, ammoStack);

            if (player.abilities.isCreativeMode) {
                projectileEntity.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
            }

            projectileEntity.func_234612_a_(player, player.rotationPitch, (float) yaw, 0.0F, projectileVelocity * 1F, 1.0F);
            world.addEntity(projectileEntity);
        } else if (ammoStack.getItem() instanceof FireworkRocketItem) {
            FireworkRocketEntity projectile = new FireworkRocketEntity(world, ammoStack, player, player.getPosX(),
                    player.getPosYEye() - 0.15, player.getPosZ(), true);

            projectile.func_234612_a_(player, player.rotationPitch, (float) yaw, 0.0F, projectileVelocity * 1.6F, 1.0F);
            world.addEntity(projectile);
        } else {
            ArrowItem ammoItem = CastOptional.cast(ammoStack.getItem(), ArrowItem.class).orElse((ArrowItem) Items.ARROW);

            AbstractArrowEntity projectile = ammoItem.createArrow(world, ammoStack, player);
            projectile.setHitSound(SoundEvents.ITEM_CROSSBOW_HIT);
            projectile.setShotFromCrossbow(true);
            projectile.setIsCritical(true);

            // the damage modifier is based on fully drawn damage, vanilla bows deal 3 times base damage + 0-4 crit damage
            projectile.setDamage(projectile.getDamage() -2 + strength / 3);

            // velocity multiplies arrow damage for vanilla projectiles, need to reduce damage if velocity > 1
            if (projectileVelocity > 1) {
                projectile.setDamage(projectile.getDamage() / projectileVelocity);
            }


            int piercingLevel = getEffectLevel(crossbowStack, ItemEffect.piercing);
            if (piercingLevel > 0) {
                projectile.setPierceLevel((byte) piercingLevel);
            }

            if (player.abilities.isCreativeMode) {
                projectile.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
            }

            projectile.func_234612_a_(player, player.rotationPitch, (float) yaw, 0.0F, projectileVelocity * 3.15F, 1.0F);
            world.addEntity(projectile);
        }
    }

    /**
     * Gets the velocity for the projectile entity
     */
    public static float getProjectileVelocity(double strength, float velocityBonus) {
        float velocity = (float) Math.max(1, 1 + (strength - 6) * velocityFactor);

        velocity += velocity * velocityBonus;

        return velocity;
    }

    public int getReloadDuration(ItemStack itemStack) {
        return Math.max((int) (20 * getAttributeValue(itemStack, TetraAttributes.drawSpeed.get())), 1);
    }

    /**
     * Returns a value between 0 - 1 representing how far the crossbow has been drawn, a value of 1 means that the crossbow is fully drawn
     * @param itemStack
     * @param entity
     * @return
     */
    public float getProgress(ItemStack itemStack, @Nullable LivingEntity entity) {
        return Optional.ofNullable(entity)
                .filter(e -> e.getItemInUseCount() > 0)
                .filter(e -> itemStack.equals(e.getActiveItemStack()))
                .map( e -> (getUseDuration(itemStack) - e.getItemInUseCount()) * 1f / getReloadDuration(itemStack))
                .orElse(0f);
    }

    private ItemStack findAmmo(LivingEntity entity) {
        return entity.findAmmo(shootableDummy);
    }

    private boolean reload(LivingEntity entity, ItemStack crossbowStack) {
        int count = Math.max(getEffectLevel(crossbowStack, ItemEffect.multishot), 1);
        boolean infinite = CastOptional.cast(entity, PlayerEntity.class)
                .map(player -> player.abilities.isCreativeMode)
                .orElse(false);

        // todo: this has to be improved
        ItemStack ammoStack = findAmmo(entity);
        ItemStack itemstack1 = ammoStack.copy();

        for(int i = 0; i < count; i++) {
            if (i > 0) {
                ammoStack = itemstack1.copy();
            }

            if (ammoStack.isEmpty() && infinite) {
                ammoStack = new ItemStack(Items.ARROW);
                itemstack1 = ammoStack.copy();
            }

            if (!loadProjectiles(entity, crossbowStack, ammoStack, i > 0, infinite)) {
                return false;
            }
        }

        return true;
    }

    private boolean loadProjectiles(LivingEntity entity, ItemStack crossbowStack, ItemStack ammoStack, boolean p_220023_3_, boolean p_220023_4_) {
        if (ammoStack.isEmpty()) {
            return false;
        } else {
            boolean flag = p_220023_4_ && ammoStack.getItem() instanceof ArrowItem;
            ItemStack itemstack;
            if (!flag && !p_220023_4_ && !p_220023_3_) {
                itemstack = ammoStack.split(1);
                if (ammoStack.isEmpty() && entity instanceof PlayerEntity) {
                    ((PlayerEntity)entity).inventory.deleteStack(ammoStack);
                }
            } else {
                itemstack = ammoStack.copy();
            }

            writeProjectile(crossbowStack, itemstack);
            return true;
        }
    }

    public boolean isLoaded(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getTag();
        return compoundnbt != null && compoundnbt.getBoolean("Charged");
    }

    public void setLoaded(ItemStack stack, boolean chargedIn) {
        CompoundNBT compoundnbt = stack.getOrCreateTag();
        compoundnbt.putBoolean("Charged", chargedIn);
    }

    private ListNBT getProjectilesNBT(CompoundNBT nbt) {
        if (nbt.contains("ChargedProjectiles", 9)) {
            return nbt.getList("ChargedProjectiles", 10);
        }
        return new ListNBT();
    }

    private void writeProjectile(ItemStack crossbowStack, ItemStack projectileStack) {
        CompoundNBT crossbowTag = crossbowStack.getOrCreateTag();
        ListNBT list = getProjectilesNBT(crossbowTag);

        CompoundNBT projectileTag = new CompoundNBT();
        projectileStack.write(projectileTag);
        list.add(projectileTag);

        crossbowTag.put("ChargedProjectiles", list);
    }

    private ItemStack getFirstProjectile(ItemStack itemStack) {
        ListNBT projectiles = getProjectilesNBT(NBTHelper.getTag(itemStack));
        if (projectiles.size() > 0) {
            return ItemStack.read(projectiles.getCompound(0));
        }

        return ItemStack.EMPTY;
    }

    private List<ItemStack> getProjectiles(ItemStack itemStack) {
        List<ItemStack> result = Lists.newArrayList();
        ListNBT projectileTags = getProjectilesNBT(NBTHelper.getTag(itemStack));

        for(int i = 0; i < projectileTags.size(); ++i) {
            CompoundNBT stackNbt = projectileTags.getCompound(i);
            result.add(ItemStack.read(stackNbt));
        }

        return result;
    }

    private List<ItemStack> takeProjectiles(ItemStack itemStack, int count) {
        ListNBT nbtList = getProjectilesNBT(NBTHelper.getTag(itemStack));
        int size = Math.min(nbtList.size(), count);
        List<ItemStack> result = new ArrayList<>(size);

        for(int i = 0; i < size; ++i) {
            CompoundNBT stackNbt = nbtList.getCompound(0);
            nbtList.remove(0);
            result.add(ItemStack.read(stackNbt));
        }

        return result;
    }

    public boolean hasProjectiles(ItemStack stack, Item ammoItem) {
        return getProjectiles(stack).stream().anyMatch(s -> s.getItem() == ammoItem);
    }

    private SoundEvent getSoundEvent(float velocity) {
        if (velocity < 7) {
            return SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_3;
        } else if (velocity < 15) {
            return SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_2;
        } else if (velocity < 22) {
            return SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_1;
        }

        return SoundEvents.ITEM_CROSSBOW_LOADING_START;
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 37000;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public UseAction getUseAction(ItemStack stack) {
        if (isLoaded(stack)) {
            return UseAction.BOW;
        }
        return UseAction.CROSSBOW;
    }

    @Override
    public boolean isCrossbow(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    private String getDrawVariant(ItemStack itemStack, @Nullable LivingEntity entity) {
        float progress = getProgress(itemStack, entity);

        if (isLoaded(itemStack)) {
            return "loaded";
        } else if (progress == 0) {
            return "item";
        } else if (progress < 0.58) {
            return "draw_0";
        } else if (progress < 1) {
            return "draw_1";
        }
        return "draw_2";
    }

    private String getProjectileVariant(ItemStack itemStack) {
        ItemStack projectileStack = getFirstProjectile(itemStack);

        if (projectileStack.getItem() instanceof FireworkRocketItem) {
            return "p1";
        }

        if (ChthonicExtractorBlock.item.equals(projectileStack.getItem()) || ChthonicExtractorBlock.usedItem.equals(projectileStack.getItem())) {
            return "p2";
        }

        return "p0";
    }

    private ModuleModel getProjectileModel(ItemStack itemStack) {
        ItemStack projectileStack = getFirstProjectile(itemStack);

        if (projectileStack.getItem() instanceof FireworkRocketItem) {
            return fireworkModel;
        }

        if (ChthonicExtractorBlock.item.equals(projectileStack.getItem()) || ChthonicExtractorBlock.usedItem.equals(projectileStack.getItem())) {
            return extractorModel;
        }

        return arrowModel;
    }

    @Override
    public String getModelCacheKey(ItemStack itemStack, LivingEntity entity) {
        return super.getModelCacheKey(itemStack, entity) + ":" +  getDrawVariant(itemStack, entity) + getProjectileVariant(itemStack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ImmutableList<ModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity) {
        String modelType = getDrawVariant(itemStack, entity);

        ImmutableList<ModuleModel> models = getAllModules(itemStack).stream()
                .sorted(Comparator.comparing(ItemModule::getRenderLayer))
                .flatMap(itemModule -> Arrays.stream(itemModule.getModels(itemStack)))
                .filter(Objects::nonNull)
                .filter(model -> model.type.equals(modelType) || model.type.equals("static"))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        if (isLoaded(itemStack)) {
            return ImmutableList.<ModuleModel>builder()
                    .addAll(models)
                    .add(getProjectileModel(itemStack))
                    .build();
        }

        return models;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMajorGuiOffsets() {
        return majorOffsets;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMinorGuiOffsets() {
        return minorOffsets;
    }
}
