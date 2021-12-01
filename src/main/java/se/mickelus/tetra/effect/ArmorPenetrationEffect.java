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

    /**
     * Applies the armor reduction effect before the damage value is calculated.
     * Note that some mods cause this to be called twice before onLivingDamage.
     * @param event
     * @param effectLevel
     */
    public static void onLivingHurt(LivingHurtEvent event, int effectLevel) {
        Optional.of(event.getEntityLiving())
                .map(LivingEntity::getAttributes)
                .filter(manager -> manager.hasAttribute(Attributes.ARMOR))
                .map(manager -> manager.getInstance(Attributes.ARMOR))
                .filter(instance -> instance.getModifier(uuid) == null)
                .ifPresent(instance -> instance.addTransientModifier(
                        new AttributeModifier(uuid, "tetra_armor_pen", effectLevel * -0.01, AttributeModifier.Operation.MULTIPLY_TOTAL)));
    }

    public static void onLivingDamage(LivingDamageEvent event) {
        Optional.of(event.getEntityLiving())
                .map(LivingEntity::getAttributes)
                .filter(manager -> manager.hasAttribute(Attributes.ARMOR))
                .map(manager -> manager.getInstance(Attributes.ARMOR))
                .ifPresent(instance -> instance.removeModifier(uuid));
    }
}
