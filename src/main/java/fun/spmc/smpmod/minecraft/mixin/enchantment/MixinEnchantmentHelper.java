package fun.spmc.smpmod.minecraft.mixin.enchantment;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fun.spmc.smpmod.minecraft.utils.RegistryEntryTool;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {

    @ModifyReturnValue(method = "getProjectileSpread", at = @At("RETURN"))
    private static float getProjectileSpread(float original, ServerWorld serverWorld, ItemStack itemStack, Entity entity, float f) {
        if (EnchantmentHelper.getEnchantments(itemStack).getLevel(RegistryEntryTool.getEnchantment(Enchantments.MULTISHOT)) > 0) return 3f;
        return original;
    }
}
