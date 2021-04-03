package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.Optional;
import java.util.UUID;

public class ArmorPenetrationEffect {

    private static final UUID uuid = UUID.fromString("a43e0407-f070-4e2f-8813-a5e16328f1a5");

    public static void onLivingHurt(LivingHurtEvent event, int effectLevel) {
        Optional.of(event.getEntityLiving())
                .map(LivingEntity::getAttributeManager)
                .filter(manager -> manager.hasAttributeInstance(Attributes.ARMOR))
                .map(manager -> manager.createInstanceIfAbsent(Attributes.ARMOR))
                .ifPresent(instance -> instance.applyNonPersistentModifier(
                        new AttributeModifier(uuid, "tetra_armor_pen", effectLevel * -0.01, AttributeModifier.Operation.MULTIPLY_TOTAL)));
    }

    public static void onLivingDamage(LivingDamageEvent event) {
        Optional.of(event.getEntityLiving())
                .map(LivingEntity::getAttributeManager)
                .filter(manager -> manager.hasAttributeInstance(Attributes.ARMOR))
                .map(manager -> manager.createInstanceIfAbsent(Attributes.ARMOR))
                .ifPresent(instance -> instance.removeModifier(uuid));
    }
}
