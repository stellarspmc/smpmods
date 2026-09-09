package `fun`.spmc.smpmod.registry

import `fun`.spmc.smpmod.core.ItemRarity
import `fun`.spmc.smpmod.plant.CropItem
import `fun`.spmc.smpmod.plant.SeedBlock
import `fun`.spmc.smpmod.plant.SeedItem
import `fun`.spmc.smpmod.utils.MessageUtils
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.state.BlockBehaviour

object PlantRegistry {
    val SEEDS: HashMap<String, SeedItem> = HashMap()

    private fun getBaseSeed(baseCrop: Item): Item {
        if (baseCrop === Items.WHEAT) return Items.WHEAT_SEEDS
        if (baseCrop === Items.CARROT) return Items.CARROT
        if (baseCrop === Items.POTATO) return Items.POTATO
        if (baseCrop === Items.BEETROOT) return Items.BEETROOT_SEEDS
        if (baseCrop === Items.TORCHFLOWER) return Items.TORCHFLOWER_SEEDS
        if (baseCrop === Items.MELON) return Items.MELON_SEEDS
        if (baseCrop === Items.PUMPKIN) return Items.PUMPKIN_SEEDS
        return Items.AIR
    }

    private fun registerPlant(cropId: String, baseCrop: Item, basePrice: Double, rarity: ItemRarity) {
        val baseSeed = getBaseSeed(baseCrop)
        val cropItem: CropItem = PolymerRegistry.createItem(cropId) { properties -> CropItem(properties, baseCrop, MessageUtils.formatName(cropId), basePrice, rarity) }
        val seedBlock: SeedBlock = PolymerRegistry.createBlockOnly(cropId + "_crop", { properties -> SeedBlock(properties) { cropItem } }, BlockBehaviour.Properties.of()) as SeedBlock
        val seedItem: SeedItem = PolymerRegistry.createItem(cropId + "_seeds") { properties -> SeedItem(seedBlock, properties, baseSeed, MessageUtils.formatName(cropId + "_seeds")) }
        SEEDS.putIfAbsent(cropId, seedItem)
    }

    fun getItem(id: String): SeedItem? {
        return SEEDS.getOrDefault(id, null)
    }

    internal fun register() {
        registerPlant("wheat", Items.WHEAT, 1.0, ItemRarity.COMMON)
        registerPlant("beetroot", Items.BEETROOT, 2.0, ItemRarity.COMMON)
    }
}
