package fun.spmc.smpmod.registry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;
import java.util.function.Function;

public class PolymerRegistry {
    protected static <T extends Item> T createItem(String id, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smpmod", id));
        Item.Properties properties = new Item.Properties().setId(key);
        T item = factory.apply(properties);
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    protected static Block createBlockWithItem(String id, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties properties) {
        Identifier identifier = Identifier.fromNamespaceAndPath("smpmod", id);
        BlockItemId blockId = BlockItemId.create(identifier, identifier);
        Block block = blockFactory.apply(properties.setId(blockId.block()));
        Registry.register(BuiltInRegistries.ITEM, blockId.item(), new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(blockId.item())));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    protected static Block createBlockOnly(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties properties) {
        Identifier id = Identifier.fromNamespaceAndPath("smpmod", name);
        Block block = blockFactory.apply(properties.setId(ResourceKey.create(Registries.BLOCK, id)));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    protected static <T extends Recipe<?>> RecipeType<T> registerRecipeType(String id) {
        Identifier identifier = Identifier.fromNamespaceAndPath("smpmod", id);
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, identifier, new RecipeType<T>() {@Override public String toString() { return identifier.toString(); }});
    }

    // 1 input recipe serializer
    protected static <T extends Recipe<?>> RecipeSerializer<T> registerSingleInputSerializer(String id, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath("smpmod", id), new RecipeSerializer<>(codec, streamCodec));
    }

    protected static <T extends LivingEntity> void registerEntity(String id, EntityType.Builder<T> builder, AttributeSupplier supplier) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("smpmod", id));
        EntityType<T> type = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath("smpmod", id),
                builder.build(key));
        PolymerEntityUtils.registerType(type);
        FabricDefaultAttributeRegistry.register(type, supplier);
    }

    public static void register() {
        PolymerFishes.registerRods();
        PolymerFishes.registerFishes();
        PolymerPlants.register();
        PolymerIndustrial.registerMinerals();
        PolymerIndustrial.registerBlocks();
        PolymerIndustrial.registerRecipes();
        PolymerMisc.register();
    }
}
