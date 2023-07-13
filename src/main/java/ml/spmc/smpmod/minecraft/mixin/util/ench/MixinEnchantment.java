package ml.spmc.smpmod.minecraft.mixin.util.ench;

import net.minecraft.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Enchantment.class)
public class MixinEnchantment {
    /**
     * @author tcfplayz
     * @reason change enchantment max level
     */
    @Overwrite
    public int getMaxLevel() {
        return 3;
    }
}
