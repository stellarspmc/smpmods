package spmc.smpmod.registry

import spmc.smpmod.core.ItemRarity
import spmc.smpmod.plant.CropItem
import spmc.smpmod.plant.SeedBlock
import spmc.smpmod.plant.SeedItem
import spmc.smpmod.utils.MessageUtils
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.state.BlockBehaviour

object PlantRegistry {
    val SEEDS: HashMap<String, SeedItem> = HashMap()

    private fun getBaseSeed(baseCrop: Item): Item = when (baseCrop) {
		Items.WHEAT -> Items.WHEAT_SEEDS
	    Items.CARROT -> Items.CARROT
	    Items.POTATO -> Items.POTATO
	    Items.BEETROOT -> Items.BEETROOT_SEEDS
	    Items.TORCHFLOWER -> Items.TORCHFLOWER_SEEDS
	    Items.MELON -> Items.MELON_SEEDS
	    Items.PUMPKIN -> Items.PUMPKIN_SEEDS
	    else -> Items.AIR
	}

    private fun registerPlant(cropId: String, baseCrop: Item, basePrice: Double, rarity: ItemRarity) {
        val baseSeed = getBaseSeed(baseCrop)
        val cropItem = PolymerRegistry.createItem(cropId) { properties -> CropItem(properties, baseCrop, MessageUtils.formatName(cropId), basePrice, rarity) }
        val seedBlock = PolymerRegistry.createBlockOnly(cropId + "_crop", { properties -> SeedBlock(properties) { cropItem } }, BlockBehaviour.Properties.of()) as SeedBlock
        val seedItem = PolymerRegistry.createItem(cropId + "_seeds") { properties -> SeedItem(seedBlock, properties, baseSeed, MessageUtils.formatName(cropId + "_seeds")) }
        SEEDS.putIfAbsent(cropId, seedItem)
    }

    fun getItem(id: String): SeedItem? = SEEDS.getOrDefault(id, null)
    

    internal fun register() {
        registerPlant("wheat", Items.WHEAT, 1.0, ItemRarity.COMMON)
        registerPlant("beetroot", Items.BEETROOT, 2.0, ItemRarity.COMMON)
    }
}
