package se.mickelus.tetra.items.sword;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.UpgradeSchema;

public class SimpleBladeSchema implements UpgradeSchema {

	@Override
	public String getName() {
		return "Basic blade";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public int getNumMaterialSlots() {

		return 1;
	}

	@Override
	public String getSlotName(final int index) {

		return "Blade material";
	}

	@Override
	public boolean slotAcceptsStack(final int index, final ItemStack itemStack) {

		return true;
	}

	@Override
	public ItemStack applyUpgrade(final ItemStack itemStack, final ItemStack[] materials) {
		ItemStack upgradedStack = itemStack.copy();

		BladeModule.instance.addModule(upgradedStack, materials);

		return upgradedStack;
	}
}
