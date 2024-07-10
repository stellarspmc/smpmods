package ml.spmc.smpmod.utils;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

import static ml.spmc.smpmod.SMPMod.minecraftServer;

public class RegistryEntryTool {
    public static RegistryEntry<Enchantment> getEnchantment(RegistryKey<Enchantment> enchantment) {
        return minecraftServer.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(enchantment).get();
    }

    public static RegistryEntry<ArmorTrimMaterial> getMaterial(RegistryKey<ArmorTrimMaterial> material) {
        return minecraftServer.getRegistryManager().get(RegistryKeys.TRIM_MATERIAL).getEntry(material).get();
    }

    public static RegistryEntry<ArmorTrimPattern> getPattern(RegistryKey<ArmorTrimPattern> pattern) {
        return minecraftServer.getRegistryManager().get(RegistryKeys.TRIM_PATTERN).getEntry(pattern).get();
    }
}
