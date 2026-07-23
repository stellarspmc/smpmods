package fun.spmc.smpmod.minecraft.economy.shop;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ShopReceiptsMenu extends ChestMenu {
    private final ShopData shopData;
    private final SimpleContainer container;

    public ShopReceiptsMenu(int containerId, Inventory playerInventory, ShopData shopData) {
        this(containerId, playerInventory, new SimpleContainer(27), shopData);
    }

    private ShopReceiptsMenu(int containerId, Inventory playerInventory, SimpleContainer container, ShopData shopData) {
        super(MenuType.GENERIC_9x3, containerId, playerInventory, container, 3);
        this.shopData = shopData;
        this.container = container;

        refreshGui();
    }

    public void refreshGui() {
        ItemStack filler = new ItemStack(Items.STAINED_GLASS_PANE.gray());
        filler.set(DataComponents.CUSTOM_NAME, Component.literal(" "));
        for (int i = 0; i < 27; i++) container.setItem(i, filler.copy());

        List<ShopReceipt> receipts = shopData.getReceipts();
        for (int i = 0; i < Math.min(receipts.size(), 22); i++) {
            ShopReceipt receipt = receipts.get(i);
            ItemStack receiptItem = new ItemStack(Items.PAPER);
            long diffMs = Math.max(0, System.currentTimeMillis() - receipt.timestamp());
            long mins = diffMs / 60000;
            String timeAgo = mins < 1 ? "Just now" : mins < 60 ? mins + "m ago" : (mins / 60) + "h ago";

            Component name = Component.literal("🧾 Sale to ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(receipt.buyerName()).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal("\n• Bought: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(receipt.stack() + "x ").withStyle(ChatFormatting.AQUA))
                    .append(shopData.getItemSold().getHoverName().copy().withStyle(ChatFormatting.AQUA))
                    .append(Component.literal("\n• Earned: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.format("$%.2f", receipt.price())).withStyle(ChatFormatting.GREEN))
                    .append(Component.literal("\n• Time: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(timeAgo).withStyle(ChatFormatting.DARK_GRAY));

            receiptItem.set(DataComponents.CUSTOM_NAME, name);
            container.setItem(i, receiptItem);
        }

        ItemStack backButton = new ItemStack(Items.BARRIER);
        backButton.set(DataComponents.CUSTOM_NAME, Component.literal("⬅ Back to Settings").withStyle(ChatFormatting.RED));
        container.setItem(22, backButton);
    }

    @Override
    public void clicked(int slotId, int button, @NonNull ContainerInput input, @NonNull Player player) {
        if (slotId == 22 && player instanceof ServerPlayer serverPlayer) {
            shopData.openOwnerMenu(serverPlayer);
            return;
        }

        if (slotId >= 0 && slotId < 27) return;
        super.clicked(slotId, button, input, player);
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return true;
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        return ItemStack.EMPTY;
    }
}
