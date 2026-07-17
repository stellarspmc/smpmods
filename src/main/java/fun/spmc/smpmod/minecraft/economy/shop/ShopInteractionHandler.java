package fun.spmc.smpmod.minecraft.economy.shop;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Interaction;

public class ShopInteractionHandler {
    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand != InteractionHand.MAIN_HAND || world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof Interaction interaction) {
                ShopData shop = ShopManager.getByInteraction(interaction.getUUID());

                if (shop != null && player instanceof ServerPlayer serverPlayer) {
                    if (serverPlayer.isShiftKeyDown() && shop.isOwner(serverPlayer)) shop.openOwnerMenu(serverPlayer);
                    else shop.processPurchase(serverPlayer);

                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });

        // 2. LEFT-CLICK / PUNCH (View Info or Destroy Shop)
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof Interaction interaction) {
                ShopData shop = ShopManager.getByInteraction(interaction.getUUID());

                if (shop != null && player instanceof ServerPlayer serverPlayer) {
                    if (serverPlayer.isShiftKeyDown() && shop.isOwner(serverPlayer)) ShopManager.removeShop(shop, (ServerLevel) world);
                    else serverPlayer.sendSystemMessage(shop.getFormattedInfoComponent((ServerLevel) world));

                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });
    }
}