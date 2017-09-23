package se.mickelus.tetra.module;

import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;

public class ModuleData {
	public String key;
	public String material;

	public int durability = 0;
	public float durabilityMultiplier = 1;

	public int integrity = 0;
	public float integrityMultiplier = 1;

	public ResourceLocation textureLocation;

	public ModuleData(final String key, final String material, final int durability, final float durabilityMultiplier,
			final int integrity, final float integrityMultiplier) {
		this(key, material, durability, integrity);
		this.durabilityMultiplier = durabilityMultiplier;
		this.integrityMultiplier = integrityMultiplier;
	}

	public ModuleData(final String key, final String material, final int durability, final int integrity) {

		this.key = key;
		this.material = material;
		this.durability = durability;
		this.integrity = integrity;

		textureLocation = new ResourceLocation(TetraMod.MOD_ID, "items/" + key);
	}
}
