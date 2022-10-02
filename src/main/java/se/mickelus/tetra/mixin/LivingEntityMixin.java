package se.mickelus.tetra.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.effect.FocusEffect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(at = @At("HEAD"), method = "increaseAirSupply(I)I", cancellable = true)
    public void increaseAirSupply(int amount, CallbackInfoReturnable<Integer> ci) {
        if (getInstance().isCrouching() && FocusEffect.hasApplicableItem(getInstance())) {
            ci.setReturnValue(getInstance().getAirSupply());
        }
    }

    private LivingEntity getInstance() {
        return ((LivingEntity) (Object) this);
    }
}
