package se.mickelus.tetra.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Registry;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.salvage.InteractiveBlockOverlay;
import se.mickelus.tetra.blocks.scroll.ScrollItem;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchScreen;
import se.mickelus.tetra.compat.botania.BotaniaCompat;
import se.mickelus.tetra.effect.gui.AbilityOverlays;
import se.mickelus.tetra.effect.howling.HowlingOverlay;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.properties.ReachEntityFix;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;

@ParametersAreNonnullByDefault
public class ClientProxy implements IProxy {

    @Override
    public void preInit(ITetraItem[] items, ITetraBlock[] blocks) {
    }

    @Override
    public void init(FMLCommonSetupEvent event, ITetraItem[] items, ITetraBlock[] blocks) {
        Arrays.stream(items).forEach(ITetraItem::clientInit);
        Arrays.stream(blocks).forEach(ITetraBlock::clientInit);

        // these are registered here as there are multiple instances of workbench blocks
        MenuScreens.register(WorkbenchTile.containerType, WorkbenchScreen::new);

        MinecraftForge.EVENT_BUS.register(new HowlingOverlay(Minecraft.getInstance()));
        MinecraftForge.EVENT_BUS.register(new AbilityOverlays(Minecraft.getInstance()));

        BotaniaCompat.clientInit();

        MinecraftForge.EVENT_BUS.register(ReachEntityFix.class);
    }

    @Override
    public void postInit() {
        MinecraftForge.EVENT_BUS.register(new InteractiveBlockOverlay());
        ScrollItem.clientPostInit();
    }

    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public TagCollection<Item> getItemTags() {
        if (Minecraft.getInstance().level != null) {
            return Minecraft.getInstance().level.getTagManager().getOrEmpty(Registry.ITEM_REGISTRY);
        }

        return SerializationTags.getInstance().getOrEmpty(Registry.ITEM_REGISTRY);
    }
}
