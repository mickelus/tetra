package se.mickelus.tetra;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.items.sword.ItemSwordModular;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.OutcomePreview;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class ModuleDevCommand {
    private static final Logger logger = LogManager.getLogger();

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("tmdev")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(ModuleDevCommand::run));
    }

    private static int run(CommandContext<CommandSource> context) {
        BlockPos pos = new BlockPos(context.getSource().getPos());
        World world = context.getSource().getWorld();

        ItemStack baseStack = new ItemStack(ItemSwordModular.instance);

        OutcomePreview[] outcomes = Optional.ofNullable(ItemUpgradeRegistry.instance.getSchema("basic_blade_schema"))
                .map(schema -> Arrays.stream(schema.getPreviews(baseStack, "sword/blade")))
                .orElseGet(Stream::empty)
                .toArray(OutcomePreview[]::new);

        for (int i = 0; i < outcomes.length; i++) {
            ItemStack itemStack = outcomes[i].itemStack.copy();
            itemStack.setDisplayName(itemStack.getDisplayName());
            ItemFrameEntity itemFrame = new ItemFrameEntity(world, pos.add(i / 5, i % 5, 0), Direction.SOUTH);
            itemFrame.setDisplayedItem(itemStack);
            world.addEntity(itemFrame);
        }

        return 1;
    }

}
