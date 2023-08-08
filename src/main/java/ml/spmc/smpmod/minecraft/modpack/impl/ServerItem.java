package ml.spmc.smpmod.minecraft.modpack.impl;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

public class ServerItem {
    public ItemStack createItem(String id, Item material) {
        ItemStack stack = new ItemStack(material);
        NbtCompound customNBT = stack.getOrCreateNbt();
        customNBT.put("ModdedID", NbtString.of("spmc:" + id));
        return stack;
    }

    public ItemStack addGlint(ItemStack item) {
        item.addEnchantment(Enchantments.UNBREAKING, 0);
        item.addHideFlag(ItemStack.TooltipSection.ENCHANTMENTS);
        return item;
    }

    public ItemStack addEnchantment(ItemStack item, Enchantment ench, int level) {
        item.addEnchantment(ench, level);
        return item;
    }

    public ItemStack setAttribute(ItemStack item, EntityAttribute attribute, double value, EntityAttributeModifier.Operation operation, EquipmentSlot slot) {
        item.addAttributeModifier(attribute, new EntityAttributeModifier("abc", value, operation), slot);
        return item;
    }

    public ItemStack setAttributeAllSlot(ItemStack item, EntityAttribute attribute, double value, EntityAttributeModifier.Operation operation) {
        item.addAttributeModifier(attribute, new EntityAttributeModifier("abc", value, operation), EquipmentSlot.CHEST);
        item.addAttributeModifier(attribute, new EntityAttributeModifier("abc", value, operation), EquipmentSlot.FEET);
        item.addAttributeModifier(attribute, new EntityAttributeModifier("abc", value, operation), EquipmentSlot.HEAD);
        item.addAttributeModifier(attribute, new EntityAttributeModifier("abc", value, operation), EquipmentSlot.OFFHAND);
        item.addAttributeModifier(attribute, new EntityAttributeModifier("abc", value, operation), EquipmentSlot.MAINHAND);
        item.addAttributeModifier(attribute, new EntityAttributeModifier("abc", value, operation), EquipmentSlot.LEGS);
        return item;
    }
}
