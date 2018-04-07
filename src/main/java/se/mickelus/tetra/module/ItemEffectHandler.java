package se.mickelus.tetra.module;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import se.mickelus.tetra.items.ItemEffect;
import se.mickelus.tetra.items.ItemModular;

import java.util.Optional;
import java.util.stream.Stream;

public class ItemEffectHandler {
    public static ItemEffectHandler instance;

    public ItemEffectHandler() {
        instance = this;
    }

    private int getEffectLevel(ItemStack itemStack, ItemEffect effect) {
        ItemModular item = (ItemModular) itemStack.getItem();
        return item.getEffectLevel(itemStack, effect);
    }

    @SubscribeEvent
    public void onLootingLevel(LootingLevelEvent event) {
        Optional.ofNullable(event.getDamageSource().getTrueSource())
                .filter(entity -> entity instanceof EntityPlayer)
                .map(entity -> (EntityLivingBase) entity)
                .map(EntityLivingBase::getHeldItemMainhand)
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .ifPresent(itemStack -> {
                    event.setLootingLevel(getEffectLevel(itemStack, ItemEffect.looting) + event.getLootingLevel());
                });
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        Optional.ofNullable(event.getSource().getTrueSource())
                .filter(entity -> entity instanceof EntityPlayer)
                .map(entity -> (EntityLivingBase) entity)
                .map(EntityLivingBase::getHeldItemMainhand)
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .ifPresent(itemStack -> {
                    if (EnumCreatureAttribute.UNDEAD.equals(event.getEntityLiving().getCreatureAttribute())) {
                        event.setAmount(event.getAmount() + getEffectLevel(itemStack, ItemEffect.smite) * 2.5f);
                    }

                    if (EnumCreatureAttribute.ARTHROPOD.equals(event.getEntityLiving().getCreatureAttribute())) {
                        event.setAmount(event.getAmount() + getEffectLevel(itemStack, ItemEffect.arthropod) * 2.5f);
                    }
                });
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        Optional.ofNullable(event.getSource().getTrueSource())
                .filter(entity -> entity instanceof EntityPlayer)
                .map(entity -> (EntityLivingBase) entity)
                .map(EntityLivingBase::getHeldItemMainhand)
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .ifPresent(itemStack -> {
                    int penetratingLevel = getEffectLevel(itemStack, ItemEffect.armorPenetration);
                    if (penetratingLevel > 0 && event.getAmount() < penetratingLevel * 2) {
                        event.setAmount(penetratingLevel * 2);
                    }
                });
    }

    @SubscribeEvent
    public void onCriticalHit(CriticalHitEvent event) {
        Optional.ofNullable(event.getEntityLiving())
                .filter(entity -> entity instanceof EntityPlayer)
                .map(entity -> (EntityLivingBase) entity)
                .map(EntityLivingBase::getHeldItemMainhand)
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .ifPresent(itemStack -> {
                    int backstabLevel = getEffectLevel(itemStack, ItemEffect.backstab);
                    if (backstabLevel > 0 && event.getTarget() instanceof EntityLivingBase) {
                        EntityLivingBase attacker = event.getEntityLiving();
                        EntityLivingBase target = (EntityLivingBase) event.getTarget();
                        if (180 - Math.abs(Math.abs(attacker.rotationYawHead - target.rotationYawHead) % 360 - 180) < 60) {
                            event.setDamageModifier(Math.max(1.25f + 0.25f * backstabLevel, event.getDamageModifier()));
                            event.setResult(Event.Result.ALLOW);
                        }
                    }
                });
    }

    @SubscribeEvent
    public void onPlayerPickupXp(PlayerPickupXpEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        Stream.concat(
                player.inventory.mainInventory.stream(),
                player.inventory.offHandInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .filter(ItemStack::isItemDamaged)
                .filter(itemStack -> getEffectLevel(itemStack, ItemEffect.mending) > 0)
                .findAny()
                .ifPresent(itemStack -> {
                    EntityXPOrb orb = event.getOrb();
                    int i = Math.min(orb.xpValue * 2, itemStack.getItemDamage());
                    orb.xpValue -= i * 2;
                    itemStack.setItemDamage(itemStack.getItemDamage() - i);

                    if (orb.xpValue == 0) {
                        player.xpCooldown = 2;
                        player.onItemPickup(event.getOrb(), 1);
                        orb.setDead();
                        event.setCanceled(true);
                    }
                });
    }
}
