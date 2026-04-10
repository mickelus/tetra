package se.mickelus.tetra.items.modular.impl.crossbow;

import com.google.common.collect.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.NotNull;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.forged.chthonic.ChthonicExtractorBlock;
import se.mickelus.tetra.blocks.forged.chthonic.ExtractorProjectileEntity;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.event.ModularLooseProjectilesEvent;
import se.mickelus.tetra.event.ModularProjectileSpawnEvent;
import se.mickelus.tetra.items.modular.impl.bow.ProjectileMotionPacket;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.properties.TetraAttributes;
import se.mickelus.tetra.util.ItemStackTagHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public class ModularCrossbowItemImpl extends AbstractModularCrossbowItem {
    public static final double velocityFactor = 1 / 8d;

    @ObjectHolder(registryName = "item", value = TetraMod.MOD_ID + ":" + identifier)
    public static ModularCrossbowItemImpl instance;
    public static double multishotDefaultSpread = 10;
    // used to pick projectiles from the player inventory
    protected ItemStack shootableDummy;
    // todo: based on vanilla, uses bool in singleton to keep track of which sound to play. Would break if multiple entities use this simultaneously
    private boolean isLoadingStart = false;
    private boolean isLoadingMiddle = false;

    public ModularCrossbowItemImpl(@NotNull Item shootableDummy) {
        super(new Properties().stacksTo(1).fireResistant());
        instance = this;

        this.shootableDummy = new ItemStack(shootableDummy);
    }

    /**
     * Gets the velocity for the projectile entity
     */
    public static float getProjectileVelocity(double strength, float velocityBonus) {
        float velocity = (float) Math.max(1, 1 + (strength - 6) * velocityFactor);

        velocity += velocity * velocityBonus;

        return velocity;
    }

    @Override
    public void commonInit(PacketHandler packetHandler) {
        DataManager.instance.synergyData.onReload(() -> synergies = DataManager.instance.synergyData.getOrdered("crossbow/"));
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }

    @Override
    public void clientInit() {
        super.clientInit();

        // todo: add item model property for transform overrides here, update overridelist and look at shield for props, or perhaps there's an arm
        //  rendering hook?

        NeoForge.EVENT_BUS.register(new CrossbowOverlay(Minecraft.getInstance()));
    }

    @OnlyIn(Dist.CLIENT)
    public IClientItemExtensions createClientExtensions() {
        return new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (entityLiving instanceof AbstractClientPlayer player
                        && !player.isUsingItem()
                        && !player.swinging
                        && ModularCrossbowItemImpl.this.equals(itemStack.getItem())
                        && isLoaded(itemStack)) {
                    return HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }

                return null;
            }

            @Override
            public boolean applyForgeHandTransform(
                    PoseStack poseStack,
                    LocalPlayer player,
                    HumanoidArm arm,
                    ItemStack itemInHand,
                    float partialTick,
                    float equipProcess,
                    float swingProcess
            ) {
                if (!ModularCrossbowItemImpl.this.equals(itemInHand.getItem())) {
                    return false;
                }

                InteractionHand hand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                boolean isMainHand = hand == InteractionHand.MAIN_HAND;
                boolean isCharged = CrossbowItem.isCharged(itemInHand);
                boolean rightArm = arm == HumanoidArm.RIGHT;
                int direction = rightArm ? 1 : -1;

                if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
                    poseStack.translate(direction * 0.56F, -0.52F + equipProcess * -0.6F, -0.72F);
                    poseStack.translate(direction * -0.4785682F, -0.094387F, 0.05731531F);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-11.935F));
                    poseStack.mulPose(Axis.YP.rotationDegrees(direction * 65.3F));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(direction * -9.785F));

                    float useTime = itemInHand.getUseDuration(player) - (player.getUseItemRemainingTicks() - partialTick + 1.0F);
                    float progress = useTime / getReloadDuration(itemInHand);
                    if (progress > 1.0F) {
                        progress = 1.0F;
                    }

                    if (progress > 0.1F) {
                        float sway = Mth.sin((useTime - 0.1F) * 1.3F);
                        float swayScale = progress - 0.1F;
                        float offset = sway * swayScale;
                        poseStack.translate(0.0F, offset * 0.004F, 0.0F);
                    }

                    poseStack.translate(0.0F, 0.0F, progress * 0.04F);
                    poseStack.scale(1.0F, 1.0F, 1.0F + progress * 0.2F);
                    poseStack.mulPose(Axis.YN.rotationDegrees(direction * 45.0F));
                    return true;
                }

                if (isCharged && swingProcess < 0.001F && isMainHand) {
                    poseStack.translate(direction * -0.641864F, 0.0F, 0.0F);
                    poseStack.mulPose(Axis.YP.rotationDegrees(direction * 10.0F));
                    return true;
                }

                return false;
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        List<ItemStack> list = getProjectiles(stack);
        if (isLoaded(stack) && !list.isEmpty()) {
            ItemStack itemstack = list.get(0);
            tooltip.add((Component.translatable("item.minecraft.crossbow.projectile")).append(" ").append(itemstack.getDisplayName()));
            if (flagIn.isAdvanced() && itemstack.getItem() == Items.FIREWORK_ROCKET) {
                List<Component> list1 = Lists.newArrayList();
                Items.FIREWORK_ROCKET.appendHoverText(itemstack, context, list1, flagIn);
                if (!list1.isEmpty()) {
                    for (int i = 0; i < list1.size(); ++i) {
                        list1.set(i, (Component.literal("  ")).append(list1.get(i)).withStyle(ChatFormatting.GRAY));
                    }

                    tooltip.addAll(list1);
                }
            }
        }

        super.appendHoverText(stack, context, tooltip, flagIn);

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("item.tetra.crossbow.wip").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(" "));
        }
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack itemStack) {
        if (isBroken(itemStack)) {
            return ItemAttributeModifiers.EMPTY;
        }

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        getAttributeModifiersCached(itemStack).forEach((attribute, modifier) -> {
            EquipmentSlotGroup slotGroup = attribute.equals(Attributes.ATTACK_DAMAGE.value()) || attribute.equals(Attributes.ATTACK_SPEED.value())
                    ? EquipmentSlotGroup.MAINHAND
                    : EquipmentSlotGroup.HAND;
            builder.add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), modifier, slotGroup);
        });
        return builder.build();
    }

    /**
     * Continously called while the item is "active"
     */
    @Override
    public void onUseTick(Level world, LivingEntity entity, ItemStack itemStack, int count) {
        if (!world.isClientSide) {
            int drawDuration = getReloadDuration(itemStack);
            float f = getProgress(itemStack, entity);

            if (f < 0.2F) {
                isLoadingStart = false;
                isLoadingMiddle = false;
            }

            if (f >= 0.2F && !isLoadingStart) {
                isLoadingStart = true;
                world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), getSoundEvent(drawDuration), SoundSource.PLAYERS, 0.5F, 1.0F);
            }

            if (f >= 0.5F && drawDuration <= 28 && !isLoadingMiddle) {
                isLoadingMiddle = true;
                if (drawDuration > 21) {
                    world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.CROSSBOW_LOADING_MIDDLE,
                            SoundSource.PLAYERS, 0.5F, 1.0F);
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (isBroken(itemstack)) {
            return InteractionResultHolder.pass(itemstack);
        }

        if (isLoaded(itemstack)) {
            fireProjectiles(itemstack, world, player);
            setLoaded(itemstack, false);
            return InteractionResultHolder.consume(itemstack);
        }

        if (findAmmo(player).isEmpty()) {
            ToolbeltHelper.loadQuickAccessAmmoFromQuiver(player, hand, Math.max(getEffectLevel(itemstack, ItemEffect.ammoCapacity), 1));
        }

        if (!findAmmo(player).isEmpty()) {
            if (!isLoaded(itemstack)) {
                this.isLoadingStart = false;
                this.isLoadingMiddle = false;
                player.startUsingItem(hand);
            }

            return InteractionResultHolder.consume(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    @Override
    public void releaseUsing(ItemStack itemStack, Level world, LivingEntity entity, int timeLeft) {
        float progress = getProgress(itemStack, entity);
        if (progress >= 1.0F && !isLoaded(itemStack)) {
            boolean gotLoaded = reload(entity, itemStack);
            if (gotLoaded) {
                setLoaded(itemStack, true);
                SoundSource soundcategory = entity instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
                world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundcategory,
                        1.0f, 1.0f / (world.random.nextFloat() * 0.5f + 1.0f) + 0.2f);
            }
        }

    }

    protected void fireProjectiles(ItemStack itemStack, Level world, LivingEntity entity) {
        if (entity instanceof Player player && !world.isClientSide) {
            ItemStack advancementCopy = itemStack.copy();

            List<ItemStack> list = takeProjectiles(itemStack, 1);
            if (!list.isEmpty()) {
                int multishotEnchantLevel = EffectHelper.getEnchantmentLevel(Enchantments.MULTISHOT, itemStack) * 3;
                int count = Math.max(getEffectLevel(itemStack, ItemEffect.multishot) + multishotEnchantLevel, 1);
                double strength = getAttributeValue(itemStack, TetraAttributes.drawStrength.get());
                float velocityBonus = getEffectLevel(itemStack, ItemEffect.velocity) / 100f;
                float projectileVelocity = getProjectileVelocity(strength, velocityBonus);
                double spread = getEffectEfficiency(itemStack, ItemEffect.multishot);

                if (spread == 0 && multishotEnchantLevel > 0) {
                    spread = multishotDefaultSpread;
                }

                ModularLooseProjectilesEvent looseProjectilesEvent = new ModularLooseProjectilesEvent(itemStack, list.get(0), player, world, 1,
                        strength,
                        false,
                        projectileVelocity,
                        spread,
                        1,
                        player.getAbilities().instabuild,
                        count,
                        player.getXRot(),
                        player.getYRot());
                NeoForge.EVENT_BUS.post(looseProjectilesEvent);

                count = looseProjectilesEvent.getCount();
                spread = looseProjectilesEvent.getMultishotSpread();
                for (int i = 0; i < count; i++) {
                    double yaw = looseProjectilesEvent.getBaseYaw() - spread * (count - 1) / 2f + spread * i;
                    boolean isDupe = looseProjectilesEvent.isInfiniteAmmo() || count > 1 && i != count / 2;
                    fireProjectile(world, looseProjectilesEvent.getFiringStack(),
                            looseProjectilesEvent.getAmmoStack(),
                            looseProjectilesEvent.getProjectileRemappers(),
                            player,
                            looseProjectilesEvent.getStrength(),
                            looseProjectilesEvent.getProjectileVelocity(),
                            (float) looseProjectilesEvent.getBasePitch(),
                            (float) yaw,
                            isDupe);
                }

                // todo: needs to apply 3 points of damage if it's firework
                itemStack.hurtAndBreak(1, player, player.getOffhandItem() == itemStack ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND);
                applyUsageEffects(entity, itemStack, 1);

                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1, 1);

                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.SHOT_CROSSBOW.trigger((ServerPlayer) player, advancementCopy);

                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    protected void fireProjectile(Level world, ItemStack crossbowStack, ItemStack ammoStack,
            ImmutableList<Function<AbstractArrow, AbstractArrow>> projectileRemappers, Player player, double strength, float projectileVelocity,
            float pitch, float yaw, boolean isDupe) {
        if (ChthonicExtractorBlock.item.equals(ammoStack.getItem()) || ChthonicExtractorBlock.usedItem.equals(ammoStack.getItem())) {
            ExtractorProjectileEntity projectileEntity = new ExtractorProjectileEntity(world, player, ammoStack);

            if (isDupe) {
                projectileEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }

            projectileEntity.shootFromRotation(player, pitch, yaw, 0.0F, projectileVelocity, 1.0F);
            world.addFreshEntity(projectileEntity);
        } else if (ammoStack.getItem() instanceof FireworkRocketItem) {
            FireworkRocketEntity projectile = new FireworkRocketEntity(world, ammoStack, player, player.getX(),
                    player.getEyeY() - 0.15, player.getZ(), true);

            spawnProjectile(player, world, projectile, projectileVelocity * 1.6F, pitch, yaw);
        } else {
            ArrowItem ammoItem = CastOptional.cast(ammoStack.getItem(), ArrowItem.class).orElse((ArrowItem) Items.ARROW);

            AbstractArrow projectile = ammoItem.createArrow(world, ammoStack, player, getProjectileWeapon(crossbowStack));
            projectile.setSoundEvent(SoundEvents.CROSSBOW_HIT);
            projectile.setCritArrow(true);

            // the damage modifier is based on fully drawn damage, vanilla bows deal 3 times base damage + 0-4 crit damage
            projectile.setBaseDamage(projectile.getBaseDamage() - 2 + strength / 3);

            // velocity multiplies arrow damage for vanilla projectiles, need to reduce damage if velocity > 1
            if (projectileVelocity > 1) {
                projectile.setBaseDamage(projectile.getBaseDamage() / projectileVelocity);
            }

            int piercingLevel =
                    getEffectLevel(crossbowStack, ItemEffect.piercing) + EffectHelper.getEnchantmentLevel(Enchantments.PIERCING,
                            crossbowStack);
            if (piercingLevel > 0) {
                projectile.setPierceLevel((byte) piercingLevel);
            }

            if (isDupe) {
                projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }

            for (Function<AbstractArrow, AbstractArrow> remapper : projectileRemappers) {
                projectile = remapper.apply(projectile);
            }
            spawnProjectile(player, world, projectile, projectileVelocity * 3.15F, pitch, yaw);
            ModularProjectileSpawnEvent event = new ModularProjectileSpawnEvent(crossbowStack, ammoStack, player, projectile, world, 1);
            NeoForge.EVENT_BUS.post(event);
        }
    }

    protected void spawnProjectile(Player player, Level world, Projectile projectile, float projectileVelocity, float pitch, float yaw) {
        projectile.shootFromRotation(player, pitch, yaw, 0.0F, projectileVelocity, 1.0F);
        world.addFreshEntity(projectile);

        // vanilla velocity sync breaks when velocity is >3.9 on any axis
        if (projectileVelocity > 4) {
            TetraMod.packetHandler.sendToAllPlayersNear(new ProjectileMotionPacket(projectile), projectile.blockPosition(), 512, world.dimension());
        }
    }

    private ItemStack getProjectileWeapon(ItemStack crossbowStack) {
        ItemStack weaponStack = Items.CROSSBOW.getDefaultInstance();
        EnchantmentHelper.setEnchantments(weaponStack, EnchantmentHelper.getEnchantmentsForCrafting(crossbowStack));
        return weaponStack;
    }

    public int getReloadDuration(ItemStack itemStack) {
        return Math.max((int) (20 * (getAttributeValue(itemStack, TetraAttributes.drawSpeed.get())
                - EffectHelper.getEnchantmentLevel(Enchantments.QUICK_CHARGE, itemStack) * 0.2)), 1);
    }

    /**
     * Returns a value between 0 - 1 representing how far the crossbow has been drawn, a value of 1 means that the crossbow is fully drawn
     *
     * @param itemStack
     * @param entity
     * @return
     */
    public float getProgress(ItemStack itemStack, @Nullable LivingEntity entity) {
        return Optional.ofNullable(entity)
                .filter(e -> e.getUseItemRemainingTicks() > 0)
                .filter(e -> itemStack.equals(e.getUseItem()))
                .map(e -> (getUseDuration(itemStack) - e.getUseItemRemainingTicks()) * 1f / getReloadDuration(itemStack))
                .orElse(0f);
    }

    private ItemStack findAmmo(LivingEntity entity) {
        return entity.getProjectile(shootableDummy);
    }

    @Override
    public boolean isLoaded(ItemStack stack) {
        CompoundTag compoundnbt = ItemStackTagHelper.getTag(stack);
        return compoundnbt != null && compoundnbt.getBoolean("Charged");
    }

    public void setLoaded(ItemStack stack, boolean chargedIn) {
        CompoundTag compoundnbt = ItemStackTagHelper.getOrCreateTag(stack);
        compoundnbt.putBoolean("Charged", chargedIn);
    }

    private ListTag getProjectilesNBT(ItemStack itemStack) {
        CompoundTag tag = ItemStackTagHelper.getTag(itemStack);
        if (tag != null) {
            return getProjectilesNBT(tag);
        }
        return new ListTag();
    }

    private ListTag getProjectilesNBT(CompoundTag nbt) {
        if (nbt.contains("ChargedProjectiles", 9)) {
            return nbt.getList("ChargedProjectiles", 10);
        }
        return new ListTag();
    }

    private void writeProjectile(ItemStack crossbowStack, ItemStack projectileStack) {
        if (projectileStack.isEmpty()) {
            return;
        }

        CompoundTag crossbowTag = ItemStackTagHelper.getOrCreateTag(crossbowStack);
        ListTag list = getProjectilesNBT(crossbowTag);

        CompoundTag projectileTag = ItemStackTagHelper.saveStack(projectileStack);
        list.add(projectileTag);

        crossbowTag.put("ChargedProjectiles", list);
    }

    @Override
    protected ItemStack getFirstProjectile(ItemStack itemStack) {
        ListTag projectiles = getProjectilesNBT(itemStack);
        for (int i = 0; i < projectiles.size(); i++) {
            ItemStack projectile = ItemStackTagHelper.parseStack(projectiles.getCompound(i));
            if (!projectile.isEmpty()) {
                return projectile;
            }
        }

        return ItemStack.EMPTY;
    }

    private List<ItemStack> getProjectiles(ItemStack itemStack) {
        List<ItemStack> result = Lists.newArrayList();
        ListTag projectileTags = getProjectilesNBT(itemStack);

        for (int i = 0; i < projectileTags.size(); ++i) {
            CompoundTag stackNbt = projectileTags.getCompound(i);
            ItemStack projectile = ItemStackTagHelper.parseStack(stackNbt);
            if (!projectile.isEmpty()) {
                result.add(projectile);
            }
        }

        return result;
    }

    private List<ItemStack> takeProjectiles(ItemStack itemStack, int count) {
        ListTag nbtList = getProjectilesNBT(itemStack);
        int size = Math.min(nbtList.size(), count);
        List<ItemStack> result = new ArrayList<>(size);

        for (int i = 0; i < size; ++i) {
            CompoundTag stackNbt = nbtList.getCompound(0);
            nbtList.remove(0);
            ItemStack projectile = ItemStackTagHelper.parseStack(stackNbt);
            if (!projectile.isEmpty()) {
                result.add(projectile);
            }
        }

        return result;
    }

    public boolean hasProjectiles(ItemStack stack, Item ammoItem) {
        return getProjectiles(stack).stream().anyMatch(s -> s.getItem() == ammoItem);
    }

    private SoundEvent getSoundEvent(float velocity) {
        if (velocity < 7) {
            return SoundEvents.CROSSBOW_QUICK_CHARGE_3.value();
        } else if (velocity < 15) {
            return SoundEvents.CROSSBOW_QUICK_CHARGE_2.value();
        } else if (velocity < 22) {
            return SoundEvents.CROSSBOW_QUICK_CHARGE_1.value();
        }

        return SoundEvents.CROSSBOW_LOADING_START.value();
    }

    public int getUseDuration(ItemStack itemStack) {
        return 37000;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return getUseDuration(stack);
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CROSSBOW;
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {
        return true;
    }

    private boolean reload(LivingEntity entity, ItemStack crossbowStack) {
        int count = Math.max(getEffectLevel(crossbowStack, ItemEffect.ammoCapacity), 1);
        boolean infinite = CastOptional.cast(entity, Player.class)
                .map(player -> player.getAbilities().instabuild)
                .orElse(false);

        // todo: this has to be improved
        ItemStack ammoStack = ItemStack.EMPTY;

        for (int i = 0; i < count; i++) {
            if (ammoStack.isEmpty()) {
                ammoStack = findAmmo(entity);
            }

            if (ammoStack.isEmpty() && infinite) {
                ammoStack = new ItemStack(Items.ARROW);
            }

            if (!loadProjectiles(entity, crossbowStack, ammoStack, infinite && ammoStack.getItem() instanceof ArrowItem)) {
                return i > 0;
            }
        }

        return true;
    }

    private boolean loadProjectiles(LivingEntity entity, ItemStack crossbowStack, ItemStack ammoStack, boolean infiniteAmmo) {
        if (ammoStack.isEmpty()) {
            return false;
        } else {
            ItemStack itemstack;
            if (!infiniteAmmo) {
                itemstack = ammoStack.split(1);
                if (ammoStack.isEmpty() && entity instanceof Player player) {
                    player.getInventory().removeItem(ammoStack);
                }
            } else {
                itemstack = ammoStack.copy();
            }

            writeProjectile(crossbowStack, itemstack);
            return true;
        }
    }
}
