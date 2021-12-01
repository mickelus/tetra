package se.mickelus.tetra.generation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.World;
import se.mickelus.tetra.data.DataManager;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class TGenCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tgen")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("feature", ResourceLocationArgument.id())
                        .suggests(TGenCommand::getFeatureSuggestions)
                        .executes(TGenCommand::generateAtPlayer)
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(TGenCommand::generateAtPos))));
    }

    private static CompletableFuture<Suggestions> getFeatureSuggestions(final CommandContext context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(DataManager.featureData.getData().keySet(), builder);
    }

    private static int generateAtPlayer(CommandContext<CommandSourceStack> context) {
        BlockPos pos = new BlockPos(context.getSource().getPosition());

        generate(ResourceLocationArgument.getId(context, "feature"), context.getSource().getLevel(), pos,
                context.getSource().getLevel().getSeed());

        return 1;
    }

    private static int generateAtPos(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{
        generate(ResourceLocationArgument.getId(context, "feature"), context.getSource().getLevel(),
                BlockPosArgument.getOrLoadBlockPos(context, "pos"), context.getSource().getLevel().getSeed());

        return 1;
    }

    private static void generate(ResourceLocation featureLocation, WorldGenLevel world, BlockPos pos, long seed) throws CommandRuntimeException {
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
