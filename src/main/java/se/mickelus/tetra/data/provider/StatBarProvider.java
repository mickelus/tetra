package se.mickelus.tetra.data.provider;

import com.google.gson.JsonElement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StatBarProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    private final Map<String, StatBarTemplate> bars = new HashMap<>();

    public StatBarProvider(PackOutput packOutput) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "stat_bars");

//        IStatGetter attackDamageGetter = sum(new StatGetterAttribute(Attributes.ATTACK_DAMAGE), sharpnessGetter);
//        GuiStatBar attackDamage = new GuiStatBar(0, 0, barLength, "tetra.stats.attack_damage",
//                0, 40, false, attackDamageGetter, LabelGetterBasic.decimalLabel,
//                new TooltipGetterDecimal("tetra.stats.attack_damage.tooltip", attackDamageGetter))
//                .setIndicators(
//                        new GuiStatIndicator(0, 0, "tetra.stats.sharpness", 17, sharpnessGetter,
//                                new TooltipGetterDecimalSingle("tetra.stats.sharpness.tooltip", sharpnessGetter)));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.allOf(bars.entrySet().stream()
                .map(template -> DataProvider.saveStable(cache, toJson(template.getValue()), pathProvider.json(new ResourceLocation(template.getKey()))))
                .toArray(CompletableFuture[]::new));
    }

    protected JsonElement toJson(StatBarTemplate template) {
        return null;
    }

    @Override
    public String getName() {
        return "tetra:statbars";
    }

    record StatBarTemplate(String name, String texture, String color, String overlay) {
    }
}


