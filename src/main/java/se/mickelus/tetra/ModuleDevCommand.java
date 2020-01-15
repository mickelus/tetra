package se.mickelus.tetra;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.ModuleData;

import java.util.concurrent.CompletableFuture;

public class ModuleDevCommand {
    private static final Logger logger = LogManager.getLogger();

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("tmdev")
                .requires(source -> source.hasPermissionLevel(2))
                .then(Commands.argument("item", ItemArgument.item())
                        .then(Commands.argument("module", StringArgumentType.greedyString())
                                .suggests(ModuleDevCommand::getModuleSuggestions)
                                .executes(ModuleDevCommand::run))));
    }

    private static int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        BlockPos pos = new BlockPos(context.getSource().getPos());
        World world = context.getSource().getWorld();

        ItemStack baseStack = ItemArgument.getItem(context, "item").createStack(1, false);

        ItemModule module = ItemUpgradeRegistry.instance.getModule(StringArgumentType.getString(context, "module"));

        ModuleData[] data = module.getData();

        for (int i = 0; i < data.length; i++) {
            ItemStack itemStack = baseStack.copy();
            module.addModule(itemStack, data[i].key, context.getSource().asPlayer());
            plopFrame(world, pos.add(i / 5, i % 5, 0), itemStack, module.getName(itemStack));
        }

        return 1;
    }

    private static void plopFrame(World world, BlockPos pos, ItemStack itemStack, String label) {
        itemStack.setDisplayName(new StringTextComponent(label));
        ItemFrameEntity itemFrame = new ItemFrameEntity(world, pos, Direction.SOUTH);
        itemFrame.setDisplayedItem(itemStack);
        world.addEntity(itemFrame);
    }

    private static CompletableFuture<Suggestions> getModuleSuggestions(final CommandContext context, final SuggestionsBuilder builder) {
        return ISuggestionProvider.suggest(
                ItemUpgradeRegistry.instance.getAllModules().stream()
                        .map(ItemModule::getKey)
                        .toArray(String[]::new),
                builder);
    }

}
