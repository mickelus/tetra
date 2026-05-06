package se.mickelus.tetra;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import se.mickelus.mutil.effect.EffectTooltipRenderer;
import se.mickelus.tetra.blocks.forged.chthonic.ExtractorProjectileEntity;
import se.mickelus.tetra.blocks.forged.chthonic.ExtractorProjectileRenderer;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerBlockEntity;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerMenu;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerScreen;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerRenderer;
import se.mickelus.tetra.blocks.forged.extractor.CoreExtractorPistonBlockEntity;
import se.mickelus.tetra.blocks.forged.extractor.CoreExtractorPistonRenderer;
import se.mickelus.tetra.blocks.forged.hammer.HammerBaseBlockEntity;
import se.mickelus.tetra.blocks.forged.hammer.HammerBaseRenderer;
import se.mickelus.tetra.blocks.forged.hammer.HammerHeadBlockEntity;
import se.mickelus.tetra.blocks.forged.hammer.HammerHeadRenderer;
import se.mickelus.tetra.blocks.geode.particle.SparkleParticle;
import se.mickelus.tetra.blocks.geode.particle.SparkleParticleType;
import se.mickelus.tetra.blocks.holo.HolosphereBlockEntity;
import se.mickelus.tetra.blocks.holo.HolosphereEntityRenderer;
import se.mickelus.tetra.blocks.multischematic.MultiblockSchematicGui;
import se.mickelus.tetra.blocks.rack.RackTESR;
import se.mickelus.tetra.blocks.rack.RackTile;
import se.mickelus.tetra.blocks.scroll.ScrollRenderer;
import se.mickelus.tetra.blocks.scroll.ScrollItem;
import se.mickelus.tetra.blocks.scroll.ScrollItemColor;
import se.mickelus.tetra.blocks.scroll.ScrollTile;
import se.mickelus.tetra.blocks.workbench.WorkbenchContainer;
import se.mickelus.tetra.blocks.workbench.WorkbenchTESR;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchScreen;
import se.mickelus.tetra.client.ItemAbilityIconStore;
import se.mickelus.tetra.client.keymap.TetraKeyMappings;
import se.mickelus.tetra.client.model.ModularModelLoader;
import se.mickelus.tetra.client.particle.*;
import se.mickelus.tetra.effect.gui.EffectUnRenderer;
import se.mickelus.tetra.effect.howling.HowlingPotionEffect;
import se.mickelus.tetra.effect.potion.BleedingPotionEffect;
import se.mickelus.tetra.effect.potion.ExhaustedPotionEffect;
import se.mickelus.tetra.effect.potion.MiningSpeedPotionEffect;
import se.mickelus.tetra.effect.potion.PriedPotionEffect;
import se.mickelus.tetra.effect.potion.PuncturedPotionEffect;
import se.mickelus.tetra.effect.potion.SatiatedPotionEffect;
import se.mickelus.tetra.effect.potion.SeveredPotionEffect;
import se.mickelus.tetra.effect.potion.SmallAbsorbPotionEffect;
import se.mickelus.tetra.effect.potion.SmallHealthPotionEffect;
import se.mickelus.tetra.effect.potion.SmallStrengthPotionEffect;
import se.mickelus.tetra.effect.potion.SteeledPotionEffect;
import se.mickelus.tetra.effect.potion.StunPotionEffect;
import se.mickelus.tetra.effect.potion.UnstablePowerMobEffect;
import se.mickelus.tetra.effect.potion.UnwaveringPotionEffect;
import se.mickelus.tetra.effect.gui.AbilityOverlays;
import se.mickelus.tetra.effect.howling.HowlingOverlay;
import se.mickelus.tetra.gui.stats.data.StatBarStore;
import se.mickelus.tetra.gui.stats.data.StatIndicatorStore;
import se.mickelus.tetra.gui.stats.data.StatRegistry;
import se.mickelus.tetra.gui.stats.data.StatSorterStore;
import se.mickelus.tetra.interactions.SecondaryInteractionOverlay;
import se.mickelus.tetra.items.modular.ThrownModularItemEntity;
import se.mickelus.tetra.items.modular.ThrownModularItemRenderer;
import se.mickelus.tetra.items.modular.impl.BlockProgressOverlay;
import se.mickelus.tetra.items.modular.impl.bow.RangedProgressOverlay;
import se.mickelus.tetra.items.modular.impl.crossbow.CrossbowOverlay;
import se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItemImpl;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HolosphereEntryStore;
import se.mickelus.tetra.items.modular.impl.holo.gui.scan.ScannerOverlayGui;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldBannerModel;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldModel;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldRenderer;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.suspend.SuspendPotionEffect;
import se.mickelus.tetra.items.modular.impl.toolbelt.booster.OverlayBooster;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltContainer;
import se.mickelus.tetra.items.modular.impl.toolbelt.gui.screen.ToolbeltScreen;
import se.mickelus.tetra.items.modular.impl.toolbelt.gui.overlay.ToolbeltOverlay;

