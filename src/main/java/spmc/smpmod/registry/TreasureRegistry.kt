package spmc.smpmod.registry

import net.minecraft.ChatFormatting
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.component.Fireworks
import net.minecraft.world.item.enchantment.Enchantments
import spmc.smpmod.core.ItemRarity
import spmc.smpmod.mining.TreasureEntry
import spmc.smpmod.mining.TreasureHelper.Biomes
import java.util.*

object TreasureRegistry {
    private val REGISTRY: MutableList<TreasureEntry> = mutableListOf()
    fun getEligibleTreasures(biome: Biomes, rarity: ItemRarity) = REGISTRY.filter { it.isValid(rarity, biome) }

    private fun add(item: Item): TreasureEntry.Builder {
        val builder = TreasureEntry.Builder(item)
        REGISTRY.add(builder.build())
        return builder
    }

    internal fun register() {
        REGISTRY.clear()
        registerDefault()
        registerOverworld()
        registerNether()
        registerEnd()
    } // TODO: use it instead of repeating level -> (or do a helper function to get enchant, but might slowdown, CAUTION)

    private fun registerDefault() {
	    add(Items.GLASS_BOTTLE).count(8).rarity(ItemRarity.COMMON)
	    add(Items.COBWEB).count(6).rarity(ItemRarity.COMMON)
	    add(Items.IRON_NUGGET).count(3).rarity(ItemRarity.COMMON)
	    add(Items.COPPER_NUGGET).count(12).rarity(ItemRarity.COMMON)
	    add(Items.GOLD_NUGGET).count(2).rarity(ItemRarity.COMMON)
	    add(Items.TORCH).count(8, 12).rarity(ItemRarity.COMMON)
	    add(Items.STICK).count(2, 8).rarity(ItemRarity.COMMON)
	    add(Items.RAIL).count(2, 6).rarity(ItemRarity.COMMON).biome(Biomes.OVERWORLD)
	    add(Items.STRING).count(4, 6).rarity(ItemRarity.COMMON)
	    add(Items.PAPER).count(5, 8).rarity(ItemRarity.COMMON)
	    add(Items.BONE).count(4).rarity(ItemRarity.COMMON)
	    add(Items.BOOK).count(4).rarity(ItemRarity.COMMON)
	    add(Items.ROTTEN_FLESH).count(2, 7).rarity(ItemRarity.COMMON)
	    add(Items.BREAD).count(3).rarity(ItemRarity.COMMON)
	    add(Items.GUNPOWDER).rarity(ItemRarity.COMMON)
	    add(Items.IRON_INGOT).rarity(ItemRarity.COMMON)
	    add(Items.COPPER_INGOT).count(3).rarity(ItemRarity.COMMON)
	    add(Items.OAK_LOG).count(4).rarity(ItemRarity.COMMON)

	    add(Items.EXPERIENCE_BOTTLE).count(2, 3).rarity(ItemRarity.UNCOMMON)
	    add(Items.BOOK).count(6, 8).rarity(ItemRarity.UNCOMMON)
	    add(Items.GUNPOWDER).count(2, 4).rarity(ItemRarity.UNCOMMON)
	    add(Items.TORCH).count(8, 12).rarity(ItemRarity.UNCOMMON)
	    add(Items.BONE).count(4, 7).rarity(ItemRarity.UNCOMMON)
	    add(Items.SPIDER_EYE).rarity(ItemRarity.UNCOMMON)
	    add(Items.REDSTONE).count(3, 4).rarity(ItemRarity.UNCOMMON)
	    add(Items.EMERALD).count(2, 4).rarity(ItemRarity.UNCOMMON)
	    add(Items.COBWEB).count(8, 12).rarity(ItemRarity.UNCOMMON)
	    add(Items.GLASS_BOTTLE).count(12, 16).rarity(ItemRarity.UNCOMMON)
	    add(Items.RAW_IRON).count(1, 2).rarity(ItemRarity.UNCOMMON)
	    add(Items.RAW_COPPER).count(1, 6).rarity(ItemRarity.UNCOMMON)
	    add(Items.RAW_GOLD).rarity(ItemRarity.UNCOMMON)
	    add(Items.RAW_COPPER_BLOCK).rarity(ItemRarity.UNCOMMON)
	    add(Items.COAL).count(16, 24).rarity(ItemRarity.UNCOMMON)
	    add(Items.LAPIS_LAZULI).count(3, 4).rarity(ItemRarity.UNCOMMON)
	    add(Items.IRON_INGOT).count(1, 2).rarity(ItemRarity.UNCOMMON)
	    add(Items.COPPER_INGOT).count(1, 4).rarity(ItemRarity.UNCOMMON)
	    add(Items.GOLD_INGOT).rarity(ItemRarity.UNCOMMON)
	    add(Items.OAK_LOG).count(4, 8).rarity(ItemRarity.UNCOMMON)

	    add(Items.CHARCOAL).count(32, 36).rarity(ItemRarity.RARE)
	    add(Items.RAW_IRON).count(8, 12).rarity(ItemRarity.RARE)
	    add(Items.RAW_COPPER).count(12, 18).rarity(ItemRarity.RARE)
	    add(Items.RAW_GOLD).count(2, 6).rarity(ItemRarity.RARE)
	    add(Items.RAW_IRON_BLOCK).count(1, 3).rarity(ItemRarity.RARE)
	    add(Items.RAW_GOLD_BLOCK).count(1, 2).rarity(ItemRarity.RARE)
	    add(Items.RAW_COPPER_BLOCK).count(1, 6).rarity(ItemRarity.RARE)
	    add(Items.LAPIS_LAZULI).count(7, 18).rarity(ItemRarity.RARE)
	    //add(Items.LAPIS_LAZULI).rarity(ItemRarity.RARE).name(Component.literal("La Peace").withStyle(ChatFormatting.BLUE)) TODO: add in IndustrialRegistry not here
	    add(Items.IRON_INGOT).count(3, 4).rarity(ItemRarity.RARE)
	    add(Items.GOLD_INGOT).count(1, 2).rarity(ItemRarity.RARE)
	    add(Items.COPPER_INGOT).count(6, 12).rarity(ItemRarity.RARE)
	    add(Items.REDSTONE).count(12, 16).rarity(ItemRarity.RARE)
	    add(Items.COAL_BLOCK).count(3, 4).rarity(ItemRarity.RARE)
	    add(Items.LAPIS_BLOCK).rarity(ItemRarity.RARE)
	    add(Items.REDSTONE_BLOCK).count(1, 2).rarity(ItemRarity.RARE)
	    add(Items.OAK_LOG).count(7).rarity(ItemRarity.RARE)
	    add(Items.DIAMOND).count(1, 2).rarity(ItemRarity.RARE)
	    add(Items.IRON_SHOVEL).rarity(ItemRarity.RARE).name(Component.literal("Copper Shovel")).modify { val enchants = it.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.EFFICIENCY), 4)
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 3)
	    }

	    add(Items.DIAMOND).count(4, 7).rarity(ItemRarity.EPIC)
	    add(Items.DIAMOND_BLOCK).rarity(ItemRarity.EPIC)
	    add(Items.IRON_BLOCK).count(3, 5).rarity(ItemRarity.EPIC)
	    add(Items.GOLD_BLOCK).count(2).rarity(ItemRarity.EPIC)
	    add(Items.COPPER_BLOCK.weathering.unaffected).count(6, 10).rarity(ItemRarity.EPIC)
	    add(Items.COAL_BLOCK).count(8, 19).rarity(ItemRarity.EPIC)
	    add(Items.IRON_INGOT).count(12, 21).rarity(ItemRarity.EPIC)
	    add(Items.GOLD_INGOT).count(8, 11).rarity(ItemRarity.EPIC)
	    add(Items.OAK_LOG).count(2, 12).rarity(ItemRarity.EPIC)
	    add(Items.LAPIS_BLOCK).count(3).rarity(ItemRarity.EPIC)
	    add(Items.REDSTONE_BLOCK).count(5, 8).rarity(ItemRarity.EPIC)
	    add(Items.COAL).count(1, 2).rarity(ItemRarity.EPIC).name(Component.literal("Refined Carbon").withStyle(ChatFormatting.DARK_GRAY))
	    add(Items.IRON_CHESTPLATE).rarity(ItemRarity.EPIC).modify { val enchants = it.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.PROTECTION), 3)
		    enchant(enchants.getOrThrow(Enchantments.MENDING), 1)
	    }
	    /*add(Items.AMETHYST_SHARD).rarity(ItemRarity.EPIC).name(Component.literal("Architect's Focus").withStyle(ChatFormatting.LIGHT_PURPLE)).modify { level ->
		    set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
			    .add(Attributes.BLOCK_INTERACTION_RANGE, AttributeModifier(ResourceLocation.fromNamespaceAndPath("smpmod", "builder_reach"), 1.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
			    .build())*/ //TODO: fix

	    add(Items.DIAMOND_BLOCK).count(1, 3).rarity(ItemRarity.LEGENDARY)
	    add(Items.DIAMOND).count(6, 11).rarity(ItemRarity.LEGENDARY)
	    add(Items.IRON_BLOCK).count(6, 11).rarity(ItemRarity.LEGENDARY)
	    add(Items.GOLD_BLOCK).count(3, 6).rarity(ItemRarity.LEGENDARY)
	    add(Items.COAL_BLOCK).count(16, 36).rarity(ItemRarity.LEGENDARY)
	    add(Items.LAPIS_BLOCK).count(6, 10).rarity(ItemRarity.LEGENDARY)
	    add(Items.REDSTONE_BLOCK).count(12, 16).rarity(ItemRarity.LEGENDARY)
	    // TODO: eme rod FISHING
	    //add(Items.COAL).count(2, 5).rarity(ItemRarity.LEGENDARY).name(Component.literal("Compressed Carbon").withStyle(ChatFormatting.GRAY)) TODO: hooks
	    add(Items.DIAMOND_PICKAXE).rarity(ItemRarity.LEGENDARY).modify { val enchants = it.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.EFFICIENCY), 6)
		    enchant(enchants.getOrThrow(Enchantments.FORTUNE), 3)
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 4)
	    }
	    add(Items.GOLDEN_SHOVEL).rarity(ItemRarity.LEGENDARY).modify { val enchants = it.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.EFFICIENCY), 8)
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 6)
	    }
	    add(Items.DIAMOND_LEGGINGS).rarity(ItemRarity.LEGENDARY).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.PROTECTION), 1)
		    enchant(enchants.getOrThrow(Enchantments.SWIFT_SNEAK), 3)
	    }
	    add(Items.LEATHER_BOOTS).rarity(ItemRarity.LEGENDARY).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.PROTECTION), 6)
		    enchant(enchants.getOrThrow(Enchantments.DEPTH_STRIDER), 3)
		    enchant(enchants.getOrThrow(Enchantments.SOUL_SPEED), 4)
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 4)
		    enchant(enchants.getOrThrow(Enchantments.FEATHER_FALLING), 6)
	    }
	    add(Items.IRON_HELMET).rarity(ItemRarity.LEGENDARY).name(Component.literal("Rose & Spikes").withStyle(ChatFormatting.RED)).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.THORNS), 8)
	    }
	    add(Items.IRON_INGOT).count(1, 2).rarity(ItemRarity.LEGENDARY).name(Component.literal("Steel Ingot").withStyle(ChatFormatting.DARK_GRAY))
	    add(Items.BOW).rarity(ItemRarity.LEGENDARY).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.PUNCH), 4)
	    }

	    add(Items.DIAMOND_BLOCK).count(4, 6).rarity(ItemRarity.MYTHIC)
	    add(Items.IRON_BLOCK).count(13, 24).rarity(ItemRarity.MYTHIC)
	    add(Items.GOLD_BLOCK).count(7, 12).rarity(ItemRarity.MYTHIC)
	    add(Items.COAL_BLOCK).count(8, 14).rarity(ItemRarity.MYTHIC)
	    add(Items.TOTEM_OF_UNDYING).rarity(ItemRarity.MYTHIC)
	    add(Items.NETHERITE_SCRAP).count(1, 3).rarity(ItemRarity.MYTHIC)
	    add(Items.NETHERITE_INGOT).rarity(ItemRarity.MYTHIC)
	    add(Items.DIAMOND_PICKAXE).rarity(ItemRarity.MYTHIC).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.EFFICIENCY), level.random.nextIntBetweenInclusive(2, 3))
		    enchant(enchants.getOrThrow(Enchantments.FORTUNE), level.random.nextIntBetweenInclusive(5, 6))
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 1)
	    }
	    add(Items.GOLDEN_PICKAXE).rarity(ItemRarity.MYTHIC).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.EFFICIENCY), 12)
		    enchant(enchants.getOrThrow(Enchantments.SILK_TOUCH), 1)
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), level.random.nextIntBetweenInclusive(8, 10))
	    }
	    add(Items.CHAINMAIL_CHESTPLATE).rarity(ItemRarity.MYTHIC).name(Component.literal("Copper Plate").withStyle(ChatFormatting.GOLD)).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.PROJECTILE_PROTECTION), 9)
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 3)
	    }
	    add(Items.BOW).rarity(ItemRarity.MYTHIC).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.POWER), 6)
		    enchant(enchants.getOrThrow(Enchantments.FLAME), 1)
	    }
	    add(Items.DIAMOND_SWORD).rarity(ItemRarity.MYTHIC).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.SMITE), 7)
		    enchant(enchants.getOrThrow(Enchantments.IMPALING), 9)
	    }
	    add(Items.DIAMOND_SWORD).rarity(ItemRarity.MYTHIC).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.BANE_OF_ARTHROPODS), 8)
		    enchant(enchants.getOrThrow(Enchantments.SWEEPING_EDGE), 5)
	    }
	    add(Items.DIAMOND_AXE).rarity(ItemRarity.MYTHIC).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.EFFICIENCY), 8)
		    enchant(enchants.getOrThrow(Enchantments.SILK_TOUCH), 1)
	    }
	    // TODO: dia rod FISHING

	    add(Items.DIAMOND_BLOCK).count(7, 8).rarity(ItemRarity.CHROMATIC)
	    add(Items.IRON_BLOCK).count(32, 45).rarity(ItemRarity.CHROMATIC)
	    add(Items.GOLD_BLOCK).count(16, 22).rarity(ItemRarity.CHROMATIC)
	    add(Items.TOTEM_OF_UNDYING).count(1, 3).rarity(ItemRarity.CHROMATIC)
	    add(Items.NETHERITE_SCRAP).count(4, 12).rarity(ItemRarity.CHROMATIC)
	    add(Items.ENCHANTED_GOLDEN_APPLE).count(1, 2).rarity(ItemRarity.CHROMATIC)
	    add(Items.SHULKER_BOX).rarity(ItemRarity.CHROMATIC)
	    add(Items.NETHERITE_INGOT).count(1, 5).rarity(ItemRarity.CHROMATIC)
	    add(Items.NETHER_STAR).rarity(ItemRarity.CHROMATIC)
	    add(Items.NETHERITE_PICKAXE).rarity(ItemRarity.CHROMATIC).name(Component.literal("Prismatic All-Rounder").withStyle(ChatFormatting.RED)).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.EFFICIENCY), 6)
		    enchant(enchants.getOrThrow(Enchantments.FORTUNE), 4)
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 4)
		    enchant(enchants.getOrThrow(Enchantments.MENDING), 1)
	    }
	    add(Items.NETHERITE_AXE).rarity(ItemRarity.CHROMATIC).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.SHARPNESS), 6)
		    enchant(enchants.getOrThrow(Enchantments.FIRE_ASPECT), 2)
		    enchant(enchants.getOrThrow(Enchantments.VANISHING_CURSE), 1)
	    }
	    add(Items.DIAMOND_SWORD).rarity(ItemRarity.CHROMATIC).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.SHARPNESS), 6)
		    enchant(enchants.getOrThrow(Enchantments.FIRE_ASPECT), 2)
		    enchant(enchants.getOrThrow(Enchantments.SWEEPING_EDGE), 4)
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 6)
	    }
	    add(Items.NETHERITE_HELMET).rarity(ItemRarity.CHROMATIC).name(Component.literal("Crown of Thorns II").withStyle(ChatFormatting.RED)).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.THORNS), 8)
		    enchant(enchants.getOrThrow(Enchantments.PROTECTION), 4)
	    }
	    add(Items.DIAMOND_CHESTPLATE).rarity(ItemRarity.CHROMATIC).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.PROTECTION), 4)
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 10)
	    }
	    add(Items.NETHERITE_LEGGINGS).rarity(ItemRarity.CHROMATIC).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.FIRE_PROTECTION), 7)
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 3)
	    }
	    add(Items.HEAVY_CORE).rarity(ItemRarity.CHROMATIC)
	    // TODO: any t6 rod
	    add(Items.TRIDENT).rarity(ItemRarity.CHROMATIC).name(Component.literal("Copper Spear").withStyle(ChatFormatting.GOLD)).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.RIPTIDE), 3)
		    enchant(enchants.getOrThrow(Enchantments.IMPALING), 6)
		    enchant(enchants.getOrThrow(Enchantments.VANISHING_CURSE), 1)
	    }

	    add(Items.WIND_CHARGE).count(1, 3).rarity(ItemRarity.ASTRAL) // TODO: add to epic+
	    // TODO: add wind charge BOOK lvl 3
	    add(Items.LEATHER_BOOTS).rarity(ItemRarity.ASTRAL).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.FROST_WALKER), level.random.nextIntBetweenInclusive(10, 24))
		    enchant(enchants.getOrThrow(Enchantments.VANISHING_CURSE), 1)
	    }
	    add(Items.HEAVY_CORE).count(1, 2).rarity(ItemRarity.ASTRAL)
	    add(Items.NETHER_STAR).count(1, 3).rarity(ItemRarity.ASTRAL)
	    add(Items.SHULKER_BOX).count(1, 2).rarity(ItemRarity.ASTRAL)
	    add(Items.NETHERITE_BLOCK).rarity(ItemRarity.ASTRAL)
	    add(Items.NETHERITE_INGOT).count(3, 7).rarity(ItemRarity.ASTRAL)
	    add(Items.ENCHANTED_GOLDEN_APPLE).count(1, 3).rarity(ItemRarity.ASTRAL)
	    add(Items.NETHERITE_PICKAXE).rarity(ItemRarity.ASTRAL).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.EFFICIENCY), 12)
	    }
	    add(Items.IRON_PICKAXE).rarity(ItemRarity.ASTRAL).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.FORTUNE), 8)
		    enchant(enchants.getOrThrow(Enchantments.VANISHING_CURSE), 1)
	    }
	    add(Items.ELYTRA).rarity(ItemRarity.ASTRAL).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.PROTECTION), 4)
		    enchant(enchants.getOrThrow(Enchantments.MENDING), 1)
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 3)
	    }
	    add(Items.NETHERITE_BOOTS).rarity(ItemRarity.ASTRAL).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.PROTECTION), 2)
		    enchant(enchants.getOrThrow(Enchantments.FEATHER_FALLING), 10)
	    }
	    add(Items.WOODEN_SWORD).rarity(ItemRarity.ASTRAL).name(Component.literal("Splinter of Eternity").withStyle(ChatFormatting.AQUA)).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.SHARPNESS), 12)
		    enchant(enchants.getOrThrow(Enchantments.SMITE), 8)
		    enchant(enchants.getOrThrow(Enchantments.BANE_OF_ARTHROPODS), 8)
		    enchant(enchants.getOrThrow(Enchantments.VANISHING_CURSE), 1)
	    }
	    add(Items.MACE).rarity(ItemRarity.ASTRAL).name(Component.literal("Netherite Spear").withStyle(ChatFormatting.AQUA)).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.DENSITY), 6)
		    enchant(enchants.getOrThrow(Enchantments.MENDING), 1)
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 4)
	    }
	    add(Items.NETHERITE_HOE).rarity(ItemRarity.ASTRAL).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.EFFICIENCY), 8)
		    enchant(enchants.getOrThrow(Enchantments.FORTUNE), 6)
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 4)
		    enchant(enchants.getOrThrow(Enchantments.MENDING), 1)
	    }
	    add(Items.GOLDEN_SHOVEL).rarity(ItemRarity.ASTRAL).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.EFFICIENCY), 8)
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 14)
		    enchant(enchants.getOrThrow(Enchantments.MENDING), 1)
	    }
	    /*add(Items.CHAINMAIL_BOOTS).rarity(ItemRarity.ASTRAL).name(Component.literal("Mountaineer's Stride").withStyle(ChatFormatting.AQUA)).modify { level ->
		    set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
			    .add(Attributes.STEP_HEIGHT, AttributeModifier(ResourceLocation.fromNamespaceAndPath("smpmod", "step_height_boost"), 0.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.FEET)
			    .build())
	    }*/ // TODO: fix
    }
    private fun registerOverworld() { // sort by rarity, then biome, group overlaps tgt TODO
        add(Items.HANGING_ROOTS).count(8).rarity(ItemRarity.COMMON).biome(Biomes.OVERWORLD)
        add(Items.RAW_COPPER).count(4).rarity(ItemRarity.COMMON).biome(Biomes.OVERWORLD)
        add(Items.RAW_IRON).count(2).rarity(ItemRarity.COMMON).biome(Biomes.OVERWORLD)
        add(Items.COAL).count(5).rarity(ItemRarity.COMMON).biome(Biomes.OVERWORLD)
        add(Items.GRAVEL).count(16).rarity(ItemRarity.COMMON).biome(Biomes.OVERWORLD)
        add(Items.FLINT).count(4).rarity(ItemRarity.COMMON).biome(Biomes.OVERWORLD)
        add(Items.OAK_PLANKS).count(16).rarity(ItemRarity.COMMON).biome(Biomes.OVERWORLD)

	    add(Items.COPPER_INGOT).count(3).rarity(ItemRarity.UNCOMMON).biome(Biomes.OVERWORLD)
	    add(Items.IRON_INGOT).count(2).rarity(ItemRarity.UNCOMMON).biome(Biomes.OVERWORLD)
	    add(Items.REDSTONE).count(8).rarity(ItemRarity.UNCOMMON).biome(Biomes.OVERWORLD)
	    add(Items.LAPIS_LAZULI).count(8).rarity(ItemRarity.UNCOMMON).biome(Biomes.OVERWORLD)
	    add(Items.CONCRETE.white).count(8).rarity(ItemRarity.UNCOMMON).biome(Biomes.OVERWORLD)

	    // biome

	    add(Items.GLOW_BERRIES).count(4).rarity(ItemRarity.COMMON).biome(Biomes.CAVES)
	    add(Items.COBBLESTONE).count(32).rarity(ItemRarity.COMMON).biome(Biomes.CAVES)
	    add(Items.COBBLED_DEEPSLATE).count(14).rarity(ItemRarity.COMMON).biome(Biomes.CAVES)
	    add(Items.ANDESITE).count(16).rarity(ItemRarity.COMMON).biome(Biomes.CAVES)
	    add(Items.DIORITE).count(16).rarity(ItemRarity.COMMON).biome(Biomes.CAVES)
	    add(Items.GRANITE).count(16).rarity(ItemRarity.COMMON).biome(Biomes.CAVES)
	    add(Items.GRAVEL).count(64).rarity(ItemRarity.COMMON).biome(Biomes.CAVES)

        add(Items.POINTED_DRIPSTONE).count(6).rarity(ItemRarity.COMMON).biome(Biomes.DRIP)
	    add(Items.DRIPSTONE_BLOCK).count(16).rarity(ItemRarity.COMMON).biome(Biomes.DRIP)

        add(Items.MOSS_BLOCK).count(4).rarity(ItemRarity.COMMON).biome(Biomes.LUSH)

        add(Items.MANGROVE_ROOTS).count(8).rarity(ItemRarity.COMMON).biome(Biomes.SWAMP)
        add(Items.CLAY_BALL).count(8).rarity(ItemRarity.COMMON).biome(Biomes.SWAMP).biome(Biomes.OCEAN)
	    add(Items.SEAGRASS).count(8).rarity(ItemRarity.COMMON).biome(Biomes.OCEAN)
	    add(Items.WATER_BUCKET).count(1).rarity(ItemRarity.COMMON).biome(Biomes.OCEAN).biome(Biomes.DRIP)

	    add(Items.RED_SAND).count(8).rarity(ItemRarity.COMMON).biome(Biomes.BADLANDS)
        add(Items.DEAD_BUSH).count(6).rarity(ItemRarity.COMMON).biome(Biomes.BADLANDS).biome(Biomes.DESERT)
        add(Items.CACTUS).count(4).rarity(ItemRarity.COMMON).biome(Biomes.DESERT)
	    add(Items.GLASS).count(4).rarity(ItemRarity.COMMON).biome(Biomes.DESERT)
	    add(Items.LAVA_BUCKET).count(1).rarity(ItemRarity.COMMON).biome(Biomes.DESERT)
	    add(Items.SAND).count(64).rarity(ItemRarity.COMMON).biome(Biomes.DESERT)

        add(Items.SNOWBALL).count(16).rarity(ItemRarity.COMMON).biome(Biomes.ICE)
	    add(Items.SPRUCE_SAPLING).rarity(ItemRarity.COMMON).biome(Biomes.ICE).biome(Biomes.TAIGA)
        add(Items.SWEET_BERRIES).count(6).rarity(ItemRarity.COMMON).biome(Biomes.TAIGA)

        add(Items.BAMBOO).count(8).rarity(ItemRarity.COMMON).biome(Biomes.JUNGLE)

	    add(Items.WOOL.gray).count(8).rarity(ItemRarity.COMMON).biome(Biomes.SCULK)
	    add(Items.SCULK_VEIN).count(5).rarity(ItemRarity.COMMON).biome(Biomes.SCULK)

	    // uncommon

	    add(Items.RED_MUSHROOM_BLOCK).count(3).rarity(ItemRarity.UNCOMMON).biome(Biomes.DARK_FOREST)
	    add(Items.BROWN_MUSHROOM_BLOCK).count(2).rarity(ItemRarity.UNCOMMON).biome(Biomes.DARK_FOREST)
	    add(Items.DARK_OAK_LEAVES).count(12).rarity(ItemRarity.UNCOMMON).biome(Biomes.DARK_FOREST)
	    add(Items.DARK_OAK_SAPLING).count(6).rarity(ItemRarity.UNCOMMON).biome(Biomes.DARK_FOREST)

        add(Items.TERRACOTTA).count(8).rarity(ItemRarity.UNCOMMON).biome(Biomes.BADLANDS)

        add(Items.PACKED_ICE).count(6).rarity(ItemRarity.UNCOMMON).biome(Biomes.ICE)

        add(Items.PRISMARINE_SHARD).count(4).rarity(ItemRarity.UNCOMMON).biome(Biomes.OCEAN)

        add(Items.HONEYCOMB).count(3).rarity(ItemRarity.UNCOMMON).biome(Biomes.FLOWER)

        add(Items.COCOA_BEANS).count(8).rarity(ItemRarity.UNCOMMON).biome(Biomes.JUNGLE)

	    add(Items.AMETHYST_SHARD).count(4).rarity(ItemRarity.UNCOMMON).biome(Biomes.CAVES)
	    add(Items.WOOL.gray).count(32).rarity(ItemRarity.UNCOMMON).biome(Biomes.SCULK)
	    add(Items.SCULK).count(16).rarity(ItemRarity.UNCOMMON).biome(Biomes.SCULK)
	    add(Items.CANDLE).count(5).rarity(ItemRarity.UNCOMMON).biome(Biomes.SCULK)
    }
    private fun registerNether() { // TODO
        add(Items.GLOWSTONE_DUST).count(12).rarity(ItemRarity.COMMON).biome(Biomes.NETHER_LIST)
        add(Items.NETHERRACK).count(32).rarity(ItemRarity.COMMON).biome(Biomes.NETHER_LIST)
        add(Items.NETHER_BRICK).count(8).rarity(ItemRarity.COMMON).biome(Biomes.NETHER_LIST)

        add(Items.SOUL_SAND).count(12).rarity(ItemRarity.COMMON).biome(Biomes.SOUL)
        add(Items.SOUL_SOIL).count(12).rarity(ItemRarity.COMMON).biome(Biomes.SOUL)
        add(Items.BASALT).count(16).rarity(ItemRarity.COMMON).biome(Biomes.BASALT)
        add(Items.BLACKSTONE).count(16).rarity(ItemRarity.COMMON).biome(Biomes.BASALT)
        add(Items.MAGMA_CREAM).count(2).rarity(ItemRarity.COMMON).biome(Biomes.BASALT)
        add(Items.CRIMSON_ROOTS).count(4).rarity(ItemRarity.COMMON).biome(Biomes.CRIMSON)
        add(Items.WARPED_ROOTS).count(4).rarity(ItemRarity.COMMON).biome(Biomes.WARPED)
        add(Items.NETHER_SPROUTS).count(6).rarity(ItemRarity.COMMON).biome(Biomes.WARPED)

        add(Items.QUARTZ).count(8).rarity(ItemRarity.UNCOMMON).biome(Biomes.NETHER_LIST)
        add(Items.GOLD_INGOT).count(2).rarity(ItemRarity.UNCOMMON).biome(Biomes.NETHER_LIST)
        add(Items.GLOWSTONE).count(2).rarity(ItemRarity.UNCOMMON).biome(Biomes.NETHER_LIST)

        add(Items.GHAST_TEAR).count(1).rarity(ItemRarity.UNCOMMON).biome(Biomes.SOUL)
        add(Items.BLAZE_POWDER).count(2).rarity(ItemRarity.UNCOMMON).biome(Biomes.NETHER_LIST)
        add(Items.CRIMSON_STEM).count(8).rarity(ItemRarity.UNCOMMON).biome(Biomes.CRIMSON)
        add(Items.WARPED_STEM).count(8).rarity(ItemRarity.UNCOMMON).biome(Biomes.WARPED)
        add(Items.CRIMSON_FUNGUS).count(3).rarity(ItemRarity.UNCOMMON).biome(Biomes.CRIMSON)
        add(Items.WARPED_FUNGUS).count(3).rarity(ItemRarity.UNCOMMON).biome(Biomes.WARPED)
    }
    private fun registerEnd() {
	    add(Items.END_STONE).count(32, 64).rarity(ItemRarity.COMMON).biome(Biomes.END_LIST)
	    add(Items.END_ROD).count(2).rarity(ItemRarity.COMMON).biome(Biomes.END_LIST)
	    add(Items.PHANTOM_MEMBRANE).count(2, 4).rarity(ItemRarity.COMMON).biome(Biomes.END_LIST)
	    add(Items.STAINED_GLASS.black).count(16, 32).rarity(ItemRarity.COMMON).biome(Biomes.END_LIST)
	    add(Items.CHORUS_FRUIT).count(2).rarity(ItemRarity.COMMON).biome(Biomes.END_LIST)
	    add(Items.POPPED_CHORUS_FRUIT).count(4).rarity(ItemRarity.COMMON).biome(Biomes.END_LIST)
	    add(Items.ENDER_PEARL).rarity(ItemRarity.UNCOMMON).biome(Biomes.END_LIST)

	    add(Items.CHORUS_FRUIT).count(8, 16).rarity(ItemRarity.UNCOMMON).biome(Biomes.END_LIST)
	    add(Items.POPPED_CHORUS_FRUIT).count(6, 12).rarity(ItemRarity.UNCOMMON).biome(Biomes.END_LIST)
	    add(Items.ENDER_PEARL).count(4, 8).rarity(ItemRarity.UNCOMMON).biome(Biomes.END_LIST)
	    add(Items.CONCRETE.purple).count(16, 32).rarity(ItemRarity.UNCOMMON).biome(Biomes.END_LIST)
	    add(Items.FIREWORK_ROCKET).count(8, 12).rarity(ItemRarity.UNCOMMON).biome(Biomes.END_LIST).modify { set(DataComponents.FIREWORKS, Fireworks(1, emptyList())) } // duration 1

	    add(Items.END_ROD).count(8, 12).rarity(ItemRarity.RARE).biome(Biomes.END_LIST)
	    add(Items.PURPUR_BLOCK).count(16, 32).rarity(ItemRarity.RARE).biome(Biomes.END_LIST)
	    add(Items.DRAGON_BREATH).count(2, 4).rarity(ItemRarity.RARE).biome(Biomes.END_LIST)
	    add(Items.ENDERMITE_SPAWN_EGG).rarity(ItemRarity.RARE).biome(Biomes.END_LIST)
	    add(Items.FIREWORK_ROCKET).count(8, 14).rarity(ItemRarity.RARE).biome(Biomes.END_LIST).modify { set(DataComponents.FIREWORKS, Fireworks(2, emptyList())) }
	    add(Items.DIAMOND_PICKAXE).rarity(ItemRarity.RARE).biome(Biomes.END_LIST).modify { level ->
		    val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), level.random.nextIntBetweenInclusive(1, 2))
		    enchant(enchants.getOrThrow(Enchantments.EFFICIENCY), level.random.nextIntBetweenInclusive(2, 3))
	    }

	    add(Items.SHULKER_SHELL).count(2, 4).rarity(ItemRarity.EPIC).biome(Biomes.END_LIST)
	    add(Items.ENDER_CHEST).count(2).rarity(ItemRarity.EPIC).biome(Biomes.END_LIST)
	    add(Items.CRYING_OBSIDIAN).count(12, 24).rarity(ItemRarity.EPIC).biome(Biomes.END_LIST)
	    add(Items.GLAZED_TERRACOTTA.purple).count(16, 32).rarity(ItemRarity.EPIC).biome(Biomes.END_LIST)
	    add(Items.POTION).rarity(ItemRarity.EPIC).biome(Biomes.END_LIST).name(Component.literal("Special Liquid")).modify { set(DataComponents.POTION_CONTENTS, PotionContents(Optional.empty<Holder<Potion>>(), Optional.of(0xFFFFFF), listOf(MobEffectInstance(MobEffects.NAUSEA, 200, 15)), Optional.empty<String>())) }

	    add(Items.POTION).rarity(ItemRarity.LEGENDARY).biome(Biomes.END_LIST).name(Component.literal("Dragon's Breath")).modify { set(DataComponents.POTION_CONTENTS, PotionContents(Optional.empty<Holder<Potion>>(), Optional.of(0x9B59B6), listOf(MobEffectInstance(MobEffects.REGENERATION, 400, 1), MobEffectInstance(MobEffects.RESISTANCE, 600, 0), MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0)), Optional.empty<String>())) }
		add(Items.SHULKER_SHELL).count(6, 10).rarity(ItemRarity.LEGENDARY).biome(Biomes.END_LIST)
	    add(Items.END_CRYSTAL).count(2).rarity(ItemRarity.LEGENDARY).biome(Biomes.END_LIST)
	    add(Items.OBSIDIAN).count(32, 64).rarity(ItemRarity.LEGENDARY).biome(Biomes.END_LIST)
	    add(Items.FIREWORK_ROCKET).count(16, 24).rarity(ItemRarity.RARE).biome(Biomes.END_LIST).modify { set(DataComponents.FIREWORKS, Fireworks(2, emptyList())) }
	    add(Items.DIAMOND_CHESTPLATE).rarity(ItemRarity.LEGENDARY).biome(Biomes.END_LIST).name(Component.literal("Voidbound Cuirass").withStyle(ChatFormatting.GOLD)).modify { level ->
			val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.PROTECTION), level.random.nextIntBetweenInclusive(3, 4))
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 3)
		    enchant(enchants.getOrThrow(Enchantments.THORNS), level.random.nextIntBetweenInclusive(5, 7))
		}

	    add(Items.END_CRYSTAL).count(3, 4).rarity(ItemRarity.MYTHIC).biome(Biomes.END_LIST)
	    add(Items.NETHER_STAR).count(1).rarity(ItemRarity.MYTHIC).biome(Biomes.END_LIST)
	    add(Items.DRAGON_HEAD).count(1).rarity(ItemRarity.MYTHIC).biome(Biomes.END_LIST)
	    add(Items.FIREWORK_ROCKET).count(24, 28).rarity(ItemRarity.RARE).biome(Biomes.END_LIST).modify { set(DataComponents.FIREWORKS, Fireworks(3, emptyList())) }
	    add(Items.NETHERITE_PICKAXE).rarity(ItemRarity.MYTHIC).biome(Biomes.END_LIST).name(Component.literal("Void Excavator").withStyle(ChatFormatting.LIGHT_PURPLE)).modify { level ->
			val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.EFFICIENCY), level.random.nextIntBetweenInclusive(7, 10))
		    enchant(enchants.getOrThrow(Enchantments.SILK_TOUCH), 1)
		}

	    add(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE).count(2, 8).rarity(ItemRarity.CHROMATIC).biome(Biomes.END_LIST)
	    add(Items.NETHERITE_INGOT).count(18).rarity(ItemRarity.CHROMATIC).biome(Biomes.END_LIST)
	    add(Items.FIREWORK_ROCKET).count(16, 18).rarity(ItemRarity.RARE).biome(Biomes.END_LIST).modify { set(DataComponents.FIREWORKS, Fireworks(4, emptyList())) }
	    add(Items.NETHERITE_SWORD).rarity(ItemRarity.CHROMATIC).biome(Biomes.END_LIST).name(Component.literal("Void's Edge").withStyle(ChatFormatting.RED)).modify { level ->
			val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.SHARPNESS), 2)
		    enchant(enchants.getOrThrow(Enchantments.LOOTING), level.random.nextIntBetweenInclusive(7, 9))
		    enchant(enchants.getOrThrow(Enchantments.UNBREAKING), 2)
		    enchant(enchants.getOrThrow(Enchantments.SWEEPING_EDGE), 4)
		}

	    add(Items.BEACON).count(1).rarity(ItemRarity.ASTRAL).biome(Biomes.END_LIST)
	    add(Items.FIREWORK_ROCKET).count(36, 40).rarity(ItemRarity.RARE).biome(Biomes.END_LIST).modify { set(DataComponents.FIREWORKS, Fireworks(10, emptyList())) }
	    add(Items.NETHERITE_CHESTPLATE).rarity(ItemRarity.ASTRAL).biome(Biomes.END_LIST).name(Component.literal("Astral Deity's Aegis").withStyle(ChatFormatting.AQUA)).modify { level ->
			val enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
		    enchant(enchants.getOrThrow(Enchantments.PROTECTION), 10)
		}
	}
}
