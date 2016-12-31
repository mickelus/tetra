package se.mickelus.tetra.blocks.geode;

import net.minecraftforge.fml.common.registry.GameRegistry;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.TetraItem;

public class ItemGeode extends TetraItem {

	public static ItemGeode instance;
	private final String unlocalizedName = "geode";

	public ItemGeode() {
		setUnlocalizedName(unlocalizedName);
		setRegistryName(unlocalizedName);
		GameRegistry.register(this);
		setCreativeTab(TetraCreativeTabs.getInstance());

		instance = this;
	}

}
