package fun.spmc.smpmod.events.mobs;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import fun.spmc.smpmod.events.ServerMob;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Objects;
import java.util.UUID;

public class EyeBoss implements ServerMob {
    @Override public EntityType<? extends LivingEntity> getEntityType() { return EntityTypes.ZOMBIE; }
    @Override public double entitySpawnRate() { return .9978; }
    @Override public Component getEntityName() { return Component.literal("eyelol").withStyle(ChatFormatting.RED, ChatFormatting.BOLD); }

    @Override
    public void setHead(LivingEntity entity) {
        UUID headUuid = UUID.fromString("ceac9936-06bd-4d08-91ef-91f230099378");

        String textureValue = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWE5NTMzZGM1ZGI4MzUwMmY4MTYyOWQ1MDVmOTJhOGE1Y2ZjNGIyNDExYjQzNDJmOWQxNjU3ZDc3NTViYjZhNiJ9fX0=";
        Multimap<String, Property> propertiesMultimap = HashMultimap.create();
        propertiesMultimap.put("textures", new Property("textures", textureValue));

        PropertyMap properties = new PropertyMap(propertiesMultimap);

        GameProfile gameProfile = new GameProfile(headUuid, "eyelol", properties);

        ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);
        playerHead.set(DataComponents.PROFILE, ResolvableProfile.createResolved(gameProfile));
        entity.setItemSlot(EquipmentSlot.HEAD, playerHead);
        ((Mob) entity).setDropChance(EquipmentSlot.HEAD, .15f);
    }

    @Override
    public void setChest(LivingEntity entity) {
        var enchantmentRegistry = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        chestplate.enchant(enchantmentRegistry.getOrThrow(Enchantments.PROTECTION), 8);
        entity.setItemSlot(EquipmentSlot.CHEST, chestplate);
        ((Mob) entity).setDropChance(EquipmentSlot.CHEST, .01f);
    }

    @Override
    public void setLegs(LivingEntity entity) {
        var enchantmentRegistry = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        ItemStack leggings = new ItemStack(Items.DIAMOND_LEGGINGS);
        leggings.enchant(enchantmentRegistry.getOrThrow(Enchantments.PROTECTION), 8);
        entity.setItemSlot(EquipmentSlot.LEGS, leggings);
        ((Mob) entity).setDropChance(EquipmentSlot.LEGS, .01f);
    }

    @Override
    public void setBoots(LivingEntity entity) {
        var enchantmentRegistry = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
        boots.enchant(enchantmentRegistry.getOrThrow(Enchantments.PROTECTION), 8);
        entity.setItemSlot(EquipmentSlot.FEET, boots);
        ((Mob) entity).setDropChance(EquipmentSlot.FEET, .01f);
    }

    @Override
    public void setItems(LivingEntity entity) {
        var enchantmentRegistry = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        ItemStack weapon = new ItemStack(Items.NETHERITE_HOE);
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Identifier.fromNamespaceAndPath("minecraft", "effect.attack_damage"), 8, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();

        weapon.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);

        entity.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        ((Mob) entity).setDropChance(EquipmentSlot.MAINHAND, 0);

        ItemStack drop = new ItemStack(Items.DIRT);
        drop.enchant(enchantmentRegistry.getOrThrow(Enchantments.SHARPNESS), 8);
        drop.enchant(enchantmentRegistry.getOrThrow(Enchantments.FIRE_ASPECT), 8);
        drop.set(DataComponents.CUSTOM_NAME, Component.literal("kidney stone"));
        entity.setItemSlot(EquipmentSlot.SADDLE, drop);
        ((Mob) entity).setDropChance(EquipmentSlot.SADDLE, .1f);
    }

    @Override
    public void setEffects(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE,
                MobEffectInstance.INFINITE_DURATION,
                0,
                false,
                false
        ));

    }

    @Override
    public void setAttributes(LivingEntity entity) {
        Objects.requireNonNull(entity.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(50);
        entity.setHealth(50f);
    }
}
