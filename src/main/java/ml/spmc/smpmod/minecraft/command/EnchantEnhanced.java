package ml.spmc.smpmod.minecraft.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;


public class EnchantEnhanced {
    public static LiteralArgumentBuilder buildCommand(CommandRegistryAccess commandRegistryAccess) {
        return CommandManager.literal("ench").requires((serverCommandSource) -> serverCommandSource.hasPermissionLevel(2))
                .then(CommandManager.argument("targets", EntityArgumentType.entities())
                .then(((RequiredArgumentBuilder)CommandManager.argument("enchantment", RegistryEntryArgumentType.registryEntry(commandRegistryAccess, RegistryKeys.ENCHANTMENT))
                        .executes((commandContext) -> execute(commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"), RegistryEntryArgumentType.getEnchantment(commandContext, "enchantment"), 1))).then(CommandManager.argument("level", IntegerArgumentType.integer(0)).executes((commandContext) -> execute(commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"), RegistryEntryArgumentType.getEnchantment(commandContext, "enchantment"), IntegerArgumentType.getInteger(commandContext, "level"))))));
    }

    private static int execute(ServerCommandSource serverCommandSource, Collection<? extends Entity> collection, RegistryEntry<Enchantment> registryEntry, int i) throws CommandSyntaxException {
        Enchantment enchantment = registryEntry.comp_349();
        Entity entity = collection.iterator().next();
        if (entity instanceof LivingEntity livingEntity) {
            ItemStack itemStack = livingEntity.getMainHandStack();
            if (!itemStack.isEmpty()) itemStack.addEnchantment(enchantment, i);
            livingEntity.sendMessage(Text.literal("Your item has been enchanted!"));
        }
        return 1;
    }
}
