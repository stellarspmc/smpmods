package fun.spmc.smpmod.minecraft.economy.shop;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Interaction;

public class ShopInteractionHandler {
    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, _) -> {
            if (hand != InteractionHand.MAIN_HAND || world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof Interaction interaction) {
                ShopData shop = ShopManager.getByInteraction((ServerLevel) world, interaction.getUUID());

                if (shop != null && player instanceof ServerPlayer serverPlayer) {
                    if (serverPlayer.isShiftKeyDown() && shop.isOwner(serverPlayer)) shop.openOwnerMenu(serverPlayer);
                    else shop.processPurchase(serverPlayer);

                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, _, entity, _) -> {
            if (world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof Interaction interaction) {
                ShopData shop = ShopManager.getByInteraction((ServerLevel) world, interaction.getUUID());

                if (shop != null && player instanceof ServerPlayer serverPlayer) {
                    if (serverPlayer.isShiftKeyDown() && shop.isOwner(serverPlayer)) ShopManager.removeShop(shop, (ServerLevel) world);
                    else serverPlayer.sendSystemMessage(shop.getFormattedInfoComponent((ServerLevel) world));

                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, _, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            ShopData shop = ShopManager.getByPos((ServerLevel) world, pos);
            if (shop != null && player instanceof ServerPlayer serverPlayer) {
                if (!shop.isOwner(serverPlayer)) {
                    serverPlayer.sendSystemMessage(
                            Component.literal("✖: You cannot open someone else's shop barrel!")
                                    .withStyle(ChatFormatting.RED)
                    );
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, _, _) -> {
            if (world.isClientSide()) return true;

            ShopData shop = ShopManager.getByPos((ServerLevel) world, pos);
            if (shop != null && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(
                        Component.literal("✖: You cannot break a shop!")
                                .withStyle(ChatFormatting.RED)
                );
                return false;
            }
            return true;
        });
    }
}