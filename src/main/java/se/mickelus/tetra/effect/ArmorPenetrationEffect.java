package se.mickelus.tetra.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import se.mickelus.tetra.TetraMod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class ArmorPenetrationEffect {

    private static final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "armor_pen");

    /**
     * Applies the armor reduction effect before the damage value is calculated.
     * Note that some mods cause this to be called twice before onLivingDamage.
     *
     * @param event
     * @param effectLevel
     */
    public static void onLivingHurt(LivingIncomingDamageEvent event, int effectLevel) {
        Optional.of(event.getEntity())
                .map(LivingEntity::getAttributes)
                .filter(manager -> manager.hasAttribute(Attributes.ARMOR))
                .map(manager -> manager.getInstance(Attributes.ARMOR))
                .filter(instance -> instance.getModifier(id) == null)
                .ifPresent(instance -> instance.addTransientModifier(
                        new AttributeModifier(id, effectLevel * -0.01, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)));
    }

    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        Optional.of(event.getEntity())
                .map(LivingEntity::getAttributes)
                .filter(manager -> manager.hasAttribute(Attributes.ARMOR))
                .map(manager -> manager.getInstance(Attributes.ARMOR))
                .ifPresent(instance -> instance.removeModifier(id));
    }
}
