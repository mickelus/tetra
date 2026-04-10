package net.minecraftforge.common;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.Supplier;

public final class ForgeMod {
    public static final Supplier<Attribute> BLOCK_REACH = () -> Attributes.BLOCK_INTERACTION_RANGE.value();
    public static final Supplier<Attribute> ENTITY_REACH = () -> Attributes.ENTITY_INTERACTION_RANGE.value();

    private ForgeMod() {}
}
