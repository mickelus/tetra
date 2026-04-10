package net.minecraftforge.common.extensions;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;

public interface IForgeMenuType {
    static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory) {
        return IMenuTypeExtension.create(factory);
    }
}
