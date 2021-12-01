package se.mickelus.tetra.mixin;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(at = @At("HEAD"), method = "addEnchantment(Lnet/minecraft/enchantment/Enchantment;I)V", cancellable = true)
    private void addEnchantment(Enchantment enchantment, int level, CallbackInfo callback) {
        if (getItem() instanceof IModularItem) {
            ItemStack itemStack = getInstance();
            IModularItem item = (IModularItem) getItem();
            ItemUpgradeRegistry.applyEnchantment(item, itemStack, enchantment, level);
            item.assemble(itemStack, null, 0);

            callback.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "isEnchanted()Z", cancellable = true)
    private void isEnchanted(CallbackInfoReturnable<Boolean> callback) {
        if (getItem() instanceof IModularItem) {
            ItemStack itemStack = getInstance();
            callback.setReturnValue(itemStack.hasTag()
                    && (itemStack.getTag().contains("Enchantments", 9) || ((IModularItem) getItem()).hasEnchantments(itemStack)));

            callback.cancel();
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
