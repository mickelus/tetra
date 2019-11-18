package se.mickelus.tetra.data;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.network.AbstractPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateDataPacket extends AbstractPacket {

    private String directory;
    private Map<ResourceLocation, String> data;

    public UpdateDataPacket() {}

    public UpdateDataPacket(String directory, Map<ResourceLocation, JsonElement> data) {
        this.directory = directory;
        this.data = data.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().toString()
                ));
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeString(directory);
        buffer.writeInt(data.size());
        data.forEach((resourceLocation, data) -> {
            buffer.writeResourceLocation(resourceLocation);
            buffer.writeString(data);
        });
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        directory = buffer.readString();
        int count = buffer.readInt();
        data = new HashMap<>();
        for (int i = 0; i < count; i++) {
            data.put(buffer.readResourceLocation(), buffer.readString());
        }
    }

    @Override
    public void handle(PlayerEntity player) {
        DataManager.instance.onDataRecieved(directory, data);
    }
}
