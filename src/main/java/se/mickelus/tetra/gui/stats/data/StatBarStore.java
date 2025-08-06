package se.mickelus.tetra.gui.stats.data;

import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;
import se.mickelus.tetra.gui.stats.bar.GuiStatBase;
import se.mickelus.tetra.gui.stats.sorting.StatSorters;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloStatsGui;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class StatBarStore implements ResourceManagerReloadListener {
    private static final Logger logger = LogManager.getLogger();
    public static StatBarStore instance;
    private GuiStatBase[] statBars = new GuiStatBase[0];

    public StatBarStore() {
        instance = this;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        statBars = prepareBars();
        logger.info("Loaded {} stat bars", this.statBars.length);

        HoloStatsGui.setDataBars(Arrays.stream(statBars).filter(bar -> Arrays.asList(bar.getContexts()).contains("tetra:holosphere")).toArray(GuiStatBase[]::new));
        WorkbenchStatsGui.setDataBars(Arrays.stream(statBars).filter(bar -> Arrays.asList(bar.getContexts()).contains("tetra:workbench")).toArray(GuiStatBase[]::new));
        StatSorters.setDerivedSorters(Arrays.stream(statBars)
                .map(GuiStatBase::getSorter)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    public GuiStatBase[] getBars() {
        return statBars;
    }

    private static GuiStatBase[] prepareBars() {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        return resourceManager.listResources("stat_bars", rl -> rl.getPath().endsWith(".json")).entrySet().stream()
                .filter(entry -> TetraMod.MOD_ID.equals(entry.getKey().getNamespace()))
                .map(entry -> parseBar(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .toArray(GuiStatBase[]::new);
    }

    @Nullable
    private static GuiStatBase parseBar(ResourceLocation resourceLocation, Resource resource) {
        try (BufferedReader reader = resource.openAsReader()) {
            return GsonHelper.fromJson(StatRegistry.gson, reader, GuiStatBase.class);
        } catch (IOException | JsonParseException e) {
            logger.error("Failed to parse statbar data from '{}': {}", resourceLocation, e);
        }

        return null;
    }

}
