package se.mickelus.tetra.items.modular;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.compat.botania.ManaRepair;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.*;
import se.mickelus.tetra.properties.IToolProvider;
import se.mickelus.tetra.items.TetraItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.minecraft.item.Item.Properties;

public abstract class ModularItem extends TetraItem implements IModularItem, IToolProvider {
    private static final Logger logger = LogManager.getLogger();

    protected int honeBase = 450;
    protected int honeIntegrityMultiplier = 200;

    // static marker for item, denoting if it can progress towards being honed
    protected boolean canHone = true;

    protected String[] majorModuleKeys;
    protected String[] minorModuleKeys;

    protected String[] requiredModules = new String[0];

    protected int baseDurability = 0;
    protected int baseIntegrity = 0;

    protected SynergyData[] synergies = new SynergyData[0];

    public static final UUID attackDamageModifier = Item.BASE_ATTACK_DAMAGE_UUID;
    public static final UUID attackSpeedModifier = Item.BASE_ATTACK_SPEED_UUID;

    private Cache<String, Multimap<Attribute, AttributeModifier>> attributeCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private Cache<String, ToolData> toolCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private Cache<String, EffectData> effectCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private Cache<String, ItemProperties> propertyCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public ModularItem(Properties properties) {
        super(properties);

        DataManager.moduleData.onReload(this::clearCaches);
    }

    public void clearCaches() {
        logger.debug("Clearing item data caches for {}...", getRegistryName());
        attributeCache.invalidateAll();
        toolCache.invalidateAll();
        effectCache.invalidateAll();
        propertyCache.invalidateAll();
    }

    @Override
    public String[] getMajorModuleKeys() {
        return majorModuleKeys;
    }

    @Override
    public String[] getMinorModuleKeys() {
        return minorModuleKeys;
    }

    @Override
    public String[] getRequiredModules() {
        return requiredModules;
    }

    @Override
    public int getHoneBase() {
        return honeBase;
    }

    @Override
    public int getHoneIntegrityMultiplier() {
        return honeIntegrityMultiplier;
    }

    @Override
    public boolean canGainHoneProgress() {
        return canHone;
    }

    @Override
    public Cache<String, Multimap<Attribute, AttributeModifier>> getAttributeModifierCache() {
        return attributeCache;
    }

    @Override
    public Cache<String, EffectData> getEffectDataCache() {
        return effectCache;
    }

    @Override
    public Cache<String, ItemProperties> getPropertyCache() {
        return propertyCache;
    }

    public Cache<String, ToolData> getToolDataCache() {
        return toolCache;
    }

    @Override
    public Item getItem() {
        return this;
    }

    @Override
    public boolean canProvideTools(ItemStack itemStack) {
        return !isBroken(itemStack);
    }

    @Override
    public ToolData getToolData(ItemStack itemStack) {
        try {
            return getToolDataCache().get(getDataCacheKey(itemStack),
                    () -> Optional.ofNullable(getToolDataRaw(itemStack)).orElseGet(ToolData::new));
        } catch (ExecutionException e) {
            e.printStackTrace();
            return Optional.ofNullable(getToolDataRaw(itemStack)).orElseGet(ToolData::new);
        }
    }

    /**
     * Get uncached tool data, this is not needed in most cases.
     * @param itemStack
     * @return
     */
    protected ToolData getToolDataRaw(ItemStack itemStack) {
        logger.debug("Gathering tool data for {} ({})", getName(itemStack).getString(), getDataCacheKey(itemStack));
        return Stream.concat(
                getAllModules(itemStack).stream()
                        .map(module -> module.getToolData(itemStack)),
                Arrays.stream(getSynergyData(itemStack))
                        .map(synergy -> synergy.tools))
                .filter(Objects::nonNull)
                .reduce(null, ToolData::merge);
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        return new StringTextComponent(getItemName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.addAll(getTooltip(stack, world, flag));
    }

    @Override
    public void inventoryTick(ItemStack itemStack, World world, Entity entity, int itemSlot, boolean isSelected) {
        ManaRepair.itemInventoryTick(itemStack, world, entity);
    }

    @Override
    public int getMaxDamage(ItemStack itemStack) {
        return Optional.of(getPropertiesCached(itemStack))
                .map(properties -> (properties.durability + baseDurability) * properties.durabilityMultiplier)
                .map(Math::round)
                .orElse(0);
    }

    @Override
    public void setDamage(ItemStack itemStack, int damage) {
        super.setDamage(itemStack, Math.min(itemStack.getMaxDamage() - 1, damage));
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        return Math.min(stack.getMaxDamage() - stack.getDamageValue() - 1, amount);
    }

    @Override
    public void onCraftedBy(ItemStack itemStack, World world, PlayerEntity player) {
        IModularItem.updateIdentifier(itemStack);
    }

    /**
     * Vanilla method for determining if the item should display the enchantment glint
     * @param itemStack The itemstack for the item
     * @return true if should display glint
     */
    @Override
    public boolean isFoil(@Nonnull ItemStack itemStack) {
        if (ConfigHandler.enableGlint.get()) {
            return Arrays.stream(getImprovements(itemStack))
                    .anyMatch(improvement -> improvement.enchantment);
        }

        return false;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public SynergyData[] getAllSynergyData(ItemStack itemStack) {
        return synergies;
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return canEnchantInEnchantingTable(itemStack);
    }

    @Override
    public boolean isBookEnchantable(final ItemStack itemStack, final ItemStack bookStack) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack itemStack, Enchantment enchantment) {
        return acceptsEnchantment(itemStack, enchantment);
    }

    @Override
    public int getItemEnchantability(ItemStack itemStack) {
        return getEnchantability(itemStack);
    }
}
