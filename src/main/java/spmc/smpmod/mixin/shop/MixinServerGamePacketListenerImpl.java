package spmc.smpmod.mixin.shop;

import spmc.smpmod.economy.shop.ShopManager;
import spmc.smpmod.utils.MessageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListenerImpl {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "updateSignText", at = @At("TAIL"))
    private void smpmod$createShop(ServerboundSignUpdatePacket packet, List<FilteredText> lines, CallbackInfo ci) {
        ServerLevel serverLevel = this.player.level();
        BlockPos signPos = packet.getPos();

        if (!(serverLevel.getBlockEntity(signPos) instanceof SignBlockEntity signBlockEntity)) return;
        if (!serverLevel.dimension().identifier().getNamespace().equals("minecraft")) return;

        SignText signText = signBlockEntity.getText(packet.isFrontText());
        String line1 = signText.getMessage(0, false).getString().trim();
        String line2 = signText.getMessage(1, false).getString().trim();

        if (line1.equalsIgnoreCase("[shop]")) smpmod$handleCreation(this.player, serverLevel, signPos, line2);
        if (line1.equalsIgnoreCase("[ashop]")) smpmod$handleAdminShop(this.player, serverLevel, signPos, line2);
    }

    @Unique
    private void smpmod$handleCreation(ServerPlayer player, ServerLevel level, BlockPos signPos, String priceText) {
        BlockPos barrelPos = signPos.below();

        double price;
        try {
            price = Double.parseDouble(priceText.replace("$", "").trim());
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            MessageUtils.INSTANCE.sendError(player, "Invalid price format on line 2! Use e.g. $10.50", 0);
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            MessageUtils.INSTANCE.sendError(player, "Hold the item you want to sell in your main hand!", 0);
            return;
        }

        ShopManager.Companion.createShop(player, barrelPos, price, heldItem, level);
        level.destroyBlock(signPos, true);
        MessageUtils.INSTANCE.sendSuccess(player, "Shop created successfully!", 1);
    }

    @Unique
    private void smpmod$handleAdminShop(ServerPlayer player, ServerLevel level, BlockPos signPos, String priceText) {
        BlockPos barrelPos = signPos.below();
        if (!player.checkPermission(Identifier.fromNamespaceAndPath("smpmod", "admin"), PermissionLevel.GAMEMASTERS)) {
            level.destroyBlock(signPos, true);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText.replace("$", "").trim());
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            MessageUtils.INSTANCE.sendError(player, "Invalid price format on line 2! Use e.g. $10.50", 0);
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            MessageUtils.INSTANCE.sendError(player, "Hold the item you want to sell in your main hand!", 0);
            return;
        }

        ShopManager.createCreativeShop(barrelPos, price, heldItem, level);
        level.destroyBlock(signPos, true);
        MessageUtils.INSTANCE.sendSuccess(player, "Shop created successfully!", 1);
    } // i wasn't gonna include this but whatever
}
