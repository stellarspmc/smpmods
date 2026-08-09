package fun.spmc.smpmod.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

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

    /**protected static <T extends BlockEntity> BlockEntityType<T> createBlockEntity(String id, FabricBlockEntityTypeBuilder.Factory<? extends T> factory, Block... blocks) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(factory, blocks).build());
    }*/

    public static void register() {
        PolymerFishes.registerRods();
        PolymerFishes.registerFishes();
        PolymerPlants.register();
        PolymerIndustrial.registerMinerals();
    }
}
