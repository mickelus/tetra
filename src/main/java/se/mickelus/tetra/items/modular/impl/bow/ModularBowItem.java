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
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.modular.ItemModular;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
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

public class ModularBowItem extends ItemModular {

    public final static String staveKey = "bow/stave";
    public final static String stringKey = "bow/string";
    public final static String riserKey = "bow/riser";

    public static final String unlocalizedName = "modular_bow";

    protected ModuleModel arrowModel0 = new ModuleModel("draw_0", new ResourceLocation(TetraMod.MOD_ID, "items/module/bow/arrow_0"));
    protected ModuleModel arrowModel1 = new ModuleModel("draw_1", new ResourceLocation(TetraMod.MOD_ID, "items/module/bow/arrow_1"));
    protected ModuleModel arrowModel2 = new ModuleModel("draw_2", new ResourceLocation(TetraMod.MOD_ID, "items/module/bow/arrow_2"));

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ModularBowItem instance;

    public ModularBowItem() {
        super(new Properties().maxStackSize(1));
        setRegistryName(unlocalizedName);

        majorModuleKeys = new String[] { stringKey, staveKey };
        minorModuleKeys = new String[] { riserKey };

        requiredModules = new String[] { stringKey, staveKey };

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
    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    public void onPlayerStoppedUsing(ItemStack stack, World world, LivingEntity entity, int timeLeft) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            boolean playerInfinite = player.abilities.isCreativeMode
                    || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
            ItemStack ammoStack = player.findAmmo(Items.BOW.getDefaultInstance());

            int drawProgress = getUseDuration(stack) - timeLeft;
            drawProgress = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, world, player, drawProgress,
                    !ammoStack.isEmpty() || playerInfinite);

            if (drawProgress < 0) {
                return;
            }

            if (!ammoStack.isEmpty() || playerInfinite) {
                if (ammoStack.isEmpty()) {
                    ammoStack = new ItemStack(Items.ARROW);
                }

                float projectileVelocity = getArrowVelocity(drawProgress);
                if (projectileVelocity > 0.1f) {
                    ArrowItem ammoItem = CastOptional.cast(ammoStack.getItem(), ArrowItem.class)
                            .orElse((ArrowItem) Items.ARROW);

                    boolean infiniteAmmo = player.abilities.isCreativeMode || ammoItem.isInfinite(ammoStack, stack, player);

                    if (!world.isRemote) {
                        AbstractArrowEntity projectile = ammoItem.createArrow(world, ammoStack, player);
                        projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F,
                                projectileVelocity * 3.0F, 1.0F);

                        if (projectileVelocity == 1.0F) {
                            projectile.setIsCritical(true);
                        }

                        projectile.setDamage(projectile.getDamage() + getDamageModifier(stack));


                        int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);
                        if (punchLevel > 0) {
                            projectile.setKnockbackStrength(punchLevel);
                        }

                        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0) {
                            projectile.setFire(100);
                        }

                        stack.damageItem(1, player, (p_220009_1_) -> {
                            p_220009_1_.sendBreakAnimation(player.getActiveHand());
                        });
                        if (infiniteAmmo || player.abilities.isCreativeMode
                                && (ammoStack.getItem() == Items.SPECTRAL_ARROW || ammoStack.getItem() == Items.TIPPED_ARROW)) {
                            projectile.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                        }

                        world.addEntity(projectile);
                    }

                    world.playSound(null, player.posX, player.posY, player.posZ,
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
    public static float getArrowVelocity(int charge) {
        float f = (float)charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    public int getDrawDuration(ItemStack itemStack) {
        return (int)(20 * getSpeedModifier(itemStack));
    }

    public float getProgress(ItemStack itemStack, @Nullable LivingEntity entity) {
        return Optional.ofNullable(entity)
                .filter(e -> e.getItemInUseCount() > 0)
                .filter(e -> itemStack.equals(e.getActiveItemStack()))
                .map( e -> (getUseDuration(itemStack) - e.getItemInUseCount()) * 1f / getDrawDuration(itemStack))
                .orElse(0f);
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
        boolean hasAmmo = !player.findAmmo(Items.BOW.getDefaultInstance()).isEmpty();

        ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(bowStack, world, player, hand, hasAmmo);
        if (ret != null) return ret;

        if (!player.abilities.isCreativeMode && !hasAmmo) {
            return hasAmmo ? new ActionResult<>(ActionResultType.PASS, bowStack) : new ActionResult<>(ActionResultType.FAIL, bowStack);
        } else {
            player.setActiveHand(hand);
            return new ActionResult<>(ActionResultType.SUCCESS, bowStack);
        }
    }

    private String getDrawVariant(ItemStack itemStack, @Nullable LivingEntity entity) {
        float progress = getProgress(itemStack, entity);

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
