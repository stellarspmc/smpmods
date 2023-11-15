package ml.spmc.smpmod.minecraft.mixin.ench;

import net.minecraft.enchantment.EfficiencyEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EfficiencyEnchantment.class)
public class MixinEfficiencyEnchantment {
    /**
     * @author tcfplayz
     * @reason change enchantment max level
     */
    @Overwrite
    public int getMaxLevel() {
        return 10;
    }
}
