package fun.spmc.smpmod.registry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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

    public static void register() {
        PolymerFishes.registerRods();
        PolymerFishes.registerFishes();
        PolymerPlants.register();
        PolymerIndustrial.registerMinerals();
        PolymerIndustrial.registerBlocks();
        PolymerIndustrial.registerRecipes();
    }
}
