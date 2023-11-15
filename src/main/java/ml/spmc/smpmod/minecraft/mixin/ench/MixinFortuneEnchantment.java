package ml.spmc.smpmod.minecraft.mixin.ench;

import net.minecraft.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Enchantment.class)
public class MixinFortuneEnchantment {
    /**
     * @author tcfplayz
     * @reason change enchantment max level
     */
    @Overwrite
    public int getMaxLevel() {
        return 6;
    }
}
