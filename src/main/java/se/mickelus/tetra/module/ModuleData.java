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

	public ResourceLocation glyphLocation = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/glyphs.png");
	public int glyphTint;
	public int glyphTextureX;
	public int glyphTextureY;

	public ModuleData(final String key, final String material, final int durability, final float durabilityMultiplier,
			final int integrity, final float integrityMultiplier, final int glyphTint, final int glyphTextureX,
			final int glyphTextureY) {
		this(key, material, durability, integrity, glyphTint, glyphTextureX, glyphTextureY);
		this.durabilityMultiplier = durabilityMultiplier;
		this.integrityMultiplier = integrityMultiplier;
	}

	public ModuleData(final String key, final String material, final int durability, final int integrity,
			final int glyphTint, final int glyphTextureX, final int glyphTextureY) {

		this.key = key;
		this.material = material;
		this.durability = durability;
		this.integrity = integrity;

		this.glyphTint = glyphTint;
		this.glyphTextureX = glyphTextureX;
		this.glyphTextureY = glyphTextureY;

		textureLocation = new ResourceLocation(TetraMod.MOD_ID, "items/" + key);
	}
}
