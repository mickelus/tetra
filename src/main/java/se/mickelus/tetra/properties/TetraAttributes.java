package se.mickelus.tetra.properties;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import se.mickelus.tetra.TetraMod;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TetraAttributes {
    public static final DeferredRegister<Attribute> registry = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, TetraMod.MOD_ID);

    public static RegistryObject<Attribute> drawStrength = RegistryObject.of(registry.register("draw_strength",
            () -> new RangedAttribute("tetra.attribute.draw_strength.name", 0, 0, 2048)));
    public static RegistryObject<Attribute> drawSpeed = RegistryObject.of(registry.register("draw_speed",
            () -> new RangedAttribute("tetra.attribute.draw_speed.name", 0, 0, 2048)));

    public static RegistryObject<Attribute> abilityCooldown = RegistryObject.of(registry.register("ability_cooldown",
            () -> new RangedAttribute("tetra.attribute.ability_cooldown.name", 0, 0, 2048)));
    public static RegistryObject<Attribute> abilityDamage = RegistryObject.of(registry.register("ability_damage",
            () -> new RangedAttribute("tetra.attribute.ability_damage.name", 0, 0, 2048)));

    public static void onEntityAttributeModification(final EntityAttributeModificationEvent event) {
        registry.getEntries().forEach(attribute -> event.add(EntityType.PLAYER, attribute, attribute.value().getDefaultValue()));
    }
}
