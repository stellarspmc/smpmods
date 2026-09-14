package spmc.smpmod.fishing

import eu.pb4.polymer.core.api.item.PolymerItem
import net.fabricmc.fabric.api.networking.v1.context.PacketContext
import net.minecraft.ChatFormatting
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
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
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent
import spmc.smpmod.core.ItemModifier
import spmc.smpmod.core.TierSystem
import kotlin.math.min

class RodItem(properties: Properties, val name: String, val color: TextColor, vararg val mods: ItemModifier, repaired: Item, val stats: RodStats,
              @JvmField val tier: TierSystem): FishingRodItem(properties.stacksTo(1).durability(stats.durability).repairable(repaired)), PolymerItem {
	override fun getPolymerItem(itemStack: ItemStack, context: PacketContext) = Items.FISHING_ROD
	override fun getPolymerItemModel(stack: ItemStack, context: PacketContext, lookup: HolderLookup.Provider?) = BuiltInRegistries.ITEM.getKey(Items.FISHING_ROD)
	override fun getName(itemStack: ItemStack) = Component.literal("$name Rod").withColor(color).withStyle { it.withItalic(false) }

	override fun modifyBasePolymerItemStack(out: ItemStack, stack: ItemStack, context: PacketContext, lookup: HolderLookup.Provider) {
		out.set(DataComponents.CUSTOM_NAME, getName(out))
		out.set(DataComponents.LORE, ItemLore(buildLore()))
		val glint = tier.ordinal > TierSystem.T5.ordinal
		stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glint)
		out.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glint)
	}

	override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
		val itemStack = player.getItemInHand(hand)
		if (player.fishing == null) {
			level.playSound(null, player.x, player.y, player.z, SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, .5f, .4f / (level.getRandom().nextFloat() * .4f + .8f))
			if (level is ServerLevel) Projectile.spawnProjectile(FishingHook(player, level, 0, min(500, stats.speed * 20)), level, itemStack)

			player.awardStat(Stats.ITEM_USED.get(this))
			itemStack.causeUseVibration(player, GameEvent.ITEM_INTERACT_START)
			itemStack.hurtAndBreak(1, player, hand.asEquipmentSlot())
		}
		return InteractionResult.SUCCESS
	}

	private fun buildLore(): MutableList<Component> {
		val list: MutableList<Component> = mutableListOf(Component.literal(String.format("Luck Bonus: +%.0f%%", (stats.luck - 1f) * 100)).withStyle(ChatFormatting.GREEN).withStyle { it.withItalic(false) }, Component.literal(String.format("Easy Reel Zone: %.0f%%", stats.greenZone * 100)).withStyle(ChatFormatting.AQUA).withStyle{ it.withItalic(false) })
		list.add(Component.literal("T${tier.ordinal + 1} Rod").withColor(tier.getColor()))
		if (stats.lavaFish) list.add(Component.literal("This rod can be used to fish in lava!").withStyle(ChatFormatting.RED).withStyle { it.withItalic(false) })
		if (stats.voidFish) list.add(Component.literal("This rod can be used to fish in the void!").withStyle(ChatFormatting.DARK_GRAY).withStyle { it.withItalic(false) })
		if (stats.skyFish) list.add(Component.literal("This rod can be used to fish in the sky!").withStyle(ChatFormatting.AQUA).withStyle { it.withItalic(false) })
		list.add(Component.empty())
		list.add(Component.literal("Use in water to start fishing!").withStyle(ChatFormatting.DARK_GRAY))
		return list
	}

	data class RodStats(val durability: Int, val luck: Float, val speed: Int, val greenZone: Float, val voidFish: Boolean = false, val lavaFish: Boolean = false, val skyFish: Boolean = false)
}
