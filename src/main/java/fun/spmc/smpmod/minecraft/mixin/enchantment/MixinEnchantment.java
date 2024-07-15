package fun.spmc.smpmod.minecraft.mixin.enchantment;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fun.spmc.smpmod.minecraft.utils.RegistryEntryTool;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public class MixinEnchantment {

    @ModifyReturnValue(method = "isAcceptableItem", at = @At("RETURN"))
    public boolean isAcceptableItem(boolean original, ItemStack stack) {
        Enchantment enchantment = (Enchantment) (Object) this;
        if (stack.getItem() instanceof BowItem) {
            if (enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.MULTISHOT).comp_349())) {
                return true;
            }
        }
        return original;
    }
}
