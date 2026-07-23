package fun.spmc.smpmod.minecraft.economy.shop;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public class ShopOwnerMenu extends ChestMenu {
    private final ShopData shopData;
    private final ServerLevel level;
    private final SimpleContainer container;

    public ShopOwnerMenu(int containerId, Inventory playerInventory, ShopData shopData, ServerLevel level) {
        this(containerId, playerInventory, new SimpleContainer(27), shopData, level);
    }

    private ShopOwnerMenu(int containerId, Inventory playerInventory, SimpleContainer container, ShopData shopData, ServerLevel level) {
        super(MenuType.GENERIC_9x3, containerId, playerInventory, container, 3);
        this.shopData = shopData;
        this.level = level;
        this.container = container;

        refreshGui();
    }

    public void refreshGui() {
        ItemStack filler = new ItemStack(Items.STAINED_GLASS_PANE.gray());
        filler.set(DataComponents.CUSTOM_NAME, Component.literal(" "));
        for (int i = 0; i < 27; i++) {
            container.setItem(i, filler.copy());
        }

        ItemStack minusPrice = new ItemStack(Items.STAINED_GLASS_PANE.red());
        minusPrice.set(DataComponents.CUSTOM_NAME, Component.literal("- $1.00").withStyle(ChatFormatting.RED)
                .append(Component.literal(" (Right-click: - $0.10)").withStyle(ChatFormatting.GRAY)));
        container.setItem(10, minusPrice);

        ItemStack priceDisplay = new ItemStack(Items.GOLD_INGOT);
        priceDisplay.set(DataComponents.CUSTOM_NAME, Component.literal(String.format("Current Price: $%.2f", shopData.getPrice())).withStyle(ChatFormatting.GOLD));
        container.setItem(11, priceDisplay);

        ItemStack plusPrice = new ItemStack(Items.STAINED_GLASS_PANE.lime());
        plusPrice.set(DataComponents.CUSTOM_NAME, Component.literal("+ $1.00").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(" (Right-click: + $0.10)").withStyle(ChatFormatting.GRAY)));
        container.setItem(12, plusPrice);

        ItemStack soldDisplay = shopData.getItemSold().copyWithCount(Math.min(shopData.getStack(), 64));
        soldDisplay.set(DataComponents.CUSTOM_NAME, Component.literal("Selling: ").withStyle(ChatFormatting.YELLOW)
                .append(shopData.getItemSold().getHoverName())
                .append(Component.literal("\n\nClick with an item on your cursor to swap!").withStyle(ChatFormatting.DARK_GRAY)));
        container.setItem(13, soldDisplay);

        ItemStack minusStack = new ItemStack(Items.STAINED_GLASS_PANE.red());
        minusStack.set(DataComponents.CUSTOM_NAME, Component.literal("- 1 Batch Size").withStyle(ChatFormatting.RED)
                .append(Component.literal(" (Right-click: - 5)").withStyle(ChatFormatting.GRAY)));
        container.setItem(14, minusStack);

        ItemStack stackDisplay = new ItemStack(Items.BARREL);
        stackDisplay.set(DataComponents.CUSTOM_NAME, Component.literal("Batch Size: " + shopData.getStack()).withStyle(ChatFormatting.AQUA));
        container.setItem(15, stackDisplay);

        ItemStack plusStack = new ItemStack(Items.STAINED_GLASS_PANE.lime());
        plusStack.set(DataComponents.CUSTOM_NAME, Component.literal("+ 1 Batch Size").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(" (Right-click: + 5)").withStyle(ChatFormatting.GRAY)));
        container.setItem(16, plusStack);

        ItemStack receiptsButton = new ItemStack(Items.PAPER);
        receiptsButton.set(DataComponents.CUSTOM_NAME, Component.literal("📜 View Sales Receipts").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("\n\nClick to inspect transaction history!").withStyle(ChatFormatting.GRAY)));
        container.setItem(22, receiptsButton);
    }

    @Override
    public void clicked(int slotId, int button, @NonNull ContainerInput input, @NonNull Player player) {
        if (slotId >= 0 && slotId < 27 && player instanceof ServerPlayer) {
            handleButtonClick(slotId, button, (ServerPlayer) player);
            return;
        }
        super.clicked(slotId, button, input, player);
    }

    private void handleButtonClick(int slotId, int button, ServerPlayer player) {
        boolean isRightClick = (button == 1);

        switch (slotId) {
            case 10 -> {
                double step = isRightClick ? .1 : 1;
                double newPrice = Math.max(0, shopData.getPrice() - step);
                shopData.setPrice(newPrice, level);
            }
            case 12 -> {
                double step = isRightClick ? .1 : 1;
                shopData.setPrice(shopData.getPrice() + step, level);
            }
            case 13 -> {
                ItemStack carried = getCarried();
                if (!carried.isEmpty()) shopData.setItemSold(carried, level);
            }
            case 14 -> {
                int step = isRightClick ? 5 : 1;
                int newStack = Math.max(1, shopData.getStack() - step);
                shopData.setStack(newStack, level);
            }
            case 16 -> {
                int step = isRightClick ? 5 : 1;
                shopData.setStack(shopData.getStack() + step, level);
            }
            case 22 -> {
                player.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, _) -> new ShopReceiptsMenu(containerId, playerInventory, shopData),
                        Component.literal("Sales History")
                ));
                return;
            }
        }

        refreshGui();
        broadcastChanges();
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