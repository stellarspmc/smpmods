package fun.spmc.smpmod.fishing.rod;

import fun.spmc.smpmod.utils.SimplerPolymerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class RodItem extends SimplerPolymerItem {
    private final RodTiers tier;

    public RodItem(Properties settings, RodTiers tier) {
        super(settings.stacksTo(1).durability(tier.getDurability()), Items.FISHING_ROD);
        this.tier = tier;
    }

    public RodTiers getTier() {
        return tier;
    }

    @Override
    public Component buildName(ItemStack stack) {
        return Component.literal(tier.getName() + " Fishing Rod").withStyle(tier.getColor()).withStyle(style -> style.withItalic(false));
    }

    @Override
    public List<Component> buildLore(ItemStack stack) {
        return List.of(
                Component.literal("Tier: ").withStyle(ChatFormatting.GRAY).withStyle(style -> style.withItalic(false))
                        .append(Component.literal(tier.getName()).withStyle(tier.getColor())).withStyle(style -> style.withItalic(false)),
                Component.literal(String.format("Luck Bonus: +%.0f%%", (tier.getCatchLuckBonus() - 1.0f) * 100))
                        .withStyle(ChatFormatting.GREEN).withStyle(style -> style.withItalic(false)),
                Component.literal(String.format("Easy Reel Zone: %.0f%%", tier.getGreenZoneSize() * 100))
                        .withStyle(ChatFormatting.AQUA).withStyle(style -> style.withItalic(false)),
                Component.empty(),
                Component.literal("Use in water to start fishing!").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
        );
    }

    @Override
    public void modifyItem(ItemStack stack) {
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    @Override
    public @NonNull InteractionResult use(final @NonNull Level level, final Player player, final @NonNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (player.fishing == null) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL,
                    .5f, .4f / (level.getRandom().nextFloat() * .4f + .8f));
            if (level instanceof ServerLevel serverLevel) {
                int lureSpeed = (int)(EnchantmentHelper.getFishingTimeReduction(serverLevel, itemStack, player) * 20.0F);
                int luck = EnchantmentHelper.getFishingLuckBonus(serverLevel, itemStack, player);
                Projectile.spawnProjectile(new FishingHook(player, level, luck, lureSpeed), serverLevel, itemStack);
            }

            player.awardStat(Stats.ITEM_USED.get(this));
            itemStack.causeUseVibration(player, GameEvent.ITEM_INTERACT_START);
            itemStack.hurtAndBreak(1, player, hand.asEquipmentSlot());
        }
        return InteractionResult.SUCCESS;
    }
}