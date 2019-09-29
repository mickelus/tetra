package se.mickelus.tetra.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import java.util.HashMap;
import java.util.Map;

public class GuiHandlerRegistry implements IGuiHandler {

    public static GuiHandlerRegistry instance;

    private Map<Integer, TetraGuiHandler> handlerMap;

    public GuiHandlerRegistry() {
        instance = this;
        handlerMap = new HashMap<>();
    }

    public void registerHandler(int id, TetraGuiHandler handler) {
        handlerMap.put(id, handler);
    }

    @Override
    public Object getServerGuiElement(int id, PlayerEntity player, World world, int x, int y, int z) {
        if (handlerMap.containsKey(id)) {
            return handlerMap.get(id).getServerGuiElement(player, world, x, y, z);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, PlayerEntity player, World world, int x, int y, int z) {
        if (handlerMap.containsKey(id)) {
            return handlerMap.get(id).getClientGuiElement(player, world, x, y, z);
        }
        return null;
    }
}
