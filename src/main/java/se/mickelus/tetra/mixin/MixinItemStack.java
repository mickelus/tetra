package se.mickelus.tetra.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.items.modular.IModularItem;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(at = @At("RETURN"), method = "addEnchantment(Lnet/minecraft/enchantment/Enchantment;I)V")
    private void addEnchantment(Enchantment enchantment, int level, CallbackInfo callback) {
        if (getItem() instanceof IModularItem) {
            ItemStack itemStack = getInstance();
            IModularItem item = (IModularItem) getItem();
            TetraEnchantmentHelper.mapEnchantments(itemStack);
            item.assemble(itemStack, null, 0);
        }
    }

    @Shadow
    public Item getItem() {
        throw new IllegalStateException("Mixin failed to shadow getItem()");
    }

    private ItemStack getInstance() {
        return ((ItemStack) (Object) this);
    }
}
