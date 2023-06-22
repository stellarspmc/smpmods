package ml.spmc.smpmod.minecraft.mixin.util.ench;

import net.minecraft.enchantment.UnbreakingEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(UnbreakingEnchantment.class)
public class MixinUnbreakingEnchantment {
    /**
     * @author tcfplayz
     * @reason make enchantment max level be level 10
     */
    @Overwrite()
    public int getMaxLevel() {
        return 10;
    }
}
