package fun.spmc.smpmod.minecraft.mixin.shop;

import fun.spmc.smpmod.minecraft.economy.shop.ShopManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
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
    private void smpmods$createShop(ServerboundSignUpdatePacket packet, List<FilteredText> lines, CallbackInfo ci) {
        ServerLevel serverLevel = this.player.level();
        BlockPos signPos = packet.getPos();

        if (!(serverLevel.getBlockEntity(signPos) instanceof SignBlockEntity signBlockEntity)) return;

        SignText signText = signBlockEntity.getText(packet.isFrontText());
        String line1 = signText.getMessage(0, false).getString().trim();
        String line2 = signText.getMessage(1, false).getString().trim();

        if (line1.equalsIgnoreCase("[Shop]")) {
            smpmods$handleCreation(this.player, serverLevel, signPos, line2);
        }
    }

    @Unique
    private void smpmods$handleCreation(ServerPlayer player, ServerLevel level, BlockPos signPos, String priceText) {
        BlockPos barrelPos = signPos.below();

        double price;
        try {
            price = Double.parseDouble(priceText.replace("$", "").trim());
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendSystemMessage(Component.literal("ERR: Invalid price format on line 2! Use e.g. $10.50").withStyle(ChatFormatting.RED));
            return;
        }

        // 3. Check held item
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            player.sendSystemMessage(Component.literal("ERR: Hold the item you want to sell in your main hand!").withStyle(ChatFormatting.RED));
            return;
        }

        ShopManager.createShop(player.getUUID(), barrelPos, price, heldItem, level);
        level.destroyBlock(signPos, true);

        player.sendSystemMessage(Component.literal("SHOP: Shop created successfully!").withStyle(ChatFormatting.GREEN));
    }
}
