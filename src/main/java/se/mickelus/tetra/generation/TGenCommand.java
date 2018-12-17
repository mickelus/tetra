package se.mickelus.tetra.generation;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.IChunkProvider;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TGenCommand extends CommandBase {
    @Override
    public String getName() {
        return "tgen";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "tgen feature [x y z]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        // this seems to require OP but still allows command blocks to call this command
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        BlockPos senderPos = sender.getPosition();
        ChunkPos chunkPos = new ChunkPos(senderPos);

        if (args.length == 0) {
            notifyCommandListener(sender, this, "Too few arguments: " + getUsage(sender), chunkPos);
            return;
        }
        ResourceLocation featureLocation = new ResourceLocation(args[0]);
        GenerationFeature feature = WorldGenFeatures.instance.getFeature(featureLocation);

        if (feature == null) {
            notifyCommandListener(sender, this, "Feature not found " + featureLocation, chunkPos);
        }

        if (args.length == 3) {
            chunkPos = new ChunkPos(parseInt(args[1]), parseInt(args[2]));
        }

        // seed setup based on vanilla pre-IWorldGenerator call
        long seed = sender.getEntityWorld().getSeed();
        Random random = new Random(seed);
        long xSeed = random.nextLong() >> 2 + 1L;
        long zSeed = random.nextLong() >> 2 + 1L;
        long chunkSeed = (xSeed * chunkPos.x + zSeed * chunkPos.z) ^ seed;

        random.setSeed(chunkSeed);
        WorldGenFeatures.instance.generateFeatureRoot(feature, chunkPos.x, chunkPos.z, sender.getEntityWorld(), random);

        notifyCommandListener(sender, this, "Generated feature " + featureLocation, chunkPos);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return Arrays.stream(WorldGenFeatures.instance.getFeatures())
                    .map(feature -> feature.location.toString())
                    .filter(location -> location.contains(args[0]))
                    .collect(Collectors.toList());
        }
        return Collections.<String>emptyList();
    }
}
