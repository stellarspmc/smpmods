package fun.spmc.smpmod.minecraft.economy.shop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import fun.spmc.smpmod.minecraft.economy.EconomySavedData;
import fun.spmc.smpmod.minecraft.utils.MessageUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShopData {
    public static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<ShopData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUID_CODEC.fieldOf("shop_id").forGetter(ShopData::getShopId),
            UUID_CODEC.fieldOf("owner_id").forGetter(ShopData::getOwnerUuid),
            BlockPos.CODEC.fieldOf("barrel_pos").forGetter(ShopData::getBarrelPos),
            UUID_CODEC.fieldOf("interaction_id").forGetter(ShopData::getInteractionEntityUuid),
            UUID_CODEC.fieldOf("item_display_id").forGetter(ShopData::getItemDisplayUuid),
            UUID_CODEC.fieldOf("text_display_id").forGetter(ShopData::getTextDisplayUuid),
            ItemStack.CODEC.fieldOf("item_sold").forGetter(ShopData::getItemSold),
            Codec.INT.fieldOf("stack").forGetter(ShopData::getStack),
            Codec.DOUBLE.fieldOf("price").forGetter(ShopData::getPrice),
            Codec.list(ShopReceipt.CODEC).optionalFieldOf("receipts", List.of()).forGetter(ShopData::getReceipts)
    ).apply(instance, ShopData::new));

    private final UUID shopId;
    private final UUID ownerUuid;
    private final BlockPos barrelPos;

    private final UUID interactionEntityUuid;
    private final UUID itemDisplayUuid;
    private final UUID textDisplayUuid;

    private ItemStack itemSold;
    private int stack;
    private double price;
    private final List<ShopReceipt> receipts;

    public ShopData(UUID shopId, UUID ownerUuid, BlockPos barrelPos, UUID interaction, UUID item, UUID text, ItemStack itemSold, int stack, double price, List<ShopReceipt> receipts) {
        this.shopId = shopId;
        this.ownerUuid = ownerUuid;
        this.barrelPos = barrelPos;

        this.interactionEntityUuid = interaction;
        this.itemDisplayUuid = item;
        this.textDisplayUuid = text;

        this.itemSold = itemSold;
        this.stack = stack;
        this.price = price;
        this.receipts = new ArrayList<>(receipts);
    }

    public ShopData(UUID shopId, UUID ownerUuid, BlockPos barrelPos, UUID interaction, UUID item, UUID text, ItemStack itemSold, int stack, double price) {
        this(shopId, ownerUuid, barrelPos, interaction, item, text, itemSold, stack, price, new ArrayList<>());
    }

    public UUID getShopId() { return shopId; }
    public UUID getOwnerUuid() { return ownerUuid; }
    public BlockPos getBarrelPos() { return barrelPos; }
    public UUID getInteractionEntityUuid() { return interactionEntityUuid; }
    public UUID getItemDisplayUuid() { return itemDisplayUuid; }
    public UUID getTextDisplayUuid() { return textDisplayUuid; }
    public ItemStack getItemSold() { return itemSold; }
    public int getStack() { return stack; }
    public double getPrice() { return price; }
    public List<ShopReceipt> getReceipts() { return receipts; }

    public void recordReceipt(ShopReceipt receipt, ServerLevel level) {
        this.receipts.addFirst(receipt);
        while (this.receipts.size() > 27) this.receipts.removeLast();
        ShopManager.get(level).setDirty();
    }

    public boolean isOwner(ServerPlayer player) {
        return player.getUUID().equals(ownerUuid);
    }

    public int getAvailableStock(ServerLevel level) {
        if (!(level.getBlockEntity(barrelPos) instanceof Container container)) return 0;

        int totalItems = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack slotItem = container.getItem(i);
            if (ItemStack.isSameItemSameComponents(slotItem, itemSold)) totalItems += slotItem.getCount();
        }
        return totalItems / stack;
    }

    public Component getFormattedInfoComponent(ServerLevel level) {
        int available = getAvailableStock(level);

        return Component.literal("\uD83D\uDED2 ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("Shop details\n").withStyle(ChatFormatting.GOLD))
                .append(Component.literal("• selling ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(stack + "x ").withStyle(ChatFormatting.AQUA))
                .append(itemSold.getHoverName().copy().withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\n• price ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.format("$%.2f", price)).withStyle(ChatFormatting.GOLD))
                .append(Component.literal("\n• stock ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(available + " batches").withStyle(ChatFormatting.GREEN));
    }

    public void processPurchase(ServerPlayer buyer) {
        ServerLevel level = buyer.level();
        int availableBatches = getAvailableStock(level);
        if (availableBatches < 1) {
            MessageUtils.sendErrorMessage(buyer, "This shop is out of stock!");
            return;
        }

        EconomySavedData eco = EconomySavedData.get(level);
        if (eco.getBalance(buyer.getUUID()) < price) {
            MessageUtils.sendErrorMessage(buyer, String.format("✖: Insufficient funds! You need $%.2f.", price));
            return;
        }

        if (eco.changeBalance(buyer.getUUID(), -price)) {
            eco.changeBalance(ownerUuid, price);
            removeStockFromBarrel(level, stack);
            recordReceipt(new ShopReceipt(buyer.getUUID(), buyer.getScoreboardName(), stack, price, System.currentTimeMillis()), level);

            ItemStack itemsToGive = itemSold.copyWithCount(stack);
            if (!buyer.getInventory().add(itemsToGive)) buyer.drop(itemsToGive, false);

            buyer.sendSystemMessage(Component.literal("🏢: ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("Bought ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(stack + "x " + itemSold.getHoverName().getString()).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(String.format(" for $%.2f!", price)).withStyle(ChatFormatting.GOLD)));

            updateHologram(level);
        }
    }

    private void removeStockFromBarrel(ServerLevel level, int amountToRemove) {
        if (!(level.getBlockEntity(barrelPos) instanceof Container container)) return;

        for (int i = 0; i < container.getContainerSize(); i++) {
            if (amountToRemove <= 0) break;

            ItemStack slotItem = container.getItem(i);
            if (ItemStack.isSameItemSameComponents(slotItem, itemSold)) {
                int countInSlot = slotItem.getCount();
                int take = Math.min(countInSlot, amountToRemove);

                slotItem.shrink(take);
                amountToRemove -= take;
            }
        }
        container.setChanged();
    }

    public void setPrice(double price, ServerLevel level) {
        this.price = Math.round(Math.max(0.0, price) * 100.0) / 100.0;
        updateHologram(level);
        ShopManager.get(level).setDirty();
    }

    public void setStack(int stack, ServerLevel level) {
        this.stack = Math.max(1, stack);
        updateHologram(level);
        ShopManager.get(level).setDirty();
    }

    public void setItemSold(ItemStack newItem, ServerLevel level) {
        this.itemSold = newItem.copyWithCount(1);
        updateItemDisplay(level);
        updateHologram(level);
        ShopManager.get(level).setDirty();
    }

    public void updateItemDisplay(ServerLevel level) {
        Entity entity = level.getEntity(itemDisplayUuid);
        if (entity instanceof Display.ItemDisplay itemDisplay) {
            itemDisplay.setItemStack(itemSold.copy());
        }
    }

    public void updateHologram(ServerLevel level) {
        Entity entity = level.getEntity(textDisplayUuid);
        if (entity instanceof Display.TextDisplay textDisplay) {
            int stockBatches = getAvailableStock(level);
            String label = String.format("§f%dx §e%s\n§a$%.2f\nStock: %d", stack, itemSold.getHoverName().getString(), price, stockBatches);
            textDisplay.setText(Component.literal(label));
        }
    }

    public void openOwnerMenu(ServerPlayer owner) {
        ShopOwnerMenu.open(owner, this);
    }

    public void destroyShop(ServerLevel level) {
        safelyRemoveEntity(level, interactionEntityUuid);
        safelyRemoveEntity(level, itemDisplayUuid);
        safelyRemoveEntity(level, textDisplayUuid);
    }

    private void safelyRemoveEntity(ServerLevel level, UUID entityUuid) {
        if (entityUuid == null) return;
        Entity entity = level.getEntity(entityUuid);
        if (entity != null) entity.discard();
    }

    static class ShopOwnerMenu {

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

    record ShopReceipt(UUID buyerUuid, String buyerName, int stack, double price, long timestamp) {
        public static final Codec<ShopReceipt> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ShopData.UUID_CODEC.fieldOf("buyer_id").forGetter(ShopReceipt::buyerUuid),
                Codec.STRING.fieldOf("buyer_name").forGetter(ShopReceipt::buyerName),
                Codec.INT.fieldOf("stack").forGetter(ShopReceipt::stack),
                Codec.DOUBLE.fieldOf("price").forGetter(ShopReceipt::price),
                Codec.LONG.fieldOf("timestamp").forGetter(ShopReceipt::timestamp)
        ).apply(instance, ShopReceipt::new));
    }
}