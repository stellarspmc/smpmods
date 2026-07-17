package fun.spmc.smpmod.minecraft.economy.shop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fun.spmc.smpmod.minecraft.economy.EconomySavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

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
            Codec.DOUBLE.fieldOf("price").forGetter(ShopData::getPrice)
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

    public ShopData(UUID shopId, UUID ownerUuid, BlockPos barrelPos, UUID interaction, UUID item, UUID text, ItemStack itemSold, int stack, double price) {
        this.shopId = shopId;
        this.ownerUuid = ownerUuid;
        this.barrelPos = barrelPos;

        this.interactionEntityUuid = interaction;
        this.itemDisplayUuid = item;
        this.textDisplayUuid = text;

        this.itemSold = itemSold;
        this.stack = stack;
        this.price = price;
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
        return Component.literal("--- SHOP INFO ---").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("\nSelling: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(stack + "x ").withStyle(ChatFormatting.AQUA))
                .append(itemSold.getHoverName())
                .append(Component.literal(String.format("\nPrice: $%.2f", price)).withStyle(ChatFormatting.GREEN))
                .append(Component.literal(String.format("\nAvailable Stock: %d purchases", available)).withStyle(ChatFormatting.YELLOW));
    }

    public void processPurchase(ServerPlayer buyer) {
        ServerLevel level = buyer.level();

        int availableBatches = getAvailableStock(level);
        if (availableBatches < 1) {
            buyer.sendSystemMessage(Component.literal("ERR: This shop is out of stock!").withStyle(ChatFormatting.RED));
            return;
        }

        EconomySavedData eco = EconomySavedData.get(level);

        if (eco.getBalance(buyer.getUUID()) < price) {
            buyer.sendSystemMessage(Component.literal(String.format("ERR: Insufficient funds! You need $%.2f.", price)).withStyle(ChatFormatting.RED));
            return;
        }

        if (eco.changeBalance(buyer.getUUID(), -price)) {
            eco.changeBalance(ownerUuid, price);
            removeStockFromBarrel(level, stack);

            ItemStack itemsToGive = itemSold.copyWithCount(stack);
            if (!buyer.getInventory().add(itemsToGive)) buyer.drop(itemsToGive, false);

            buyer.sendSystemMessage(Component.literal("SHOP: ").withStyle(ChatFormatting.GREEN)
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
            String label = String.format("%dx %s — $%.2f | Stock: %d",
                    stack, itemSold.getHoverName().getString(), price, stockBatches);
            textDisplay.setText(Component.literal(label));
        }
    }

    public void openOwnerMenu(ServerPlayer owner) {
        ServerLevel level = owner.level();

        owner.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, _) -> new ShopOwnerMenu(containerId, playerInventory, this, level),
                Component.literal("Shop Settings")
        ));
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
}