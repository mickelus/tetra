package se.mickelus.tetra.effect.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

@OnlyIn(Dist.CLIENT)
public class EffectUnRenderer implements IClientMobEffectExtensions {
    public static final IClientMobEffectExtensions INSTANCE = new EffectUnRenderer();

    @Override
    public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, PoseStack poseStack, int x, int y, int blitOffset) {
        return true;
    }

    @Override
    public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, PoseStack poseStack, int x, int y, float z, float alpha) {
        return true;
    }

    @Override
    public boolean isVisibleInGui(MobEffectInstance instance) {
        return false;
    }

    @Override
    public boolean isVisibleInInventory(MobEffectInstance instance) {
        return false;
    }
}
