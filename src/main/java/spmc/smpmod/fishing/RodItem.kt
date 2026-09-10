package spmc.smpmod.fishing

import eu.pb4.polymer.core.api.item.PolymerItem
import net.fabricmc.fabric.api.networking.v1.context.PacketContext
import net.minecraft.ChatFormatting
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.FishingRodItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent
import kotlin.math.min

class RodItem(properties: Properties, @JvmField val tier: RodTiers): FishingRodItem(properties.stacksTo(1).durability(tier.durability).repairable(tier.getStack())), PolymerItem {
	override fun getPolymerItem(itemStack: ItemStack, context: PacketContext) = Items.FISHING_ROD
	override fun getPolymerItemModel(stack: ItemStack, context: PacketContext, lookup: HolderLookup.Provider?) = BuiltInRegistries.ITEM.getKey(Items.FISHING_ROD)
	override fun getName(itemStack: ItemStack) = Component.literal("$tier Rod").withColor(tier.color).withStyle { style -> style.withItalic(false) }


	fun canVoidFish() = this.tier.ordinal >= RodTiers.CELESTIAL.ordinal || this.tier == RodTiers.AIR // TODO: make fishing better by limiting
	fun canLavaFish() = (this.tier == RodTiers.NETHERITE || this.tier.ordinal >= RodTiers.CELESTIAL.ordinal)

	override fun modifyBasePolymerItemStack(out: ItemStack, stack: ItemStack, context: PacketContext, lookup: HolderLookup.Provider) {
		out.set(DataComponents.CUSTOM_NAME, getName(out))
		out.set(DataComponents.LORE, ItemLore(buildLore()))
		val glint = tier.catchLuckBonus >= 1.3
		stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glint)
		out.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glint)
	}

	override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
		val itemStack = player.getItemInHand(hand)
		if (player.fishing == null) {
			level.playSound(null, player.x, player.y, player.z, SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, .5f, .4f / (level.getRandom().nextFloat() * .4f + .8f))
			if (level is ServerLevel) Projectile.spawnProjectile(FishingHook(player, level, 0, min(500, tier.lureSpeed * 20)), level, itemStack)

			player.awardStat(Stats.ITEM_USED.get(this))
			itemStack.causeUseVibration(player, GameEvent.ITEM_INTERACT_START)
			itemStack.hurtAndBreak(1, player, hand.asEquipmentSlot())
		}
		return InteractionResult.SUCCESS
	}

	private fun buildLore(): MutableList<Component> {
		val list: MutableList<Component> = mutableListOf(Component.literal(String.format("Luck Bonus: +%.0f%%", (tier.catchLuckBonus - 1.0f) * 100)).withStyle(ChatFormatting.GREEN).withStyle { style -> style.withItalic(false) }, Component.literal(String.format("Easy Reel Zone: %.0f%%", tier.greenZoneSize * 100)).withStyle(ChatFormatting.AQUA).withStyle{ style -> style.withItalic(false) })
		if (canLavaFish()) list.add(Component.literal("This rod can be used to fish in lava!").withStyle(ChatFormatting.RED).withStyle { style -> style.withItalic(false) })
		if (canVoidFish()) list.add(Component.literal("This rod can be used to fish in the void!").withStyle(ChatFormatting.DARK_GRAY).withStyle { style -> style.withItalic(false) })
		list.add(Component.empty())
		list.add(Component.literal("Use in water to start fishing!").withStyle(ChatFormatting.DARK_GRAY))
		return list
	}
}
