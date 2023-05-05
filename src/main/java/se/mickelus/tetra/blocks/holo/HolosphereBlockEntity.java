package se.mickelus.tetra.blocks.holo;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class HolosphereBlockEntity extends BlockEntity {
    public static final int maxRange = 8;
    public static RegistryObject<BlockEntityType<HolosphereBlockEntity>> type;
    private List<ScanResult> scanResults;

    public HolosphereBlockEntity(BlockPos pos, BlockState blockState) {
        super(type.get(), pos, blockState);
        scanResults = new ArrayList<>();
    }

    @Override
    public AABB getRenderBoundingBox() {
        return Shapes.block().bounds().inflate(1, 0.5, 1).move(worldPosition);
    }

    public List<ScanResult> getScanResults() {
        return scanResults;
    }

    private String[] getScannableStructures() {
        return new String[]{"#tetra:forged_ruins"};
    }

    private long getTimestamp(long gametime, int x, int y) {
        return (long) (gametime + (Mth.length(x, y) * 1.5) + Math.random() * 5);
    }

    public void use(int hammerLevel, float hammerEfficiency, float angle) {
        ServerLevel serverLevel = (ServerLevel) getLevel();
        int count = 4 + (int) hammerEfficiency / 3;
        int ox = SectionPos.blockToSectionCoord(getBlockPos().getX());
        int oz = SectionPos.blockToSectionCoord(getBlockPos().getZ());

        int preResultSize = scanResults.size();

        ChunkPos.rangeClosed(new ChunkPos(-32, -32), new ChunkPos(32, 32))
                .filter(pos -> Math.abs(Mth.atan2(pos.x, pos.z) - angle) < Math.PI / 6)
                .sorted(Comparator.comparingInt(pos -> pos.x * pos.x + pos.z * pos.z))
                .map(pos -> new ChunkPos(pos.x + ox, pos.z + oz))
                .filter(pos -> scanResults.stream().noneMatch(result -> result.chunkX == pos.x && result.chunkZ == pos.z))
                .limit(count)
                .forEach(pos -> {
                    long timestamp = this.getTimestamp(serverLevel.getGameTime(), pos.x - ox, pos.z - oz);
                    int height = getLevel().getChunk(pos.x, pos.z).getHeight(Heightmap.Types.WORLD_SURFACE, pos.x, pos.z);
                    BlockPos centerPos = pos.getMiddleBlockPosition(height);
                    float temperature = level.getBiome(centerPos).value().getTemperature(centerPos);
                    List<String> structures = Arrays.stream(getScannableStructures())
                            .filter(id -> ScanHelper.hasStructure(id, serverLevel, pos)).toList();
                    scanResults.add(new ScanResult(pos.x, pos.z, height, temperature, structures, timestamp));
                });

        if (preResultSize != scanResults.size()) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }


//        IntStream.rangeClosed(-maxRange, maxRange)
//                .boxed()
//                .flatMap(x -> IntStream.rangeClosed(-maxRange, maxRange).boxed().map(z -> Pair.of(x, z)))
//                .filter(pos -> )

//        for (int x = -maxRange; x <= maxRange; x++) {
//            for (int z = -maxRange; z <= maxRange; z++) {
//                if ()
//
//                int height = entity.getLevel().getChunk(x, z).getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
//                renderMarker(vertexBuilder, matrixStack, entity.getLevel(), dispatcher.camera, material.sprite(), 0, light, 0.5f + x * 0.075f, 0.01f * height, 0.5f + z * 0.075f, 1, 1, 1, 0.9f);
//                for (int k = height / 10; k > 0; k--) {
//                    renderMarker(vertexBuilder, matrixStack, entity.getLevel(), dispatcher.camera, material.sprite(), 0, light, 0.5f + x * 0.075f, 0.1f * k, 0.5f + z * 0.075f, 1, 1, 1, k * 0.05f);
//                }
//                renderMarker(vertexBuilder, matrixStack, entity.getLevel(), dispatcher.camera, material.sprite(), 0, light, 0.5f + x * 0.075f, 0.5f, 0.5f + z * 0.075f, 1, 1, 1, 0.2f);
//            }
//        }
//
//        boolean hasStructure = HolosphereBlock.hasStructure("#tetra:forged_ruins", (ServerLevel) world, new ChunkPos(player.getOnPos()));

    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        scanResults = compound.getList("scan", Tag.TAG_COMPOUND).stream()
                .map(nbt -> ScanResult.codec.decode(NbtOps.INSTANCE, nbt))
                .map(DataResult::result)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Pair::getFirst)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        ListTag list = scanResults.stream()
                .map(scroll -> ScanResult.codec.encodeStart(NbtOps.INSTANCE, scroll))
                .map(DataResult::result)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toCollection(ListTag::new));
        compound.put("scan", list);
    }

    record ScanResult(int chunkX, int chunkZ, int height, float temperature, List<String> structures, long timestamp) {
        static final Codec<ScanResult> codec = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("chunkX").forGetter(i -> i.chunkX),
                Codec.INT.fieldOf("chunkZ").forGetter(i -> i.chunkZ),
                Codec.INT.fieldOf("height").forGetter(i -> i.height),
                Codec.FLOAT.fieldOf("temperature").forGetter(i -> i.temperature),
                Codec.STRING.listOf().fieldOf("structures").forGetter(i -> i.structures),
                Codec.LONG.fieldOf("timestamp").forGetter(i -> i.timestamp)
        ).apply(instance, ScanResult::new));
    }
}
