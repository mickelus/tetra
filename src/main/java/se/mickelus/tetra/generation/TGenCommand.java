package se.mickelus.tetra.generation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TGenCommand {

    private static final DynamicCommandExceptionType invalidFeatureException = new DynamicCommandExceptionType(feature ->
            new TranslationTextComponent("tetra.commands.tgen.invalid_feature", feature));
    private static final SimpleCommandExceptionType tooFewArgumentsException = new SimpleCommandExceptionType(new TranslationTextComponent("tetra.commands.tgen.invalid_arguments"));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("tgen")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .then(Commands.argument("feature", ResourceLocationArgument.resourceLocation()).suggests(TGenCommand::getSuggestions))
                .then(Commands.argument("at", Vec3Argument.vec3()))
                .executes(context -> execute(context.getSource(),
                        ResourceLocationArgument.getResourceLocation(context, "feature"),
                        Vec3Argument.getLocation(context, "at")))
                .then(Commands.argument("feature", ResourceLocationArgument.resourceLocation()).suggests(TGenCommand::getSuggestions))
                .executes(context -> execute(context.getSource(),
                        ResourceLocationArgument.getResourceLocation(context, "feature"), null)));
    }

    private static int execute(CommandSource source, ResourceLocation featureLocation, ILocationArgument position) throws CommandSyntaxException {
        BlockPos pos = position != null ? position.getBlockPos(source) : new BlockPos(source.getPos());

        if (featureLocation == null) {
            throw tooFewArgumentsException.create();
        }

        GenerationFeature feature = WorldGenFeatures.instance.getFeature(featureLocation);

        if (feature == null) {
            throw invalidFeatureException.create(featureLocation);
        }

        // seed setup based on vanilla pre-IWorldGenerator call
        long seed = source.getWorld().getSeed();
        Random random = new Random(seed);
        long xSeed = random.nextLong() >> 2 + 1L;
        long zSeed = random.nextLong() >> 2 + 1L;
        long chunkSeed = (xSeed * pos.getX() + zSeed * pos.getZ()) ^ seed;

        random.setSeed(chunkSeed);
        WorldGenFeatures.instance.generateFeatureRoot(feature, pos.getX(), pos.getZ(), source.getWorld(), random);

        source.sendFeedback(new TranslationTextComponent("tetra.commands.tgen.success", featureLocation, pos), true);

        return 1;
    }

    private static CompletableFuture<Suggestions> getSuggestions(final CommandContext<CommandSource> context, final SuggestionsBuilder builder) {
        List<ResourceLocation> suggestions = Arrays.stream(WorldGenFeatures.instance.getFeatures())
                .map(feature -> feature.location)
                .collect(Collectors.toList());
        return ISuggestionProvider.suggestIterable(suggestions, builder);
    }
}