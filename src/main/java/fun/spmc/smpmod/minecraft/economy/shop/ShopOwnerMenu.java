package fun.spmc.smpmod.minecraft.economy.shop;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.List;

public class ShopOwnerMenu {

    public static void open(ServerPlayer player, ShopData shopData) {
        if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUUID())) openBedrockForm(player, shopData);
        else openJavaGui(player, shopData);
    }

    private static void openJavaGui(ServerPlayer player, ShopData shopData) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false) {
            @Override
            public void onOpen() {
                refreshGui(this, player, shopData);
            }
        };
        gui.setTitle(Component.literal("Shop Settings"));
        refreshGui(gui, player, shopData);
        gui.open();
    }

    private static void refreshGui(SimpleGui gui, ServerPlayer player, ShopData shopData) {
        GuiElementBuilder filler = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal(" "));
        for (int i = 0; i < 27; i++) gui.setSlot(i, filler);
        gui.setSlot(10, new GuiElementBuilder(Items.STAINED_GLASS_PANE.red())
                .setName(Component.literal("- $1.00").withStyle(ChatFormatting.RED)
                        .append(Component.literal(" (Right-click: - $0.10)").withStyle(ChatFormatting.GRAY)))
                .setCallback((type) -> {
                    double step = type.isRight ? 0.1 : 1.0;
                    shopData.setPrice(Math.max(0, shopData.getPrice() - step), player.level());
                    refreshGui(gui, player, shopData);
                }));
        gui.setSlot(11, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal(String.format("Current Price: $%.2f", shopData.getPrice())).withStyle(ChatFormatting.GOLD)));
        gui.setSlot(12, new GuiElementBuilder(Items.STAINED_GLASS_PANE.lime())
                .setName(Component.literal("+ $1.00").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(" (Right-click: + $0.10)").withStyle(ChatFormatting.GRAY)))
                .setCallback((type) -> {
                    double step = type.isRight ? 0.1 : 1.0;
                    shopData.setPrice(shopData.getPrice() + step, player.level());
                    refreshGui(gui, player, shopData);
                }));

        ItemStack soldDisplay = shopData.getItemSold().copyWithCount(Math.min(shopData.getStack(), 64));
        soldDisplay.set(DataComponents.CUSTOM_NAME, Component.literal("Selling: ").withStyle(ChatFormatting.YELLOW)
                .append(shopData.getItemSold().getHoverName())
                .append(Component.literal("\n\nClick with an item on your cursor to swap!").withStyle(ChatFormatting.DARK_GRAY)));
        gui.setSlot(13, GuiElementBuilder.from(soldDisplay)
                .setCallback((_) -> {
                    ItemStack carried = gui.getPlayer().containerMenu.getCarried();
                    if (!carried.isEmpty()) {
                        shopData.setItemSold(carried.copy(), player.level());
                        refreshGui(gui, player, shopData);
                    }
                }));
        gui.setSlot(14, new GuiElementBuilder(Items.STAINED_GLASS_PANE.red())
                .setName(Component.literal("- 1 Batch Size").withStyle(ChatFormatting.RED)
                        .append(Component.literal(" (Right-click: - 5)").withStyle(ChatFormatting.GRAY)))
                .setCallback((type) -> {
                    int step = type.isRight ? 5 : 1;
                    shopData.setStack(Math.max(1, shopData.getStack() - step), player.level());
                    refreshGui(gui, player, shopData);
                }));
        gui.setSlot(15, new GuiElementBuilder(Items.BARREL)
                .setName(Component.literal("Batch Size: " + shopData.getStack()).withStyle(ChatFormatting.AQUA)));
        gui.setSlot(16, new GuiElementBuilder(Items.STAINED_GLASS_PANE.lime())
                .setName(Component.literal("+ 1 Batch Size").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(" (Right-click: + 5)").withStyle(ChatFormatting.GRAY)))
                .setCallback((type) -> {
                    int step = type.isRight ? 5 : 1;
                    shopData.setStack(shopData.getStack() + step, player.level());
                    refreshGui(gui, player, shopData);
                }));
        gui.setSlot(22, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("📜 View Sales Receipts").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal("\n\nClick to inspect transaction history!").withStyle(ChatFormatting.GRAY)))
                .setCallback((_) -> openReceiptsGui(player, shopData)));
    }

    private static void openBedrockForm(ServerPlayer player, ShopData shopData) {
        CustomForm form = CustomForm.builder()
                .title("Shop Settings")
                .input("Price ($)", "Enter new price", String.format("%.2f", shopData.getPrice()))
                .input("Batch Size", "Enter batch size", String.valueOf(shopData.getStack()))
                .label("💡 To swap the item sold, hold the new item in your main hand before submitting!")
                .validResultHandler(response -> {
                    String priceStr = response.next();
                    String stackStr = response.next();
                    try {
                        assert priceStr != null;
                        double price = Math.max(0, Double.parseDouble(priceStr));
                        assert stackStr != null;
                        int stack = Math.max(1, Integer.parseInt(stackStr));

                        shopData.setPrice(price, player.level());
                        shopData.setStack(stack, player.level());

                        ItemStack heldItem = player.getMainHandItem();
                        if (!heldItem.isEmpty()) shopData.setItemSold(heldItem.copy(), player.level());
                    } catch (NumberFormatException ignored) {}
                })
                .build();

        FloodgateApi.getInstance().sendForm(player.getUUID(), form);
    }

    public static void openReceiptsGui(ServerPlayer player, ShopData shopData) {
        if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUUID())) openBedrockReceiptsForm(player, shopData);
        else openJavaReceiptsGui(player, shopData);
    }

    private static void openJavaReceiptsGui(ServerPlayer player, ShopData shopData) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Sales History"));

        GuiElementBuilder filler = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal(" "));
        for (int i = 0; i < 27; i++) gui.setSlot(i, filler);

        List<ShopReceipt> receipts = shopData.getReceipts();
        for (int i = 0; i < Math.min(receipts.size(), 22); i++) {
            ShopReceipt receipt = receipts.get(i);
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

            gui.setSlot(i, new GuiElementBuilder(Items.PAPER).setName(name));
        }

        gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("⬅ Back to Settings").withStyle(ChatFormatting.RED))
                .setCallback((_) -> open(player, shopData)));

        gui.open();
    }

    private static void openBedrockReceiptsForm(ServerPlayer player, ShopData shopData) {
        SimpleForm.Builder form = SimpleForm.builder().title("Sales History");
        List<ShopReceipt> receipts = shopData.getReceipts();

        if (receipts.isEmpty()) form.content("No sales recorded yet.");
        else {
            StringBuilder content = new StringBuilder();
            for (ShopReceipt receipt : receipts) {
                long mins = Math.max(0, System.currentTimeMillis() - receipt.timestamp()) / 60000;
                String timeAgo = mins < 1 ? "Just now" : mins < 60 ? mins + "m ago" : (mins / 60) + "h ago";
                content.append(String.format("• %s bought %dx for $%.2f (%s)\n", receipt.buyerName(), receipt.stack(), receipt.price(), timeAgo));
            }
            form.content(content.toString());
        }

        form.button("Back to Settings")
                .validResultHandler(_ -> open(player, shopData));

        FloodgateApi.getInstance().sendForm(player.getUUID(), form.build());
    }
}