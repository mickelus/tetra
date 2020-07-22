package se.mickelus.tetra.items.modular.impl.bow;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.SchemaRegistry;
import se.mickelus.tetra.module.data.ModuleModel;
import se.mickelus.tetra.module.schema.RemoveSchema;
import se.mickelus.tetra.module.schema.RepairSchema;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModularBowItem extends ModularItem {

    public final static String staveKey = "bow/stave";
    public final static String stringKey = "bow/string";
    public final static String riserKey = "bow/riser";

    public static final String unlocalizedName = "modular_bow";

    protected ModuleModel arrowModel0 = new ModuleModel("draw_0", new ResourceLocation(TetraMod.MOD_ID, "items/module/bow/arrow_0"));
    protected ModuleModel arrowModel1 = new ModuleModel("draw_1", new ResourceLocation(TetraMod.MOD_ID, "items/module/bow/arrow_1"));
    protected ModuleModel arrowModel2 = new ModuleModel("draw_2", new ResourceLocation(TetraMod.MOD_ID, "items/module/bow/arrow_2"));

    protected ItemStack vanillaBow;

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ModularBowItem instance;

    public ModularBowItem() {
        super(new Properties().maxStackSize(1));
        setRegistryName(unlocalizedName);

        majorModuleKeys = new String[] { stringKey, staveKey };
        minorModuleKeys = new String[] { riserKey };

        requiredModules = new String[] { stringKey, staveKey };

        vanillaBow = new ItemStack(Items.BOW);

        updateConfig(ConfigHandler.honeBowBase.get(), ConfigHandler.honeBowIntegrityMultiplier.get());

        SchemaRegistry.instance.registerSchema(new RepairSchema(this));
        RemoveSchema.registerRemoveSchemas(this);
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }

    @Override
    public void clientInit() {
        super.clientInit();
        MinecraftForge.EVENT_BUS.register(new RangedProgressOverlay(Minecraft.getInstance()));
        MinecraftForge.EVENT_BUS.register(new RangedFOVTransformer());
    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    public void onPlayerStoppedUsing(ItemStack itemStack, World world, LivingEntity entity, int timeLeft) {
        fireArrow(itemStack, world, entity, timeLeft);
    }

    @Override
    public void onUsingTick(ItemStack itemStack, LivingEntity player, int count) {
        if (getEffectLevel(itemStack, ItemEffect.releaseLatch) > 0 && getProgress(itemStack, player) >= 1) {
            player.stopActiveHand();
        }
    }

    protected void fireArrow(ItemStack itemStack, World world, LivingEntity entity, int timeLeft) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            boolean playerInfinite = player.abilities.isCreativeMode
                    || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, itemStack) > 0;
            ItemStack ammoStack = player.findAmmo(vanillaBow);

            // multiply by 20 to align progress with vanilla bow (fully drawn at 1sec/20ticks)
            int drawProgress = Math.round(getProgress(itemStack, entity) * 20);
            drawProgress = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(itemStack, world, player, drawProgress,
                    !ammoStack.isEmpty() || playerInfinite);

            if (drawProgress < 0) {
                return;
            }

            if (!ammoStack.isEmpty() || playerInfinite) {
                if (ammoStack.isEmpty()) {
                    ammoStack = new ItemStack(Items.ARROW);
                }

                float projectileVelocity = getArrowVelocity(drawProgress, getOverbowCap(itemStack), (float) getOverbowRate(itemStack));
                if (projectileVelocity > 0.1f) {
                    ArrowItem ammoItem = CastOptional.cast(ammoStack.getItem(), ArrowItem.class)
                            .orElse((ArrowItem) Items.ARROW);

                    boolean infiniteAmmo = player.abilities.isCreativeMode || ammoItem.isInfinite(ammoStack, itemStack, player);

                    if (!world.isRemote) {
                        AbstractArrowEntity projectile = ammoItem.createArrow(world, ammoStack, player);
                        projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F,
                                projectileVelocity * 3.0F, 1.0F);

                        if (projectileVelocity == 1.0F) {
                            projectile.setIsCritical(true);
                        }

                        // the damage modifier is based on fully drawn damage, vanilla bows deal 5 times the base damage when fully drawn
                        projectile.setDamage(projectile.getDamage() + getDamageModifier(itemStack) / 5 - 2);

                        int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, itemStack);
                        if (powerLevel > 0) {
                            projectile.setDamage(projectile.getDamage() + powerLevel * 0.5D + 0.5D);
                        }

                        int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, itemStack);
                        if (punchLevel > 0) {
                            projectile.setKnockbackStrength(punchLevel);
                        }

                        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, itemStack) > 0) {
                            projectile.setFire(100);
                        }

                        int piercingLevel = getEffectLevel(itemStack, ItemEffect.piercing);
                        if (piercingLevel > 0) {
                            projectile.setPierceLevel((byte) piercingLevel);
                        }

                        itemStack.damageItem(1, player, (p_220009_1_) -> {
                            p_220009_1_.sendBreakAnimation(player.getActiveHand());
                        });
                        if (infiniteAmmo || player.abilities.isCreativeMode
                                && (ammoStack.getItem() == Items.SPECTRAL_ARROW || ammoStack.getItem() == Items.TIPPED_ARROW)) {
                            projectile.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                        }

                        world.addEntity(projectile);

                        applyUsageEffects(entity, itemStack, 1);
                    }

                    world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(),
                            SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS,
                            1.0F, 1.0F / (random.nextFloat() * 0.4F + 1.2F) + projectileVelocity * 0.5F);

                    if (!infiniteAmmo && !player.abilities.isCreativeMode) {
                        ammoStack.shrink(1);
                        if (ammoStack.isEmpty()) {
                            player.inventory.deleteStack(ammoStack);
                        }
                    }

                    player.addStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    public double getDamageModifier(ItemStack itemStack) {
        if (isBroken(itemStack)) {
            return 0;
        }

        double damageModifier = getAllModules(itemStack).stream()
                .mapToDouble(itemModule -> itemModule.getDamageModifier(itemStack))
                .sum();

        damageModifier = Arrays.stream(getSynergyData(itemStack))
                .mapToDouble(synergyData -> synergyData.damage)
                .reduce(damageModifier, Double::sum);

        damageModifier = Arrays.stream(getSynergyData(itemStack))
                .mapToDouble(synergyData -> synergyData.damageMultiplier)
                .reduce(damageModifier, (a, b) -> a * b);

        return getAllModules(itemStack).stream()
                .map(itemModule -> itemModule.getDamageMultiplierModifier(itemStack))
                .reduce(damageModifier, (a, b) -> a * b);
    }

    public double getSpeedModifier(ItemStack itemStack) {
        double speedModifier = getAllModules(itemStack).stream()
                .map(itemModule -> itemModule.getSpeedModifier(itemStack))
                .reduce(0d, Double::sum);

        speedModifier = Arrays.stream(getSynergyData(itemStack))
                .mapToDouble(synergyData -> synergyData.attackSpeed)
                .reduce(speedModifier, Double::sum);

        speedModifier = Arrays.stream(getSynergyData(itemStack))
                .mapToDouble(synergyData -> synergyData.attackSpeedMultiplier)
                .reduce(speedModifier, (a, b) -> a * b);

        speedModifier = getAllModules(itemStack).stream()
                .map(itemModule -> itemModule.getSpeedMultiplierModifier(itemStack))
                .reduce(speedModifier, (a, b) -> a * b);

        return Math.max(0.001, speedModifier);
    }

    /**
     * Gets the velocity of the arrow entity from the bow's charge
     */
    public static float getArrowVelocity(int charge, float overbowCap, float overbowRate) {
        float f = (float)charge / 20.0F;

        f = (f * f + f * 2.0F) / 3.0F;


        if (overbowCap > 0 && f > 1) {
            f = getProgressOverbowed(f, overbowCap, overbowRate);
        } else if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    public int getDrawDuration(ItemStack itemStack) {
        return (int)(20 * getSpeedModifier(itemStack));
    }

    /**
     * Returns a value between 0 - 1 representing how far the bow has been drawn, a value of 1 means that the bow is fully drawn
     * @param itemStack
     * @param entity
     * @return
     */
    public float getProgress(ItemStack itemStack, @Nullable LivingEntity entity) {
        return Optional.ofNullable(entity)
                .filter(e -> e.getItemInUseCount() > 0)
                .filter(e -> itemStack.equals(e.getActiveItemStack()))
                .map( e -> (getUseDuration(itemStack) - e.getItemInUseCount()) * 1f / getDrawDuration(itemStack))
                .orElse(0f);
    }

    public float getOverbowCap(ItemStack itemStack) {
        return 1 / 100f * getEffectLevel(itemStack, ItemEffect.overbowed);
    }

    public double getOverbowRate(ItemStack itemStack) {
        return getEffectEfficiency(itemStack, ItemEffect.overbowed);
    }

    public static float getProgressOverbowed(float drawProgress, float overbowCap, float overbowRate) {
        return drawProgress > 1 ? 1 - MathHelper.clamp((drawProgress - 1) * overbowRate, 0, overbowCap) : drawProgress;
    }

    /**
     * How long it takes to use or consume an item
     */
    @Override
    public int getUseDuration(ItemStack stack) {
        return 37000;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    /**
     * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
     * {@link #onItemUse}.
     */
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack bowStack = player.getHeldItem(hand);
        boolean hasAmmo = !player.findAmmo(vanillaBow).isEmpty();

        ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(bowStack, world, player, hand, hasAmmo);
        if (ret != null) return ret;

        if (!player.abilities.isCreativeMode && !hasAmmo) {
            return ActionResult.resultFail(bowStack);
        } else {
            player.setActiveHand(hand);
            return ActionResult.resultConsume(bowStack);
        }
    }

    private String getDrawVariant(ItemStack itemStack, @Nullable LivingEntity entity) {
        float progress = getProgressOverbowed(getProgress(itemStack, entity), getOverbowCap(itemStack), (float) getOverbowRate(itemStack));

        if (progress == 0) {
            return "item";
        } else if (progress < 0.65) {
            return "draw_0";
        } else if (progress < 0.9) {
            return "draw_1";
        }
        return "draw_2";
    }

    private ModuleModel getArrowModel(String drawVariant) {
        switch (drawVariant) {
            case "draw_0":
                return arrowModel0;
            case "draw_1":
                return arrowModel1;
            case "draw_2":
                return arrowModel2;
            default:
                return arrowModel0;
        }
    }

    @Override
    public String getModelCacheKey(ItemStack itemStack, LivingEntity entity) {
        return super.getModelCacheKey(itemStack, entity) + ":" +  getDrawVariant(itemStack, entity);
    }

    @Override
    public ImmutableList<ModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity) {
        String modelType = getDrawVariant(itemStack, entity);

        ImmutableList<ModuleModel> models = getAllModules(itemStack).stream()
                .sorted(Comparator.comparing(ItemModule::getRenderLayer))
                .flatMap(itemModule -> Arrays.stream(itemModule.getModels(itemStack)))
                .filter(Objects::nonNull)
                .filter(model -> model.type.equals(modelType) || model.type.equals("static"))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        if (!modelType.equals("item")) {
            return ImmutableList.<ModuleModel>builder()
                    .addAll(models)
                    .add(getArrowModel(modelType))
                    .build();
        }

        return models;
    }
}
