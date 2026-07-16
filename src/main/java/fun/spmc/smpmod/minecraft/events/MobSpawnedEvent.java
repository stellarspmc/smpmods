package fun.spmc.smpmod.minecraft.events;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Objects;
import java.util.UUID;

public class MobSpawnedEvent {
    public static void onEntityJoin(Entity entity, ServerLevel level) {
        if (entity instanceof Zombie zombie) {
            if (zombie.getRandom().nextFloat() >= .9978f) spawnEyeBoss(zombie, level);
            else if (zombie.getRandom().nextFloat() >= .9931f) spawnNickBoss(zombie, level);
        }
    }

    private static void spawnEyeBoss(Zombie boss, ServerLevel level) {
        boss.setCustomName(Component.literal("eyelol").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

        Objects.requireNonNull(boss.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(50);
        boss.setHealth(50f);

        boss.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE,
                MobEffectInstance.INFINITE_DURATION,
                0,
                false,
                false
        ));

        var enchantmentRegistry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var protectionEnchant = enchantmentRegistry.getOrThrow(Enchantments.PROTECTION);

        UUID headUuid = UUID.fromString("ceac9936-06bd-4d08-91ef-91f230099378");

        String textureValue = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWE5NTMzZGM1ZGI4MzUwMmY4MTYyOWQ1MDVmOTJhOGE1Y2ZjNGIyNDExYjQzNDJmOWQxNjU3ZDc3NTViYjZhNiJ9fX0=";
        Multimap<String, Property> propertiesMultimap = HashMultimap.create();
        propertiesMultimap.put("textures", new Property("textures", textureValue));

        PropertyMap properties = new PropertyMap(propertiesMultimap);

        GameProfile gameProfile = new GameProfile(headUuid, "eyelol", properties);

// 3. Create the ItemStack and apply the profile component
        ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);
        playerHead.set(DataComponents.PROFILE, ResolvableProfile.createResolved(gameProfile));
        boss.setItemSlot(EquipmentSlot.HEAD, playerHead);
        boss.setDropChance(EquipmentSlot.HEAD, .15f);

        ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        chestplate.enchant(protectionEnchant, 8);
        boss.setItemSlot(EquipmentSlot.CHEST, chestplate);
        boss.setDropChance(EquipmentSlot.CHEST, .01f);

        ItemStack leggings = new ItemStack(Items.DIAMOND_LEGGINGS);
        leggings.enchant(protectionEnchant, 8);
        boss.setItemSlot(EquipmentSlot.LEGS, leggings);
        boss.setDropChance(EquipmentSlot.LEGS, .01f);

        ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
        boots.enchant(protectionEnchant, 8);
        boss.setItemSlot(EquipmentSlot.FEET, boots);
        boss.setDropChance(EquipmentSlot.FEET, .01f);

        ItemStack weapon = new ItemStack(Items.NETHERITE_HOE);
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Identifier.fromNamespaceAndPath("minecraft", "effect.attack_damage"), 8, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();

        weapon.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);

        boss.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        boss.setDropChance(EquipmentSlot.MAINHAND, 0);

        var sharpness = enchantmentRegistry.getOrThrow(Enchantments.SHARPNESS);
        var fire = enchantmentRegistry.getOrThrow(Enchantments.FIRE_ASPECT);

        ItemStack drop = new ItemStack(Items.DIRT);
        drop.enchant(sharpness, 8);
        drop.enchant(fire, 8);
        drop.set(DataComponents.CUSTOM_NAME, Component.literal("kidney stone"));
        boss.setItemSlot(EquipmentSlot.OFFHAND, drop);
        boss.setDropChance(EquipmentSlot.OFFHAND, .1f);
    }

    private static void spawnNickBoss(Zombie boss, ServerLevel level) {
        boss.setCustomName(Component.literal("Nickwong0910").withStyle(ChatFormatting.GREEN));

        Objects.requireNonNull(boss.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(35);
        boss.setHealth(35f);

        ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);
        playerHead.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved("spmc"));
        boss.setItemSlot(EquipmentSlot.HEAD, playerHead);
        boss.setDropChance(EquipmentSlot.HEAD, 1f);

        ItemStack weapon = new ItemStack(Items.NETHERITE_BLOCK);
        ItemAttributeModifiers mod = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Identifier.fromNamespaceAndPath("minecraft", "effect.attack_damage"), 15, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();

        weapon.set(DataComponents.ATTRIBUTE_MODIFIERS, mod);
        boss.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        boss.setDropChance(EquipmentSlot.MAINHAND, 0);

        var enchantmentRegistry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var enchant = enchantmentRegistry.getOrThrow(Enchantments.INFINITY);

        ItemStack drop = new ItemStack(Items.MILK_BUCKET);
        drop.enchant(enchant, 1);
        drop.set(DataComponents.CUSTOM_NAME, Component.literal("nick's cum"));
        boss.setItemSlot(EquipmentSlot.OFFHAND, drop);
        boss.setDropChance(EquipmentSlot.OFFHAND, 0.4f);
    }
}
