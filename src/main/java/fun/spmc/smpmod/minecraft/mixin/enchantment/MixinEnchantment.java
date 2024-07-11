package fun.spmc.smpmod.minecraft.mixin.enchantment;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fun.spmc.smpmod.utils.RegistryEntryTool;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public class MixinEnchantment {
    @ModifyReturnValue(method = "canBeCombined", at = @At("RETURN"))
    private static boolean hookCanBeCombined(boolean original, RegistryEntry<Enchantment> first, RegistryEntry<Enchantment> second) {
        if (second.matchesKey(Enchantments.SHARPNESS)) {
            if (first.matchesKey(Enchantments.SMITE) || first.matchesKey(Enchantments.BANE_OF_ARTHROPODS)) return true;
        } else if (second.matchesKey(Enchantments.SMITE)) {
            if (first.matchesKey(Enchantments.SHARPNESS) || first.matchesKey(Enchantments.BANE_OF_ARTHROPODS)) return true;
        } else if (second.matchesKey(Enchantments.BANE_OF_ARTHROPODS)) {
            if (first.matchesKey(Enchantments.SHARPNESS) || first.matchesKey(Enchantments.SMITE)) return true;
        } else if (first.matchesKey(Enchantments.MENDING) || first.matchesKey(Enchantments.INFINITY)) {
            if (second.matchesKey(Enchantments.MENDING) || second.matchesKey(Enchantments.INFINITY)) return true;
        } else if (first.matchesKey(Enchantments.PIERCING) || first.matchesKey(Enchantments.MULTISHOT)) {
            if (second.matchesKey(Enchantments.PIERCING) || second.matchesKey(Enchantments.MULTISHOT)) return true;
        }

        return original;
    }

    @ModifyReturnValue(method = "isAcceptableItem", at = @At("RETURN"))
    public boolean isAcceptableItem(boolean original, ItemStack stack) {
        Enchantment enchantment = (Enchantment) (Object) this;
        if (stack.getItem() instanceof AxeItem) {
            if (enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.FIRE_ASPECT).comp_349()) ||
                    enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.KNOCKBACK).comp_349()) ||
                    enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.LOOTING).comp_349())) return true;
        } else if (stack.getItem() instanceof TridentItem) {
            if (enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.FIRE_ASPECT).comp_349()) ||
                    enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.KNOCKBACK).comp_349()) ||
                    enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.LOOTING).comp_349()) ||
                    enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.SHARPNESS).comp_349()) ||
                    enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.BANE_OF_ARTHROPODS).comp_349()) ||
                    enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.SMITE).comp_349())) return true;
        } else if (stack.getItem() instanceof BowItem) {
            if (enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.LOOTING).comp_349()) ||
                    enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.PIERCING).comp_349()) ||
                    enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.MULTISHOT).comp_349())) return true;
        } else if (stack.getItem() instanceof CrossbowItem) {
            if (enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.LOOTING).comp_349()) ||
                    enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.INFINITY).comp_349()) ||
                    enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.FLAME).comp_349()) ||
                    enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.POWER).comp_349()) ||
                    enchantment.equals(RegistryEntryTool.getEnchantment(Enchantments.PUNCH).comp_349())) {
                return true;
            }
        }
        return original;
    }
}
