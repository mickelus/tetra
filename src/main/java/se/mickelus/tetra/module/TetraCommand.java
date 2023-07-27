package se.mickelus.tetra.module;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.data.ImprovementData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.commands.arguments.ItemEnchantmentArgument.enchantment;
import static net.minecraft.commands.arguments.ItemEnchantmentArgument.getEnchantment;

@ParametersAreNonnullByDefault
public class TetraCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        CommandBuildContext context = new CommandBuildContext(RegistryAccess.BUILTIN.get());
        context.missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy.RETURN_EMPTY);

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("tetra")
                .requires(player -> player.hasPermission(2));

        command.then(Commands.literal("hone").executes(ctx -> runHone(ctx, 100))
                .then(Commands.argument("progress", IntegerArgumentType.integer(0, 100))
                        .executes(ctx -> runHone(ctx, getInteger(ctx, "progress")))));

        command.then(Commands.literal("module")
                .then(Commands.argument("slot", StringArgumentType.string()).suggests(TetraCommand::getAllSlotSuggestions)
                        .then(Commands.literal("remove")
                                .executes(ctx -> runRemoveModule(ctx, getString(ctx, "slot"))))
                        .then(Commands.argument("module", StringArgumentType.string()).suggests(TetraCommand::getModuleSuggestions)
                                .then(Commands.argument("variant", StringArgumentType.string()).suggests(TetraCommand::getVariantSuggestions)
                                        .executes(ctx -> runAddModule(ctx, getString(ctx, "slot"), getString(ctx, "module"), getString(ctx, "variant")))))));

        command.then(Commands.literal("improvement")
                .then(Commands.argument("slot", StringArgumentType.string()).suggests(TetraCommand::getMajorSlotSuggestions)
                        .then(Commands.literal("clear")
                                .executes(ctx -> runClearImprovements(ctx, getString(ctx, "slot"))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("improvement", StringArgumentType.string()).suggests(TetraCommand::getCurrentImprovementSuggestion)
                                        .executes(ctx -> runRemoveImprovement(ctx, getString(ctx, "slot"), getString(ctx, "improvement")))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("improvement", StringArgumentType.string()).suggests(TetraCommand::getAvailableImprovementSuggestion)
                                        .then(Commands.argument("level", IntegerArgumentType.integer()).suggests(TetraCommand::getImprovementLevelSuggestion)
                                                .executes(ctx -> runAddImprovement(ctx, getString(ctx, "slot"), getString(ctx, "improvement"), getInteger(ctx, "level"))))))));

        command.then(Commands.literal("enchantment")
                .then(Commands.argument("slot", StringArgumentType.string()).suggests(TetraCommand::getMajorSlotSuggestions)
                        .then(Commands.literal("clear")
                                .executes(ctx -> runClearEnchantments(ctx, getString(ctx, "slot"))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("enchantment", enchantment()).suggests(TetraCommand::getCurrentEnchantmentsSuggestion)
                                        .executes(ctx -> runRemoveEnchantment(ctx, getString(ctx, "slot"), getEnchantment(ctx, "enchantment")))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("enchantment", enchantment())
                                        .then(Commands.argument("level", IntegerArgumentType.integer()).suggests(TetraCommand::getEnchantmentLevelSuggestion)
                                                .executes(ctx -> runAddEnchantment(ctx, getString(ctx, "slot"), getEnchantment(ctx, "enchantment"), getInteger(ctx, "level"))))))));


        dispatcher.register(command);
    }

    private static int runHone(CommandContext<CommandSourceStack> context, int progress) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.getItem() instanceof IModularItem item) {
            if (item.canGainHoneProgress()) {
                item.setHoningProgress(itemStack, (int) Math.ceil((100 - progress) / 100f * item.getHoningLimit(itemStack)));
                context.getSource().sendSuccess(Component.literal("Honing progression set to §e" + progress + "%§r for ").append(itemStack.getDisplayName()), true);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("Item cannot be honed"));
            }
        } else {
            context.getSource().sendFailure(Component.literal("Main hand item is not a modular item"));
        }
        return 0;
    }

    private static int runRemoveModule(CommandContext<CommandSourceStack> context, String slot) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.getItem() instanceof IModularItem item) {
            ItemModule module = item.getModuleFromSlot(itemStack, slot);
            if (module != null) {
                String moduleIdentifiers = "'" + module.getKey() + "' (" + module.getVariantData(itemStack).key + ")";
                module.removeModule(itemStack);
                module.postRemove(itemStack, player);
                IModularItem.updateIdentifier(itemStack);
                context.getSource().sendSuccess(Component.literal("Removed module " + moduleIdentifiers + " from slot '" + slot + "' in item ")
                        .append(itemStack.getDisplayName()), true);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("The provided slot is already empty"));
            }
        } else {
            context.getSource().sendFailure(Component.literal("Main hand item is not a modular item"));
        }
        return 0;
    }

    private static int runAddModule(CommandContext<CommandSourceStack> context, String slot, String moduleKey, String variantKey) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.getItem() instanceof IModularItem item) {
            ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleKey);
            if (module != null) {
                ItemModule previousModule = item.getModuleFromSlot(itemStack, slot);
                if (previousModule != null) {
                    previousModule.removeModule(itemStack);
                    previousModule.postRemove(itemStack, player);
                }
                module.addModule(itemStack, variantKey, player);
                IModularItem.updateIdentifier(itemStack);


                context.getSource().sendSuccess(Component.literal("Added module " + moduleKey + " in slot '" + slot + "' in item ")
                        .append(itemStack.getDisplayName()), true);
                return 1;
            }
        } else {
            context.getSource().sendFailure(Component.literal("Main hand item is not a modular item"));
        }

        return 0;
    }

    private static int runClearImprovements(CommandContext<CommandSourceStack> context, String slot) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.getItem() instanceof IModularItem item) {
            if (item.getModuleFromSlot(itemStack, slot) instanceof ItemModuleMajor module) {
                ImprovementData[] improvements = module.getImprovements(itemStack);
                Arrays.stream(improvements).forEach(improvement -> module.removeImprovement(itemStack, improvement.key));
                IModularItem.updateIdentifier(itemStack);
                context.getSource().sendSuccess(Component.literal("Cleared " + improvements.length + " improvements from slot '" + slot + "' in item ")
                        .append(itemStack.getDisplayName()), true);
            } else {
                context.getSource().sendFailure(Component.literal("The provided slot is empty (or not a major module slot)"));
            }
        } else {
            context.getSource().sendFailure(Component.literal("Main hand item is not a modular item"));
            return 0;
        }
        return 1;
    }

    private static int runRemoveImprovement(CommandContext<CommandSourceStack> context, String slot, String improvementKey) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.getItem() instanceof IModularItem item) {
            if (item.getModuleFromSlot(itemStack, slot) instanceof ItemModuleMajor module) {
                module.removeImprovement(itemStack, improvementKey);
                IModularItem.updateIdentifier(itemStack);
                context.getSource().sendSuccess(Component.literal("Removed improvement '" + improvementKey + "' from slot '" + slot + "' in item ")
                        .append(itemStack.getDisplayName()), true);
            } else {
                context.getSource().sendFailure(Component.literal("The provided slot is empty (or not a major module slot)"));
            }
        } else {
            context.getSource().sendFailure(Component.literal("Main hand item is not a modular item"));
            return 0;
        }
        return 1;
    }

    private static int runAddImprovement(CommandContext<CommandSourceStack> context, String slot, String improvementKey, int level) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.getItem() instanceof IModularItem item) {
            if (item.getModuleFromSlot(itemStack, slot) instanceof ItemModuleMajor module) {
                module.removeCollidingImprovements(itemStack, improvementKey, level);
                module.addImprovement(itemStack, improvementKey, level);
                IModularItem.updateIdentifier(itemStack);
                context.getSource().sendSuccess(Component.literal("Added improvement '" + improvementKey + "' at level " + level + " from slot '" + slot + "' in item ")
                        .append(itemStack.getDisplayName()), true);
            } else {
                context.getSource().sendFailure(Component.literal("The provided slot is empty (or not a major module slot)"));
            }
        } else {
            context.getSource().sendFailure(Component.literal("Main hand item is not a modular item"));
            return 0;
        }
        return 1;
    }

    private static int runClearEnchantments(CommandContext<CommandSourceStack> context, String slot) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.getItem() instanceof IModularItem item) {
            if (item.getModuleFromSlot(itemStack, slot) instanceof ItemModuleMajor module) {
                Map<Enchantment, Integer> enchantments = module.getEnchantments(itemStack);
                module.removeEnchantments(itemStack);
                IModularItem.updateIdentifier(itemStack);
                context.getSource().sendSuccess(Component.literal("Cleared " + enchantments.size() + " enchantments from slot '" + slot + "' in item ")
                        .append(itemStack.getDisplayName()), true);
            } else {
                context.getSource().sendFailure(Component.literal("The provided slot is empty (or not a major module slot)"));
            }
        } else {
            context.getSource().sendFailure(Component.literal("Main hand item is not a modular item"));
            return 0;
        }
        return 1;
    }

    private static int runRemoveEnchantment(CommandContext<CommandSourceStack> context, String slot, Enchantment enchantment) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.getItem() instanceof IModularItem item) {
            if (item.getModuleFromSlot(itemStack, slot) instanceof ItemModuleMajor module) {
                String enchantmentId = Registry.ENCHANTMENT.getKey(enchantment).toString();
                TetraEnchantmentHelper.removeEnchantment(itemStack, enchantmentId);
                IModularItem.updateIdentifier(itemStack);
                context.getSource().sendSuccess(Component.literal("Removed enchantment '" + enchantmentId + "' from item ")
                        .append(itemStack.getDisplayName()), true);
            } else {
                context.getSource().sendFailure(Component.literal("The provided slot is empty (or not a major module slot)"));
            }
        } else {
            context.getSource().sendFailure(Component.literal("Main hand item is not a modular item"));
            return 0;
        }
        return 1;
    }

    private static int runAddEnchantment(CommandContext<CommandSourceStack> context, String slot, Enchantment enchantment, int level) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.getItem() instanceof IModularItem item) {
            if (item.getModuleFromSlot(itemStack, slot) instanceof ItemModuleMajor module) {
                String enchantmentId = Registry.ENCHANTMENT.getKey(enchantment).toString();
                int currentLevel = itemStack.getItem().getEnchantmentLevel(itemStack, enchantment);
                TetraEnchantmentHelper.removeEnchantment(itemStack, enchantmentId);
                TetraEnchantmentHelper.applyEnchantment(itemStack, module.getSlot(), enchantment, level);
                IModularItem.updateIdentifier(itemStack);

                if (currentLevel > 0) {
                    context.getSource().sendSuccess(Component.literal("Updated enchantment '" + enchantmentId + "' to level " + level + " in slot '" + slot + "' in item ")
                            .append(itemStack.getDisplayName()), true);
                } else {

                    context.getSource().sendSuccess(Component.literal("Added enchantment '" + enchantmentId + "' at level " + level + " for slot '" + slot + "' in item ")
                            .append(itemStack.getDisplayName()), true);
                }
            } else {
                context.getSource().sendFailure(Component.literal("The provided slot is empty (or not a major module slot)"));
            }
        } else {
            context.getSource().sendFailure(Component.literal("Main hand item is not a modular item"));
            return 0;
        }
        return 1;
    }

    private static CompletableFuture<Suggestions> getMajorSlotSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            ItemStack itemStack = player.getMainHandItem();
            if (itemStack.getItem() instanceof IModularItem item) {
                return SharedSuggestionProvider.suggest(Arrays.stream(item.getMajorModuleKeys()).map(key -> "\"" + key + "\""), builder);
            }
        }
        return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
    }

    private static CompletableFuture<Suggestions> getAllSlotSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            ItemStack itemStack = player.getMainHandItem();
            if (itemStack.getItem() instanceof IModularItem item) {
                List<String> suggestions = Stream.concat(Arrays.stream(item.getMajorModuleKeys()), Arrays.stream(item.getMinorModuleKeys()))
                        .map(slot -> "\"" + slot + "\"")
                        .toList();
                return SharedSuggestionProvider.suggest(suggestions, builder);
            }
        }
        return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
    }

    private static CompletableFuture<Suggestions> getModuleSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        String slot = getString(context, "slot");
        return SharedSuggestionProvider.suggest(
                ItemUpgradeRegistry.instance.getAllModules().stream()
                        .filter(module -> slot == null || slot.equals(module.getSlot()))
                        .filter(module -> !module.perk)
                        .map(ItemModule::getKey)
                        .map(key -> "\"" + key + "\"")
                        .toArray(String[]::new),
                builder);
    }

    private static CompletableFuture<Suggestions> getVariantSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        ItemModule module = ItemUpgradeRegistry.instance.getModule(getString(context, "module"));
        return SharedSuggestionProvider.suggest(Arrays.stream(module.getVariantData()).map(data -> data.key).map(key -> "\"" + key + "\""), builder);
    }

    private static CompletableFuture<Suggestions> getAvailableImprovementSuggestion(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        String slot = getString(context, "slot");
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            ItemStack itemStack = player.getMainHandItem();
            if (itemStack.getItem() instanceof IModularItem item
                    && item.getModuleFromSlot(itemStack, slot) instanceof ItemModuleMajor module) {
                List<String> suggestions = Arrays.stream(module.improvements).map(improvement -> improvement.key)
                        .map(key -> "\"" + key + "\"")
                        .toList();
                return SharedSuggestionProvider.suggest(suggestions, builder);
            }
        }
        return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
    }

    private static CompletableFuture<Suggestions> getCurrentImprovementSuggestion(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        String slot = getString(context, "slot");
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            ItemStack itemStack = player.getMainHandItem();
            if (itemStack.getItem() instanceof IModularItem item
                    && item.getModuleFromSlot(itemStack, slot) instanceof ItemModuleMajor module) {
                List<String> suggestions = Arrays.stream(module.getImprovements(itemStack))
                        .map(improvement -> improvement.key)
                        .map(key -> "\"" + key + "\"")
                        .toList();
                return SharedSuggestionProvider.suggest(suggestions, builder);
            }
        }
        return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
    }

    private static CompletableFuture<Suggestions> getImprovementLevelSuggestion(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        String slot = getString(context, "slot");
        String improvementKey = getString(context, "improvement");
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            ItemStack itemStack = player.getMainHandItem();
            if (itemStack.getItem() instanceof IModularItem item
                    && item.getModuleFromSlot(itemStack, slot) instanceof ItemModuleMajor module) {
                List<String> suggestions = Arrays.stream(module.improvements)
                        .filter(improvement -> improvement.key.equals(improvementKey))
                        .map(improvement -> improvement.level)
                        .map(String::valueOf)
                        .toList();
                return SharedSuggestionProvider.suggest(suggestions, builder);
            }
        }
        return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
    }


    private static CompletableFuture<Suggestions> getCurrentEnchantmentsSuggestion(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        String slot = getString(context, "slot");
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            ItemStack itemStack = player.getMainHandItem();
            if (itemStack.getItem() instanceof IModularItem item
                    && item.getModuleFromSlot(itemStack, slot) instanceof ItemModuleMajor module) {
                List<String> suggestions = module.getEnchantments(itemStack).keySet().stream()
                        .map(Registry.ENCHANTMENT::getKey)
                        .filter(Objects::nonNull)
                        .map(ResourceLocation::toString)
                        .toList();
                return SharedSuggestionProvider.suggest(suggestions, builder);
            }
        }
        return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
    }

    private static CompletableFuture<Suggestions> getEnchantmentLevelSuggestion(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        Enchantment enchantment = getEnchantment(context, "enchantment");
        List<String> suggestions = IntStream.rangeClosed(enchantment.getMinLevel(), enchantment.getMaxLevel())
                .mapToObj(String::valueOf)
                .toList();
        return SharedSuggestionProvider.suggest(suggestions, builder);
    }
}
