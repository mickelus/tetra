package se.mickelus.tetra.module;

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
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.data.VariantData;

import java.util.concurrent.CompletableFuture;

public class ModuleDevCommand {
    private static final Logger logger = LogManager.getLogger();

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("tmdev")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("item", ItemArgument.item())
                        .then(Commands.argument("module", StringArgumentType.greedyString())
                                .suggests(ModuleDevCommand::getModuleSuggestions)
                                .executes(ModuleDevCommand::run))));
    }

    private static int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        BlockPos pos = new BlockPos(context.getSource().getPosition());
        World world = context.getSource().getLevel();

        ItemStack baseStack = ItemArgument.getItem(context, "item").createItemStack(1, false);

        if (!(baseStack.getItem() instanceof IModularItem)) {
            baseStack = ItemUpgradeRegistry.instance.getReplacement(baseStack);
        }

        ItemModule module = ItemUpgradeRegistry.instance.getModule(StringArgumentType.getString(context, "module"));

        VariantData[] data = module.getVariantData();

        for (int i = 0; i < data.length; i++) {
            ItemStack itemStack = baseStack.copy();
            module.addModule(itemStack, data[i].key, context.getSource().getPlayerOrException());
            IModularItem.updateIdentifier(itemStack);
            plopFrame(world, pos.offset(i / 5, i % 5, 0), itemStack, module.getName(itemStack));
        }

        return 1;
    }

    private static void plopFrame(World world, BlockPos pos, ItemStack itemStack, String label) {
        itemStack.setHoverName(new StringTextComponent(label));
        ItemFrameEntity itemFrame = new ItemFrameEntity(world, pos, Direction.SOUTH);
        itemFrame.setItem(itemStack);
        world.addFreshEntity(itemFrame);
    }

    private static CompletableFuture<Suggestions> getModuleSuggestions(final CommandContext context, final SuggestionsBuilder builder) {
        return ISuggestionProvider.suggest(
                ItemUpgradeRegistry.instance.getAllModules().stream()
                        .map(ItemModule::getKey)
                        .toArray(String[]::new),
                builder);
    }

}
