package fun.spmc.smpmod.plant.seed;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import fun.spmc.smpmod.plant.crop.CropItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public class SeedBlock extends CropBlock implements PolymerBlock {
    public static final IntegerProperty BONEMEAL_COUNT = IntegerProperty.create("bonemeal_count", 0, 5);
    private final Supplier<CropItem> cropItemSupplier;

    public SeedBlock(Properties properties, Supplier<CropItem> cropItemSupplier) {
        super(properties);
        this.cropItemSupplier = cropItemSupplier;
        this.registerDefaultState(this.stateDefinition.any().setValue(getAgeProperty(), 0).setValue(BONEMEAL_COUNT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BONEMEAL_COUNT);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        int age = state.getValue(getAgeProperty());
        return Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, age);
    }

    @Override
    public void performBonemeal(@NotNull ServerLevel level, @NotNull RandomSource random, @NotNull BlockPos pos, @NotNull BlockState state) {
        super.performBonemeal(level, random, pos, state);
        int currentBonemeal = state.getValue(BONEMEAL_COUNT);
        if (currentBonemeal < 5) level.setBlock(pos, state.setValue(BONEMEAL_COUNT, currentBonemeal + 1), Block.UPDATE_CLIENTS);
    }

    @Override
    public void playerDestroy(Level level, @NonNull Player player, @NonNull BlockPos pos, @NonNull BlockState state, @Nullable BlockEntity blockEntity, @NonNull ItemStack tool) {
        if (!level.isClientSide() && isMaxAge(state)) {
            int bonemealUsed = state.getValue(BONEMEAL_COUNT);

            int rawQuality = level.getRandom().nextInt(5) + 1;
            int finalQuality = Math.max(1, rawQuality - (bonemealUsed > 2 ? 1 : 0));

            CropItem cropItem = cropItemSupplier.get();
            ItemStack harvestedCrop = cropItem.createCropInstance(finalQuality, Map.of());

            popResource(level, pos, harvestedCrop);
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }
}
