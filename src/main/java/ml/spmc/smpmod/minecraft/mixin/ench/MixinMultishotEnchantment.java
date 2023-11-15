package ml.spmc.smpmod.minecraft.mixin.ench;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.MultishotEnchantment;
import net.minecraft.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MultishotEnchantment.class)
public class MixinMultishotEnchantment extends Enchantment {

    protected MixinMultishotEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
        super(rarity, EnchantmentTarget.CROSSBOW, equipmentSlots);
    }

    @Unique
    public int getMaxLevel() {
        return 10;
    }
}