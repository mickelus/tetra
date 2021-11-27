package se.mickelus.tetra.mixin;

import net.minecraft.inventory.container.GrindstoneContainer;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.Arrays;
import java.util.Objects;

@Mixin(GrindstoneContainer.class)
public class MixinGrindstoneContainer {

    @Inject(at = @At("HEAD"), method = "removeEnchantments", cancellable = true)
    private void removeEnchantments(ItemStack itemStack, int damage, int count, CallbackInfoReturnable<ItemStack> callback) {
        if (itemStack.getItem() instanceof IModularItem) {
            ItemStack result = IModularItem.removeAllEnchantments(itemStack.copy());

            callback.setReturnValue(result);
            callback.cancel();
        }
    }
}
