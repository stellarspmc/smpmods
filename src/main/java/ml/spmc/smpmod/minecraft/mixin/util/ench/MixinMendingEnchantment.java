package ml.spmc.smpmod.minecraft.mixin.util.ench;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.MendingEnchantment;
import net.minecraft.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MendingEnchantment.class)
public class MixinMendingEnchantment extends Enchantment {

    protected MixinMendingEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
        super(rarity, EnchantmentTarget.BREAKABLE, equipmentSlots);
    }

    @Unique
    public int getMaxLevel() {
        return 5;
    }

    @Unique
    public int getMinLevel() {
        return 5;
    }
}
