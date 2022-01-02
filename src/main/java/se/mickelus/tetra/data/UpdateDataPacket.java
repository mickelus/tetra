package se.mickelus.tetra.data;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import se.mickelus.mutil.data.AbstractUpdateDataPacket;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
public class UpdateDataPacket extends AbstractUpdateDataPacket {
    public UpdateDataPacket() {
    }

    public UpdateDataPacket(String directory, Map<ResourceLocation, JsonElement> data) {
        super(directory, data);
    }

    @Override
    public void handle(Player player) {
        DataManager.instance.onDataRecieved(directory, data);
    }
}
