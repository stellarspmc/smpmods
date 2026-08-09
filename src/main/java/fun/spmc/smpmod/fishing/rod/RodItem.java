package fun.spmc.smpmod.fishing.rod;

import eu.pb4.polymer.core.api.item.PolymerItem;
import fun.spmc.smpmod.misc.ItemModifier;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class RodItem extends FishingRodItem implements PolymerItem {
    private final RodTiers tier;
    private final Item vanillaItem;

    public RodItem(Properties properties, RodTiers tier) {
        super(properties.stacksTo(1).durability(tier.getDurability()).food(new FoodProperties(0, 0, true), new Consumable(1, ItemUseAnimation.EAT, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SULFUR_CUBE_SMALL_EAT), true, List.of(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 3))))));
        this.vanillaItem = Items.FISHING_ROD;
        this.tier = tier;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return vanillaItem;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return BuiltInRegistries.ITEM.getKey(vanillaItem);
    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        out.set(DataComponents.CUSTOM_NAME, Component.literal(tier.toString() + " Fishing Rod").withStyle(tier.getColor()).withStyle(style -> style.withItalic(false)));
        out.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(String.format("Luck Bonus: +%.0f%%", (tier.getCatchLuckBonus() - 1.0f) * 100))
                        .withStyle(ChatFormatting.GREEN).withStyle(style -> style.withItalic(false)),
                Component.literal(String.format("Easy Reel Zone: %.0f%%", tier.getGreenZoneSize() * 100))
                        .withStyle(ChatFormatting.AQUA).withStyle(style -> style.withItalic(false)),
                Component.empty(),
                Component.literal("Use in water to start fishing!").withStyle(ChatFormatting.DARK_GRAY)
        )));
        out.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    public RodTiers getTier() {
        return tier;
    }

    @Override
    public @NonNull InteractionResult use(final @NonNull Level level, final Player player, final @NonNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (player.fishing == null) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, .5f, .4f / (level.getRandom().nextFloat() * .4f + .8f));
            if (level instanceof ServerLevel serverLevel) Projectile.spawnProjectile(new FishingHook(player, level, 0, Math.min(500, tier.getLureSpeed() * 20)), serverLevel, itemStack);

            player.awardStat(Stats.ITEM_USED.get(this));
            itemStack.causeUseVibration(player, GameEvent.ITEM_INTERACT_START);
            itemStack.hurtAndBreak(1, player, hand.asEquipmentSlot());
        }
        return InteractionResult.SUCCESS;
    }
}