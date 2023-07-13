package ml.spmc.smpmod.minecraft.mixin.util.ench;

import net.minecraft.enchantment.QuickChargeEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(QuickChargeEnchantment.class)
public class MixinQuickChargeEnchantment {
    /**
     * @author tcfplayz
     * @reason change enchantment max level
     */
    @Overwrite()
    public int getMaxLevel() {
        return 5;
    }
}
