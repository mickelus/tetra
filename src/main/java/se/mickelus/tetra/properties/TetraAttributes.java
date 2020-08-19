package se.mickelus.tetra.properties;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import se.mickelus.tetra.TetraMod;

public class TetraAttributes {
    public static final DeferredRegister<Attribute> registry = DeferredRegister.create(Attribute.class, TetraMod.MOD_ID);

    public static RegistryObject<Attribute> damage = registry.register("damage", () -> new RangedAttribute("tetra.damage", 0, 0, 2048));
    public static RegistryObject<Attribute> speed = registry.register("speed", () -> new RangedAttribute("tetra.speed", 0, 0, 2048));
}
