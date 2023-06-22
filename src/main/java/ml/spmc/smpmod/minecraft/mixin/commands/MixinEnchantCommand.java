package ml.spmc.smpmod.minecraft.mixin.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.EnchantCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(EnchantCommand.class)
public class MixinEnchantCommand {
    /**
     * @author tcfplayz
     * @reason to make enchanting easier
     */
    @Overwrite
    private static int execute(ServerCommandSource serverCommandSource, Collection<? extends Entity> collection, RegistryEntry<Enchantment> registryEntry, int i) throws CommandSyntaxException {
        Enchantment enchantment = registryEntry.comp_349();
        Entity entity = collection.iterator().next();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            ItemStack itemStack = livingEntity.getMainHandStack();
            if (!itemStack.isEmpty()) itemStack.addEnchantment(enchantment, i);
            livingEntity.sendMessage(Text.literal("Your item has been enchanted to " + enchantment.getName(1)));
        }
        return 1;
    }
}
