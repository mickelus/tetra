package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
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
    public void toBytes(FriendlyByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeLong(timestamp);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        super.fromBytes(buffer);
        timestamp = buffer.readLong();
    }

    @Override
    public void handle(Player player) {
        TileEntityOptional.from(player.level, pos, CoreExtractorPistonTile.class)
                .ifPresent(tile -> tile.setEndTime(timestamp));
    }
}
