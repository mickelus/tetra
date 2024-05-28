package se.mickelus.tetra.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

public class ModularItemDamageEvent extends Event {
    private LivingEntity usingEntity;
    private ItemStack itemStack;
    private int originalAmount;
    private int amount;

    public ModularItemDamageEvent(@Nullable LivingEntity usingEntity, ItemStack itemStack, int amount) {
        this.usingEntity = usingEntity;
        this.itemStack = itemStack;
        this.originalAmount = amount;
        this.amount = amount;
    }

    @Nullable
    public LivingEntity getUsingEntity() {
        return usingEntity;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getOriginalAmount() {
        return originalAmount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
