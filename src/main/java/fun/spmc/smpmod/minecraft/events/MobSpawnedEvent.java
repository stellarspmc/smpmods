package fun.spmc.smpmod.minecraft.events;

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

public class MobSpawnedEvent {
    public static void onEntityJoin(Entity entity, ServerLevel level) {
        if (entity instanceof Zombie zombie) {
            if (zombie.getRandom().nextFloat() >= .978f) spawnEyeBoss(zombie, level);
            else if (zombie.getRandom().nextFloat() >= .911f) spawnNickBoss(zombie);
        }
    }

    private static void spawnEyeBoss(Zombie boss, ServerLevel level) {
        boss.setCustomName(Component.literal("eyelol").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        boss.setCustomNameVisible(true);
        boss.setPersistenceRequired();

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

        ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);
        playerHead.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved("eyelol"));
        playerHead.enchant(protectionEnchant, 8);
        boss.setItemSlot(EquipmentSlot.HEAD, playerHead);
        boss.setDropChance(EquipmentSlot.HEAD, 1);

        ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        chestplate.enchant(protectionEnchant, 8);
        boss.setItemSlot(EquipmentSlot.CHEST, chestplate);
        boss.setDropChance(EquipmentSlot.CHEST, 0);

        ItemStack leggings = new ItemStack(Items.DIAMOND_LEGGINGS);
        leggings.enchant(protectionEnchant, 8);
        boss.setItemSlot(EquipmentSlot.LEGS, leggings);
        boss.setDropChance(EquipmentSlot.LEGS, 0);

        ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
        boots.enchant(protectionEnchant, 8);
        boss.setItemSlot(EquipmentSlot.FEET, boots);
        boss.setDropChance(EquipmentSlot.FEET, 0);

        ItemStack weapon = new ItemStack(Items.NETHERITE_HOE);
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Identifier.fromNamespaceAndPath("minecraft", "effect.attack_damage"), 8, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();

        weapon.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);

        boss.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        boss.setDropChance(EquipmentSlot.MAINHAND, 0);
    }

    private static void spawnNickBoss(Zombie boss) {
        boss.setCustomName(Component.literal("Nickwong0910").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        boss.setCustomNameVisible(true);
        boss.setPersistenceRequired();

        Objects.requireNonNull(boss.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(35);
        boss.setHealth(35f);


        ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);
        playerHead.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved("spmc"));
        boss.setItemSlot(EquipmentSlot.HEAD, playerHead);
        boss.setDropChance(EquipmentSlot.HEAD, 1f);

        ItemStack weapon = new ItemStack(Items.NETHERITE_BLOCK);
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Identifier.fromNamespaceAndPath("minecraft", "effect.attack_damage"), 15, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();

        weapon.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);

        boss.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        boss.setDropChance(EquipmentSlot.MAINHAND, 0);
    }
}
