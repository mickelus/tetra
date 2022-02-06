package se.mickelus.tetra.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.items.modular.IModularItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {
    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;setEnchantments(Ljava/util/Map;Lnet/minecraft/world/item/ItemStack;)V")
    private static void setEnchantments(Map<Enchantment, Integer> enchantments, ItemStack itemStack, CallbackInfo ci) {
        if (itemStack.getItem() instanceof IModularItem item) {
            TetraEnchantmentHelper.mapEnchantments(itemStack);
            item.assemble(itemStack, null, 0);
        }
    }
}
