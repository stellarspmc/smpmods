package ml.spmc.smpmod.minecraft.mixin.util;

import net.minecraft.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Enchantment.class)
public class MixinEnchantment {
    /**
     * @author tcfplayz
     * @reason make all enchantments max level be level 10
     */
    @Overwrite()
    public int getMaxLevel() {
        return 10;
    }
}
