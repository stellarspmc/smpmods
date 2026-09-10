package spmc.smpmod.economy.fluctuate

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.RandomSource
import net.minecraft.world.item.Item
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

class FluctuationData @JvmOverloads constructor(val mineral: Item, @JvmField var defaultPrice: Double, var fluctuation: Double, var amountDeposited: Long = 0, var amountWithdrawn: Long = 0) {
	private var lastTransactionTime = System.currentTimeMillis()

	fun getBasePriceAt(netDemand: Long) = (max(defaultPrice * (1 + ((netDemand / SATURATION_VOLUME) * fluctuation)), .0) * 100.0).roundToLong() / 100.0
	val currentPrice: Double get() = getBasePriceAt(amountWithdrawn - amountDeposited)

	fun getBulkBuyCost(amount: Int): Double {
		val currentNet = amountWithdrawn - amountDeposited
		return (((getBasePriceAt(currentNet) * BUY_MARGIN + getBasePriceAt(currentNet + amount) * BUY_MARGIN) / 2) * amount * 100.0).roundToLong() / 100.0
	}

	fun getBulkSellPayout(amount: Int): Double {
		val currentNet = amountWithdrawn - amountDeposited
		val startPrice: Double = getBasePriceAt(currentNet) * SELL_MARGIN
		val endPrice: Double = getBasePriceAt(currentNet - amount) * SELL_MARGIN

		val avgPrice = (startPrice + endPrice) / 2
		return (avgPrice * amount * 100.0).roundToLong() / 100.0
	}

	fun deposit(amount: Long) {
		if (amount <= 0) return
		this.amountDeposited = Math.addExact(this.amountDeposited, amount)
		this.lastTransactionTime = System.currentTimeMillis()
	}

	fun withdraw(amount: Long) {
		if (amount <= 0) return
		this.amountWithdrawn = Math.addExact(this.amountWithdrawn, amount)
		this.lastTransactionTime = System.currentTimeMillis()
	}

	fun applyMarketDecay(source: RandomSource): Boolean {
		if ((System.currentTimeMillis() - this.lastTransactionTime) >= 150000 && (amountDeposited >= 0 || amountWithdrawn >= 0)) {
			amountDeposited = processFluctuation(amountDeposited, source.nextFloat() < 0.60f)
			amountWithdrawn = processFluctuation(amountWithdrawn, source.nextFloat() < 0.60f)
			return true
		}
		return false
	}

	private fun processFluctuation(currentAmount: Long, moveTowardsBase: Boolean): Long {
		if (currentAmount < 0) return 0
		var newAmount: Long
		if (moveTowardsBase) {
			if (currentAmount <= 30) return currentAmount + 15
			newAmount = (currentAmount * 0.99).toLong()
			if (newAmount == currentAmount) newAmount--
		} else {
			newAmount = (currentAmount * 1.01).toLong()
			if (newAmount == currentAmount) newAmount++
			newAmount = min(100000L, newAmount)
		}

		return max(0, newAmount)
	}

	companion object {
		val CODEC: Codec<FluctuationData> = RecordCodecBuilder.create { instance -> instance.group(BuiltInRegistries.ITEM.byNameCodec().fieldOf("mineral").forGetter(FluctuationData::mineral), Codec.DOUBLE.fieldOf("default_price").forGetter(FluctuationData::defaultPrice), Codec.DOUBLE.fieldOf("fluctuation").forGetter(FluctuationData::fluctuation), Codec.LONG.fieldOf("amount_deposit").forGetter(FluctuationData::amountDeposited), Codec.LONG.fieldOf("amount_withdraw").forGetter(FluctuationData::amountWithdrawn)).apply(instance, ::FluctuationData) }

		private const val SATURATION_VOLUME = 1000.0
		private var BUY_MARGIN = 1.15
		private var SELL_MARGIN = .85

		@JvmStatic
		fun changeMargin(percentage: Double) {
			BUY_MARGIN = 1.15 * percentage
			SELL_MARGIN = .85 / percentage
		}
	}
}