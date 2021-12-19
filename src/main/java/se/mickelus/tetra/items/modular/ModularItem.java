package se.mickelus.tetra.items.modular;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.compat.botania.ManaRepair;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ItemProperties;
import se.mickelus.tetra.module.data.SynergyData;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.properties.IToolProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class ModularItem extends TetraItem implements IModularItem, IToolProvider {
    public static final UUID attackDamageModifier = Item.BASE_ATTACK_DAMAGE_UUID;
    public static final UUID attackSpeedModifier = Item.BASE_ATTACK_SPEED_UUID;
    private static final Logger logger = LogManager.getLogger();
    private final Cache<String, Multimap<Attribute, AttributeModifier>> attributeCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    private final Cache<String, ToolData> toolCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    private final Cache<String, EffectData> effectCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    private final Cache<String, ItemProperties> propertyCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
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

    public ModularItem(Properties properties) {
        super(properties);

        DataManager.instance.moduleData.onReload(this::clearCaches);
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
     *
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
    public Component getName(ItemStack stack) {
        return new TextComponent(getItemName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.addAll(getTooltip(stack, world, flag));
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level world, Entity entity, int itemSlot, boolean isSelected) {
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
    public int getBarWidth(ItemStack itemStack) {
        return Math.round(13.0F - (float) itemStack.getDamageValue() * 13.0F / (float) getMaxDamage(itemStack));
    }

    @Override
    public int getBarColor(ItemStack itemStack) {
        float maxDamage = getMaxDamage(itemStack);
        float f = Math.max(0.0F, (maxDamage - itemStack.getDamageValue()) / maxDamage);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void onCraftedBy(ItemStack itemStack, Level world, Player player) {
        IModularItem.updateIdentifier(itemStack);
    }

    /**
     * Vanilla method for determining if the item should display the enchantment glint
     *
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
