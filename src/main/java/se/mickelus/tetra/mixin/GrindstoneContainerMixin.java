package se.mickelus.tetra.mixin;

import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.items.modular.IModularItem;

// Cross-version compat: matches upstream 1.20's GrindstoneContainerMixin. The vanilla method was
// renamed from removeNonCurses(ItemStack, int, int) to removeNonCursesFrom(ItemStack) in 1.21,
// but the role (build the disenchanted result stack) is unchanged.
@Mixin(GrindstoneMenu.class)
public class GrindstoneContainerMixin {

    @Inject(at = @At("HEAD"), method = "removeNonCursesFrom", cancellable = true)
    private void removeEnchantments(ItemStack itemStack, CallbackInfoReturnable<ItemStack> callback) {
        if (itemStack.getItem() instanceof IModularItem) {
            ItemStack result = TetraEnchantmentHelper.removeAllEnchantments(itemStack.copy());
            callback.setReturnValue(result);
            callback.cancel();
        }
    }
}
