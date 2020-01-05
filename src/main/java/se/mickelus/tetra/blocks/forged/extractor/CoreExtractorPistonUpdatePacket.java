package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import se.mickelus.tetra.network.BlockPosPacket;
import se.mickelus.tetra.util.TileEntityOptional;

public class CoreExtractorPistonUpdatePacket extends BlockPosPacket {
    private long timestamp;

    public CoreExtractorPistonUpdatePacket() {}

    public CoreExtractorPistonUpdatePacket(BlockPos pos, long timestamp) {
        super(pos);

        this.timestamp = timestamp;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        buffer.writeLong(timestamp);
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        super.fromBytes(buffer);
        timestamp = buffer.readLong();
    }

    @Override
    public void handle(PlayerEntity player) {
        TileEntityOptional.from(player.world, pos, CoreExtractorPistonTile.class)
                .ifPresent(tile -> tile.setEndTime(timestamp));
    }
}
