package spmc.smpmod.registry

import com.mojang.serialization.MapCodec
import eu.pb4.polymer.core.api.block.PolymerBlockUtils
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils
import spmc.smpmod.registry.FishingRegistry.registerFishes
import spmc.smpmod.registry.FishingRegistry.registerRods
import spmc.smpmod.utils.BaseImplementedItem
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.references.BlockItemId
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import java.util.function.Function

@Suppress("unused")
class PolymerRegistry {

    companion object {
        fun init() {
            registerFishes()
            registerRods()
            PlantRegistry.register()
            IndustrialRegistry.registerMinerals()
            IndustrialRegistry.registerBlocks()
            BossRegistry.register()
            IndustrialRegistry.registerRecipes()

            QuestRegistry.init()
            NPCRegistry.init()
            TreasureRegistry.register()
        }
        
        fun <T : Item> createItem(id: String, factory: Function<Item.Properties, T>): T {
            val key: ResourceKey<Item> = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smpmod", id))
            val properties = Item.Properties().setId(key)
            val item = factory.apply(properties)
            return Registry.register(BuiltInRegistries.ITEM, key, item)
        }

        fun createBlockWithItem(id: String, blockFactory: Function<BlockBehaviour.Properties, Block>, properties: BlockBehaviour.Properties, item: Item): Block {
            val identifier = Identifier.fromNamespaceAndPath("smpmod", id)
            val blockId = BlockItemId.create(identifier, identifier)
            val block = blockFactory.apply(properties.setId(blockId.block()))
            Registry.register(BuiltInRegistries.ITEM, blockId.item(), BaseImplementedItem(block, Item.Properties().useBlockDescriptionPrefix().setId(blockId.item()), item, id))
            return Registry.register(BuiltInRegistries.BLOCK, identifier, block)
        }

        fun createBlockOnly(id: String, blockFactory: Function<BlockBehaviour.Properties, Block>, properties: BlockBehaviour.Properties): Block {
            val identifier = Identifier.fromNamespaceAndPath("smpmod", id)
            val block = blockFactory.apply(properties.setId(ResourceKey.create(Registries.BLOCK, identifier)))
            return Registry.register(BuiltInRegistries.BLOCK, identifier, block)
        }

        fun <T : BlockEntity> createBlockWithItemEntity(id: String, blockFactory: Function<BlockBehaviour.Properties, Block>, properties: BlockBehaviour.Properties, entityFactory: FabricBlockEntityTypeBuilder.Factory<out T>, item: Item): BlockEntityType<T> {
            val identifier = Identifier.fromNamespaceAndPath("smpmod", id)
            val blockId = BlockItemId.create(identifier, identifier)
            val block = blockFactory.apply(properties.setId(ResourceKey.create(Registries.BLOCK, identifier)))
            Registry.register(BuiltInRegistries.BLOCK, identifier, block)
            Registry.register(BuiltInRegistries.ITEM, blockId.item(), BaseImplementedItem(block, Item.Properties().useBlockDescriptionPrefix().setId(blockId.item()), item, id))
            val type: BlockEntityType<T> = Registry.register<BlockEntityType<*>, BlockEntityType<T>>(BuiltInRegistries.BLOCK_ENTITY_TYPE, identifier, FabricBlockEntityTypeBuilder.create(entityFactory, block).build())
            PolymerBlockUtils.registerBlockEntity(type)
            return type
        }

        @JvmStatic
        fun <T : Recipe<*>> registerRecipeType(id: String): RecipeType<T> {
            val identifier = Identifier.fromNamespaceAndPath("smpmod", id)
            return Registry.register(BuiltInRegistries.RECIPE_TYPE, identifier, object : RecipeType<T> { override fun toString(): String { return identifier.toString() } })
        }

        // recipe serializer
        fun <T : Recipe<*>> registerRecipeSerializer(id: String, codec: MapCodec<T>, streamCodec: StreamCodec<RegistryFriendlyByteBuf, T>): RecipeSerializer<T> { return Registry.register<RecipeSerializer<*>, RecipeSerializer<T>>(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath("smpmod", id), RecipeSerializer(codec, streamCodec)) }

        fun <T: LivingEntity> registerEntity(id: String, builder: EntityType.Builder<T>, supplier: AttributeSupplier.Builder) {
            val key: ResourceKey<EntityType<*>> = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("smpmod", id))
            val type: EntityType<T> = Registry.register<EntityType<*>, EntityType<T>>(BuiltInRegistries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("smpmod", id), builder.build(key))
            PolymerEntityUtils.registerType(type)
            FabricDefaultAttributeRegistry.register(type, supplier)
        }
    }
}
