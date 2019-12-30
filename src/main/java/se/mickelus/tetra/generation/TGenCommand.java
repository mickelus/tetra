package se.mickelus.tetra.generation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import se.mickelus.tetra.data.DataManager;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class TGenCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("tgen")
                .requires(source -> source.hasPermissionLevel(2))
                .then(Commands.argument("feature", ResourceLocationArgument.resourceLocation())
                        .suggests(TGenCommand::getFeatureSuggestions)
                        .executes(TGenCommand::generateAtPlayer)
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(TGenCommand::generateAtPos))));
    }

    private static CompletableFuture<Suggestions> getFeatureSuggestions(final CommandContext context, final SuggestionsBuilder builder) {
        return ISuggestionProvider.suggestIterable(DataManager.featureData.getData().keySet(), builder);
    }

    private static int generateAtPlayer(CommandContext<CommandSource> context) {
        BlockPos pos = new BlockPos(context.getSource().getPos());

        generate(ResourceLocationArgument.getResourceLocation(context, "feature"), context.getSource().getWorld(), pos,
                context.getSource().getWorld().getSeed());

        return 1;
    }

    private static int generateAtPos(CommandContext<CommandSource> context) throws CommandSyntaxException{
        generate(ResourceLocationArgument.getResourceLocation(context, "feature"), context.getSource().getWorld(),
                BlockPosArgument.getBlockPos(context, "pos"), context.getSource().getWorld().getSeed());

        return 1;
    }

    private static void generate(ResourceLocation featureLocation, World world, BlockPos pos, long seed) throws CommandException {
        FeatureParameters feature = DataManager.featureData.getData(featureLocation);
        if (feature != null) {
            ChunkPos chunkPos = new ChunkPos(pos);

            // seed setup based on vanilla pre-IWorldGenerator call
            Random random = new Random(seed);
            long xSeed = random.nextLong() >> 2 + 1L;
            long zSeed = random.nextLong() >> 2 + 1L;
            long chunkSeed = (xSeed * chunkPos.x + zSeed * chunkPos.z) ^ seed;

            random.setSeed(chunkSeed);
            FeatureEntry.instance.generateFeatureRoot(feature, world, pos, random);
        }
    }
}
