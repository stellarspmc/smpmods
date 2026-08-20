package fun.spmc.smpmod.plant;

import eu.pb4.polymer.core.api.block.PolymerBlock;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public class SeedBlock extends CropBlock implements PolymerBlock {
    public static final IntegerProperty BONEMEAL_COUNT = IntegerProperty.create("bonemeal_count", 0, 5);
    private final Supplier<CropItem> cropItemSupplier;
    private final int[] boneMealAffection = new int[]{0, 1, 1, 3, 3, 5};

    public SeedBlock(Properties properties, Supplier<CropItem> cropItemSupplier) {
        super(properties.mapColor(state -> state.getValue(CropBlock.AGE) >= 6 ? MapColor.COLOR_YELLOW : MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY));
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
        BlockState updatedState = level.getBlockState(pos);
        int currentBonemeal = state.getValue(BONEMEAL_COUNT);
        if (currentBonemeal < 5 && updatedState.is(this)) level.setBlock(pos, updatedState.setValue(BONEMEAL_COUNT, currentBonemeal + 1), Block.UPDATE_CLIENTS);
    }

    @Override
    public void playerDestroy(Level level, @NonNull Player player, @NonNull BlockPos pos, @NonNull BlockState state, @Nullable BlockEntity blockEntity, @NonNull ItemStack tool) {
        if (!level.isClientSide() && isMaxAge(state)) {
            int bonemealUsed = state.getValue(BONEMEAL_COUNT); // basic impl, TODO: make this more sophisticated -> impl fusing
            int finalQuality = Math.clamp(rollStarQuality(level.getRandom(), player.getLuck()) - boneMealAffection[bonemealUsed], -2, 5);

            CropItem cropItem = cropItemSupplier.get();
            ItemStack harvestedCrop = cropItem.createCropInstance(finalQuality, Map.of());

            popResource(level, pos, harvestedCrop);
        } super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    private static final double[] BASE_STAR_WEIGHTS = { 1000, 600, 300, 120, 35, 6 };

    private static int rollStarQuality(RandomSource random, float luckBonus) { // copied from fishing, TODO: change it bruv
        double[] adjustedWeights = new double[BASE_STAR_WEIGHTS.length];
        double totalWeight = 0;

        for (int star = 0; star < BASE_STAR_WEIGHTS.length; star++) {
            double weight = BASE_STAR_WEIGHTS[star] * Math.pow(luckBonus, star);
            adjustedWeights[star] = weight;
            totalWeight += weight;
        }

        double roll = random.nextDouble() * totalWeight;
        double cumulative = 0;

        for (int star = 0; star < adjustedWeights.length; star++) {
            cumulative += adjustedWeights[star];
            if (roll < cumulative) return star;
        }

        return 0;
    }
}
