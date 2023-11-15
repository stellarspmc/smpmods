package ml.spmc.smpmod.minecraft.mixin.ench;

import net.minecraft.enchantment.UnbreakingEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(UnbreakingEnchantment.class)
public class MixinUnbreakingEnchantment {
    /**
     * @author tcfplayz
     * @reason change enchantment max level
     */
    @Overwrite()
    public int getMaxLevel() {
        return 10;
    }
}
