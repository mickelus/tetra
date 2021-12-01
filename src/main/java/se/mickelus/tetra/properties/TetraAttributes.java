package se.mickelus.tetra.properties;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import se.mickelus.tetra.TetraMod;

public class TetraAttributes {
    public static final DeferredRegister<Attribute> registry = DeferredRegister.create(Attribute.class, TetraMod.MOD_ID);

    public static RegistryObject<Attribute> drawStrength = registry.register("draw_strength", () -> new RangedAttribute("tetra.attribute.draw_strength.name", 0, 0, 2048));
    public static RegistryObject<Attribute> drawSpeed = registry.register("draw_speed", () -> new RangedAttribute("tetra.attribute.draw_speed.name", 0, 0, 2048));

    public static RegistryObject<Attribute> abilityCooldown = registry.register("ability_cooldown", () -> new RangedAttribute("tetra.attribute.ability_cooldown.name", 0, 0, 2048));
    public static RegistryObject<Attribute> abilityDamage = registry.register("ability_damage", () -> new RangedAttribute("tetra.attribute.ability_damage.name", 0, 0, 2048));
}
