package fun.spmc.smpmod.events.mobs;

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

public class NickBoss implements ServerMob {
    @Override public EntityType<? extends LivingEntity> getEntityType() { return EntityTypes.ZOMBIE; }
    @Override public double entitySpawnRate() { return .991; }
    @Override public Component getEntityName() { return Component.literal("Nickwong0910").withStyle(ChatFormatting.GREEN); }

    @Override
    public void setHead(LivingEntity entity) {
        ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);
        playerHead.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved("spmc"));
        entity.setItemSlot(EquipmentSlot.HEAD, playerHead);
        ((Mob) entity).setDropChance(EquipmentSlot.HEAD, 1f);
    }

    @Override public void setChest(LivingEntity entity) {}
    @Override public void setLegs(LivingEntity entity) {}
    @Override public void setBoots(LivingEntity entity) {}
    @Override public void setAttributes(LivingEntity entity) {}

    @Override
    public void setItems(LivingEntity entity) {
        var enchantmentRegistry = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        ItemStack weapon = new ItemStack(Items.NETHERITE_BLOCK);
        ItemAttributeModifiers mod = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Identifier.fromNamespaceAndPath("minecraft", "effect.attack_damage"), 15, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();

        weapon.set(DataComponents.ATTRIBUTE_MODIFIERS, mod);
        entity.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        ((Mob) entity).setDropChance(EquipmentSlot.MAINHAND, 0);

        ItemStack drop = new ItemStack(Items.MILK_BUCKET);
        drop.enchant(enchantmentRegistry.getOrThrow(Enchantments.INFINITY), 1);
        drop.set(DataComponents.CUSTOM_NAME, Component.literal("nick's cum"));
        entity.setItemSlot(EquipmentSlot.SADDLE, drop);
        ((Mob) entity).setDropChance(EquipmentSlot.SADDLE, 0.4f);
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
}
