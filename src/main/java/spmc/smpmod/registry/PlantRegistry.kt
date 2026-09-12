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

    private fun getBaseSeed(baseCrop: Item) = when (baseCrop) {
		Items.WHEAT -> Items.WHEAT_SEEDS
	    Items.CARROT -> Items.CARROT
	    Items.POTATO -> Items.POTATO
	    Items.BEETROOT -> Items.BEETROOT_SEEDS
	    Items.TORCHFLOWER -> Items.TORCHFLOWER_SEEDS
	    Items.MELON -> Items.MELON_SEEDS
	    Items.PUMPKIN -> Items.PUMPKIN_SEEDS
	    Items.SWEET_BERRIES -> Items.SWEET_BERRIES
	    Items.CHORUS_FRUIT -> Items.CHORUS_FLOWER // or CHORUS_PLANT?
	    else -> Items.AIR
	}

    private fun registerPlant(cropId: String, baseCrop: Item, basePrice: Double, rarity: ItemRarity) {
        val baseSeed = getBaseSeed(baseCrop)
        val cropItem = PolymerRegistry.createItem(cropId) { CropItem(it, baseCrop, MessageUtils.formatName(cropId), basePrice, rarity) }
        val seedBlock = PolymerRegistry.createBlockOnly(cropId + "_crop", { SeedBlock(it) { cropItem }}, BlockBehaviour.Properties.of()) as SeedBlock
        val seedItem = PolymerRegistry.createItem(cropId + "_seeds") { SeedItem(seedBlock, it, baseSeed, MessageUtils.formatName(cropId + "_seeds")) }
        SEEDS.putIfAbsent(cropId, seedItem)
    }

    fun getItem(id: String): SeedItem? = SEEDS.getOrDefault(id, null)
    

    internal fun register() {
        registerPlant("wheat", Items.WHEAT, 1.0, ItemRarity.COMMON)
        registerPlant("beetroot", Items.BEETROOT, 2.0, ItemRarity.COMMON)
	    registerPlant("carrot", Items.CARROT, 3.6, ItemRarity.COMMON)
	    registerPlant("berry", Items.SWEET_BERRIES, 4.5, ItemRarity.COMMON)
	    registerPlant("potato", Items.POTATO, 6.0, ItemRarity.COMMON)

	    registerPlant("strawberry", Items.SWEET_BERRIES, 14.0, ItemRarity.UNCOMMON)
	    registerPlant("pumpkin", Items.PUMPKIN, 24.6, ItemRarity.UNCOMMON)
	    registerPlant("melon", Items.MELON, 22.5, ItemRarity.UNCOMMON)
	    registerPlant("carrot", Items.CARROT, 3.6, ItemRarity.COMMON)

	    registerPlant("torch_flower", Items.SWEET_BERRIES, 45.0, ItemRarity.RARE)
	    registerPlant("cucumber", Items.MELON, 67.5, ItemRarity.RARE)
	    registerPlant("eggplant", Items.TORCHFLOWER, 69.0, ItemRarity.RARE) // 3522-eggplant
	    registerPlant("piranha", Items.TORCHFLOWER, 69.0, ItemRarity.RARE) // 127088-piranha-plant OR 127087-piranha-plant-facing-up
	    registerPlant("life_mushroom", Items.RED_MUSHROOM, 72.0, ItemRarity.RARE) // 128174-life-mushroom
	    registerPlant("rose", Items.ROSE_BUSH, 80.0, ItemRarity.RARE) // 126801-rose
    }
}
