package spmc.smpmod.industrial

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.Level
import spmc.smpmod.registry.IndustrialRegistry

@JvmRecord
data class CompressorRecipe(val ingredient: Ingredient, val count: Int, val result: ItemStackTemplate, val processTime: Int): Recipe<SingleRecipeInput> {
	override fun assemble(input: SingleRecipeInput) = this.result.create()
	override fun showNotification() = false
	override fun group() = ""
	override fun getSerializer() = IndustrialRegistry.COMPRESSOR_SERIALIZER!!
	override fun getType() = IndustrialRegistry.COMPRESSOR_TYPE!!
	override fun placementInfo() = PlacementInfo.create(this.ingredient)
	override fun recipeBookCategory() = RecipeBookCategory()
	override fun matches(input: SingleRecipeInput, level: Level) = this.ingredient.test(input.item()) && input.item().count >= this.count
}

@JvmRecord
data class SmelterRecipe(val ingredients: MutableList<Ingredient>, val count: Int, val result: ItemStackTemplate, val processTime: Int): Recipe<TripleRecipeInput> {
	override fun assemble(input: TripleRecipeInput) = this.result.create()
	override fun showNotification() = false
	override fun group() = ""
	override fun getSerializer() = IndustrialRegistry.SMELTERY_SERIALIZER!!
	override fun getType() = IndustrialRegistry.SMELTERY_TYPE!!
	override fun placementInfo() = PlacementInfo.NOT_PLACEABLE
	override fun recipeBookCategory() = RecipeBookCategory()
	override fun matches(input: TripleRecipeInput, level: Level): Boolean {
		val inputs = mutableListOf<ItemStack>()
		for (i in 0 ..< input.size()) {
			val stack = input.getItem(i)
			if (!stack.isEmpty) {
				if (stack.count < this.count) return false
				inputs.add(stack)
			}
		}

		if (inputs.size != this.ingredients.size) return false
		val matched = BooleanArray(inputs.size)
		for (ingredient in this.ingredients) {
			var foundMatch = false
			for (i in inputs.indices) {
				if (!matched[i] && ingredient.test(inputs[i])) {
					matched[i] = true
					foundMatch = true
					break
				}
			}
			if (!foundMatch) return false
		}

		return true
	}
}

data class TripleRecipeInput(val item1: ItemStack, val item2: ItemStack, val item3: ItemStack): RecipeInput {
	override fun getItem(index: Int): ItemStack {
		return when (index) {
			0 -> item1
			1 -> item2
			2 -> item3
			else -> throw IllegalArgumentException("No item for index $index")
		}
	}

	override fun size() = 3
}