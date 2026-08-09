package fun.spmc.smpmod.industrial.machine;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import fun.spmc.smpmod.industrial.recipe.CompressorRecipe;
import fun.spmc.smpmod.registry.PolymerIndustrial;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class SmeltryBlock extends Block implements PolymerBlock {
    public static final MapCodec<SmeltryBlock> CODEC = simpleCodec(SmeltryBlock::new);

    public SmeltryBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull MapCodec<? extends SmeltryBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        return Blocks.SMOKER.defaultBlockState().setValue(BlockStateProperties.LIT, true);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer)
            serverPlayer.sendSystemMessage(Component.literal("The smeltry is a work in progress..."));//new SmeltryUI(serverPlayer).open();
        return InteractionResult.SUCCESS;
    }

}