public class ClientSetup {
    public static void init(IEventBus modBus) {
        modBus.register(ClientSetup.class);
        NeoForge.EVENT_BUS.register(ClientScheduler.class);

        StatRegistry.init();
        new StatIndicatorStore();
        new StatBarStore();
        new StatSorterStore();
        new HolosphereEntryStore();
        new ItemAbilityIconStore();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // enqueueWork swallows exceptions without logging
            try {
                ModularModelLoader.init();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(WorkbenchContainer.containerType.get(), WorkbenchScreen::new);
        event.register(ForgedContainerMenu.type.get(), ForgedContainerScreen::new);
        event.register(ToolbeltContainer.type.get(), ToolbeltScreen::new);
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(StatIndicatorStore.instance);
        event.registerReloadListener(StatBarStore.instance);
        event.registerReloadListener(StatSorterStore.instance);
        event.registerReloadListener(HolosphereEntryStore.instance);
        event.registerReloadListener(ItemAbilityIconStore.instance);
    }

    @SubscribeEvent
    public static void registerParticleFactory(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(SparkleParticleType.instance, SparkleParticle.Provider::new);
        event.registerSpriteSet(SweepingStrikeParticleType.instance, SweepingStrikeParticle.Provider::new);
        event.registerSpriteSet(PlainParticleType.instance, PlainParticle.Provider::new);
        event.registerSpriteSet(DripParticles.fallingBlood.get(), DripParticles.FallingBloodProvider::new);
        event.registerSpriteSet(DripParticles.landingBlood.get(), DripParticles.LandingBloodProvider::new);
        event.registerSpriteSet(DripParticles.fallingSlime.get(), DripParticles.FallingSlimeProvider::new);
        event.registerSpriteSet(DripParticles.landingSlime.get(), DripParticles.LandingSlimeProvider::new);
        event.registerSpriteSet(Particles.arcaneFire.get(), ArcaneFireParticleProvider::new);
        event.registerSpriteSet(Particles.splinteredPower.get(), SplinteredPowerParticleProvider::new);
        event.registerSpriteSet(Particles.sputteringPower.get(), SputteringPowerParticleProvider::new);
    }

    @SubscribeEvent
    public static void modelRegistryReady(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "modular_loader"), new ModularModelLoader());
    }

    @SubscribeEvent
    public static void registerEntityLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ForgedContainerRenderer.layer, ForgedContainerRenderer::createLayer);
        event.registerLayerDefinition(HammerBaseRenderer.layer, HammerBaseRenderer::createLayer);

        event.registerLayerDefinition(ScrollRenderer.layer, ScrollRenderer::createLayer);
        event.registerLayerDefinition(ModularShieldRenderer.layer, ModularShieldModel::createLayer);
        event.registerLayerDefinition(ModularShieldRenderer.bannerLayer, ModularShieldBannerModel::createLayer);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ExtractorProjectileEntity.type, ExtractorProjectileRenderer::new);
        event.registerEntityRenderer(ThrownModularItemEntity.type, ThrownModularItemRenderer::new);

        event.registerBlockEntityRenderer(WorkbenchTile.type.get(), WorkbenchTESR::new);
        event.registerBlockEntityRenderer(ScrollTile.type, ScrollRenderer::new);
        event.registerBlockEntityRenderer(HolosphereBlockEntity.type.get(), HolosphereEntityRenderer::new);
        event.registerBlockEntityRenderer(RackTile.type, RackTESR::new);

        event.registerBlockEntityRenderer(ForgedContainerBlockEntity.type.get(), ForgedContainerRenderer::new);
        event.registerBlockEntityRenderer(CoreExtractorPistonBlockEntity.type.get(), CoreExtractorPistonRenderer::new);
        event.registerBlockEntityRenderer(HammerBaseBlockEntity.type.get(), HammerBaseRenderer::new);
        event.registerBlockEntityRenderer(HammerHeadBlockEntity.type.get(), HammerHeadRenderer::new);
    }

    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register(new ScrollItemColor(), ScrollItem.instance);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(ModularShieldItem.instance.createClientExtensions(), ModularShieldItem.instance);
        event.registerItem(ModularCrossbowItemImpl.instance.createClientExtensions(), ModularCrossbowItemImpl.instance);
        event.registerMobEffect(EffectUnRenderer.INSTANCE,
                BleedingPotionEffect.instance,
                MiningSpeedPotionEffect.instance,
                StunPotionEffect.instance,
                SuspendPotionEffect.instance);
        event.registerMobEffect(new EffectTooltipRenderer(effect -> I18n.get("effect.tetra.unwavering.tooltip")), UnwaveringPotionEffect.instance);
        event.registerMobEffect(new EffectTooltipRenderer(effect -> I18n.get("effect.tetra.satiated.tooltip", effect.getAmplifier() + 1)),
                SatiatedPotionEffect.instance);
        event.registerMobEffect(new EffectTooltipRenderer(effect -> I18n.get("effect.tetra.pried.tooltip", effect.getAmplifier() + 1)),
                PriedPotionEffect.instance);
        event.registerMobEffect(new EffectTooltipRenderer(effect -> I18n.get("effect.tetra.small_health.tooltip", effect.getAmplifier() + 1)),
                SmallHealthPotionEffect.instance);
        event.registerMobEffect(new EffectTooltipRenderer(effect -> I18n.get("effect.tetra.small_absorb.tooltip", effect.getAmplifier() + 1)),
                SmallAbsorbPotionEffect.instance);
        event.registerMobEffect(new EffectTooltipRenderer(effect -> I18n.get("effect.tetra.small_strength.tooltip", effect.getAmplifier() + 1)),
                SmallStrengthPotionEffect.instance);
        event.registerMobEffect(new EffectTooltipRenderer(effect -> I18n.get("effect.tetra.steeled.tooltip", effect.getAmplifier() + 1)),
                SteeledPotionEffect.instance);
        event.registerMobEffect(new EffectTooltipRenderer(effect -> {
            int amount = effect.getAmplifier() + 1;
            return I18n.get("effect.tetra.exhausted.tooltip", amount * 10, amount * 5);
        }), ExhaustedPotionEffect.instance);
        event.registerMobEffect(new EffectTooltipRenderer(effect -> {
            int amp = effect.getAmplifier() + 1;
            double armor = Minecraft.getInstance().player.getArmorValue();
            double armorReduction = armor / (1 - amp * 0.1) - armor;
            return I18n.get("effect.tetra.punctured.tooltip", String.format("%d", amp * 10), String.format("%.1f", armorReduction));
        }), PuncturedPotionEffect.instance);
        event.registerMobEffect(new EffectTooltipRenderer(effect -> {
            int amp = effect.getAmplifier() + 1;
            return I18n.get("effect.tetra.severed.tooltip", String.format("%d", amp * 10), String.format("%d", amp * 5));
        }), SeveredPotionEffect.instance);
        event.registerMobEffect(new EffectTooltipRenderer(effect -> {
            int amp = effect.getAmplifier() + 1;
            return I18n.get("effect.tetra.howling.tooltip",
                    String.format("%d", amp * -5), String.format("%.01f", Math.min(amp * 12.5, 100)), String.format("%.01f", amp * 2.5));
        }), HowlingPotionEffect.instance);
        event.registerMobEffect(new UnstablePowerMobEffect.ClientRenderer(), UnstablePowerMobEffect.instance);
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        var mc = Minecraft.getInstance();
        registerOverlay(event, "howling", new HowlingOverlay(mc));
        registerOverlay(event, "ability_overlays", new AbilityOverlays(mc));
        registerOverlay(event, "toolbelt", new ToolbeltOverlay(mc));
        registerOverlay(event, "secondary_interaction", new SecondaryInteractionOverlay(mc));
        registerOverlay(event, "booster", new OverlayBooster(mc));
        registerOverlay(event, "block_progresss", new BlockProgressOverlay(mc));
        registerOverlay(event, "ranged_progresss", new RangedProgressOverlay(mc));
        registerOverlay(event, "crossbow", new CrossbowOverlay(mc));
        registerOverlay(event, "scanner", new ScannerOverlayGui());
        registerOverlay(event, "multiblock_schematic", new MultiblockSchematicGui(mc));
    }

    private static void registerOverlay(RegisterGuiLayersEvent event, String id, LayeredDraw.Layer overlay) {
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, id), overlay);
        NeoForge.EVENT_BUS.register(overlay);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TetraKeyMappings.accessBinding);
        event.register(TetraKeyMappings.restockBinding);
        event.register(TetraKeyMappings.openBinding);
        event.register(TetraKeyMappings.secondaryUseBinding);
    }
}
