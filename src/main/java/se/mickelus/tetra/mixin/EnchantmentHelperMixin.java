package se.mickelus.tetra.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.items.modular.IModularItem;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    // Cross-version compat: matches upstream 1.20's EnchantmentHelperMixin. Signature changed to
    // (ItemStack, ItemEnchantments) in 1.21 (was (Map<Enchantment, Integer>, ItemStack) on 1.20).
    @Inject(at = @At("RETURN"),
            method = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;setEnchantments(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/ItemEnchantments;)V")
    private static void setEnchantments(ItemStack itemStack, ItemEnchantments enchantments, CallbackInfo ci) {
        if (itemStack.getItem() instanceof IModularItem item) {
            TetraEnchantmentHelper.mapEnchantments(itemStack);
            item.assemble(itemStack, null, 0);
        }
    }
}
