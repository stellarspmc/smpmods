package ml.spmc.smpmod.minecraft.modpack.impl;

import ml.spmc.smpmod.utils.RegistryEntryTool;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKey;

public class ServerItem {
    public ItemStack createItem(String id, Item material) {
        ItemStack stack = new ItemStack(material);
        NbtCompound customNBT = new NbtCompound();
        customNBT.put("ModdedID", NbtString.of("spmc:" + id));
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customNBT));
        return stack;
    }

    public ItemStack addGlint(ItemStack item) {
        item.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return item;
    }

    public ItemStack addEnchantment(ItemStack item, RegistryKey<Enchantment> ench, int level) {
        item.addEnchantment(RegistryEntryTool.getEnchantment(ench), level);
        return item;
    }

    /* so scary
    public ItemStack setAttribute(ItemStack item, EntityAttribute attribute, double value, EntityAttributeModifier.Operation operation, EquipmentSlot slot) {
        item.applyAttributeModifier(attribute, new EntityAttributeModifier(Identifier.of("spmc", "abc"), value, operation), slot);
        return item;
    }

    public ItemStack setAttributeAllSlot(ItemStack item, EntityAttribute attribute, double value, EntityAttributeModifier.Operation operation) {
        item.applyAttributeModifier(attribute, new EntityAttributeModifier("abc", value, operation), EquipmentSlot.CHEST);
        item.applyAttributeModifier(attribute, new EntityAttributeModifier("abc", value, operation), EquipmentSlot.FEET);
        item.applyAttributeModifier(attribute, new EntityAttributeModifier("abc", value, operation), EquipmentSlot.HEAD);
        item.applyAttributeModifier(attribute, new EntityAttributeModifier("abc", value, operation), EquipmentSlot.OFFHAND);
        item.applyAttributeModifier(attribute, new EntityAttributeModifier("abc", value, operation), EquipmentSlot.MAINHAND);
        item.applyAttributeModifier(attribute, new EntityAttributeModifier("abc", value, operation), EquipmentSlot.LEGS);
        return item;
    }
     */
}
