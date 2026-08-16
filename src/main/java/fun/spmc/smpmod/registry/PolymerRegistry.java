package fun.spmc.smpmod.registry;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import fun.spmc.smpmod.utils.BaseImplementedItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

@SuppressWarnings({"DataFlowIssue", "UnusedReturnValue", "SameParameterValue"})
public class PolymerRegistry implements ModInitializer {
    protected static <T extends Item> T createItem(String id, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smpmod", id));
        Item.Properties properties = new Item.Properties().setId(key);
        T item = factory.apply(properties);
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    protected static Block createBlockWithItem(String id, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties properties, Item item) {
        Identifier identifier = Identifier.fromNamespaceAndPath("smpmod", id);
        BlockItemId blockId = BlockItemId.create(identifier, identifier);
        Block block = blockFactory.apply(properties.setId(blockId.block()));
        Registry.register(BuiltInRegistries.ITEM, blockId.item(), new BaseImplementedItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(blockId.item()), item, id));
        return Registry.register(BuiltInRegistries.BLOCK, identifier, block);
    }

    protected static Block createBlockOnly(String id, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties properties) {
        Identifier identifier = Identifier.fromNamespaceAndPath("smpmod", id);
        Block block = blockFactory.apply(properties.setId(ResourceKey.create(Registries.BLOCK, identifier)));
        return Registry.register(BuiltInRegistries.BLOCK, identifier, block);
    }

    protected static <T extends BlockEntity> BlockEntityType<T> createBlockWithItemEntity(String id, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties properties, FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory, Item item) {
        Identifier identifier = Identifier.fromNamespaceAndPath("smpmod", id);
        BlockItemId blockId = BlockItemId.create(identifier, identifier);
        Block block = blockFactory.apply(properties.setId(ResourceKey.create(Registries.BLOCK, identifier)));
        Registry.register(BuiltInRegistries.BLOCK, identifier, block);
        Registry.register(BuiltInRegistries.ITEM, blockId.item(), new BaseImplementedItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(blockId.item()), item, id));
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, identifier, FabricBlockEntityTypeBuilder.<T>create(entityFactory, block).build());

    }

    protected static <T extends Recipe<?>> RecipeType<T> registerRecipeType(String id) {
        Identifier identifier = Identifier.fromNamespaceAndPath("smpmod", id);
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, identifier, new RecipeType<T>() {@Override public String toString() { return identifier.toString(); }});
    }

    // 1 input recipe serializer
    protected static <T extends Recipe<?>> RecipeSerializer<T> registerSingleInputSerializer(String id, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath("smpmod", id), new RecipeSerializer<>(codec, streamCodec));
    }

    protected static <T extends LivingEntity> void registerEntity(String id, EntityType.Builder<T> builder, AttributeSupplier.Builder supplier) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("smpmod", id));
        EntityType<T> type = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath("smpmod", id),
                builder.build(key));
        PolymerEntityUtils.registerType(type);
        FabricDefaultAttributeRegistry.register(type, supplier);
    }

    @Override
    public void onInitialize() {
        PolymerFishes.registerRods();
        PolymerFishes.registerFishes();
        PolymerPlants.register();
        PolymerIndustrial.registerMinerals();
        PolymerIndustrial.registerBlocks();
        PolymerMisc.register();
        PolymerIndustrial.registerRecipes();
    }
}
