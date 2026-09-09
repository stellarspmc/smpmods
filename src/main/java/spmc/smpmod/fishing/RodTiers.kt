package spmc.smpmod.fishing

import net.minecraft.network.chat.TextColor
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import spmc.smpmod.core.ItemModifier
import spmc.smpmod.registry.FishingRegistry.getFish
import java.util.*
import java.util.function.Supplier

enum class RodTiers(@JvmField val color: TextColor, @JvmField val durability: Int, @JvmField val catchLuckBonus: Float, @JvmField val greenZoneSize: Float, // 8 terms EXACT
                    @JvmField val rates: DoubleArray, // reduction, in seconds
                    @JvmField val lureSpeed: Int, @JvmField val obtainable: Array<ItemModifier>, private val stack: Supplier<Item>) {
	NORMAL(TextColor.fromRgb(0x706E6B), 34, 1f, .23f, doubleArrayOf(78.0, 18.0, 3.5, .45, .045, .004, .0008, .0002), 1, arrayOf<ItemModifier>(), { Items.STRING }),
	RAINBOW(TextColor.fromRgb(0xFF70A6), 102, 1.15f, .2f, doubleArrayOf(70.0, 20.0, 8.0, 1.6, .35, .04, .008, .002), 1, arrayOf(ItemModifier.COLORFUL), { Items.WOOL.white() }),
	COPPER(TextColor.fromRgb(0xE07A5F), 89, 1.1f, .22f, doubleArrayOf(68.0, 23.0, 7.5, 1.2, .25, .04, .008, .002), 1, arrayOf<ItemModifier>(), { Items.COPPER_INGOT }),
	IRON(TextColor.fromRgb(0xD0D7DC), 177, 1.15f, .25f, doubleArrayOf(56.0, 27.0, 12.0, 3.8, .9, .2, .08, .02), 3, arrayOf<ItemModifier>(), { Items.IRON_BLOCK }),
	GOLD(TextColor.fromRgb(0xFFD700), 48, 1.6f, .33f, doubleArrayOf(44.0, 30.0, 17.0, 6.5, 2.0, .4, .08, .02), 8, arrayOf(ItemModifier.GOLDEN), { Items.GOLD_BLOCK }),
	EMERALD(TextColor.fromRgb(0x2ECC71), 259, 1.3f, .3f, doubleArrayOf(34.0, 31.0, 21.0, 10.0, 3.2, .65, .12, .03), 5, arrayOf(), { Items.EMERALD_BLOCK }),
	LUNA(TextColor.fromRgb(0x9B59B6), 152, 1.65f, .35f, doubleArrayOf(28.0, 42.0, 21.0, 8.0, .8, .19, .0075, .0025), 6, arrayOf(), { Items.STRING }),
	DIAMOND(TextColor.fromRgb(0x3498DB), 533, 1.45f, .33f, doubleArrayOf(25.0, 32.0, 25.0, 12.5, 4.2, .95, .3, .05), 5, arrayOf(ItemModifier.CRYSTALLIZED), { Items.DIAMOND_BLOCK }),
	NETHERITE(TextColor.fromRgb(0x4A3B4E), 1007, 1.8f, .35f, doubleArrayOf(18.0, 28.0, 31.0, 15.0, 5.0, 2.0, .8, .2), 6, arrayOf(), { Items.NETHERITE_BLOCK }),
	TOXIC(TextColor.fromRgb(0x39FF14), 850, 1.75f, .32f, doubleArrayOf(20.0, 28.0, 30.0, 14.0, 5.3, 2.0, .5, .2), 6, arrayOf(ItemModifier.NUCLEAR), { getFish("poisonquill") }),
	DEATH(TextColor.fromRgb(0x800020), 666, 1.9f, .28f, doubleArrayOf(15.0, 25.0, 32.0, 16.0, 8.0, 2.8, 1.0, .2), 7, arrayOf(ItemModifier.EVIL), { getFish("reaper") }),
	AIR(TextColor.fromRgb(0xA0E7E5), 751, 1.7f, .3f, doubleArrayOf(22.0, 30.0, 28.0, 12.0, 5.0, 2.1, .7, .2), 10, arrayOf(ItemModifier.SPEEDY), { getFish("aerie") }),
	SEA(TextColor.fromRgb(0x0077B6), 717, 1.85f, .36f, doubleArrayOf(16.0, 26.0, 32.0, 15.5, 6.0, 2.5, 1.2, .2), 7, arrayOf(ItemModifier.BRUCED), { getFish("poseidon") }),
	FLICKERING(TextColor.fromRgb(0xFFD166), 549, 2f, .3f, doubleArrayOf(14.0, 24.0, 30.0, 18.0, 8.0, 3.5, 2.0, .5), 8, arrayOf(ItemModifier.GOLDEN), { getFish("prismite") }),
	CELESTIAL(TextColor.fromRgb(0x9D4EDD), 1211, 2.2f, .37f, doubleArrayOf(10.0, 20.0, 32.0, 20.0, 11.0, 4.5, 2.0, .5), 9, arrayOf(), { getFish("cor") }),
	ELEMENTAL(TextColor.fromRgb(0xFF5722), 1496, 2.6f, .48f, doubleArrayOf(6.0, 14.0, 30.0, 24.0, 15.0, 7.0, 3.2, .8), 12, arrayOf(ItemModifier.GODLY), { getFish("hades") }),
	CTHULHU(TextColor.fromRgb(0x00F5D4), 1574, 2.7f, .48f, doubleArrayOf(5.0, 13.0, 28.0, 25.0, 16.0, 8.0, 4.0, 1.0), 14, arrayOf(ItemModifier.SPEEDY), { getFish("cthulhu") }),
	EVERYTHING(TextColor.fromRgb(0xE056FD), 1689, 3.2f, .4f, doubleArrayOf(2.0, 8.9, 21.8, 28.0, 22.0, 12.0, 4.2, 1.1), 16, arrayOf(ItemModifier.GODLY, ItemModifier.ULTIMATE), { getFish("fish_de_ckc") });

	override fun toString(): String = name[0].toString() + name.substring(1).lowercase(Locale.getDefault())
	fun getStack(): Item = stack.get()

}
