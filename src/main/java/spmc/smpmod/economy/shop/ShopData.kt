package spmc.smpmod.economy.shop

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.UUIDUtil
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Display.ItemDisplay
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import org.geysermc.cumulus.form.CustomForm
import org.geysermc.cumulus.form.SimpleForm
import org.geysermc.floodgate.api.FloodgateApi
import spmc.smpmod.SMPMod
import spmc.smpmod.economy.EconomyData
import spmc.smpmod.utils.MessageUtils.sendError
import spmc.smpmod.utils.UtilFunc.isAdmin
import spmc.smpmod.utils.UtilFunc.rnd2DP
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class ShopData(val shopId: UUID, val ownerUuid: UUID, val dimension: ResourceKey<Level>, val barrelPos: BlockPos, val interactionEntityUuid: UUID, val itemDisplayUuid: UUID, val textDisplayUuid: UUID, private var itemSold: ItemStack, private var stack: Int, private var price: Double, receipts: MutableList<ShopReceipt>, val isCreative: Boolean) {
    val receipts: MutableList<ShopReceipt> = ArrayList<ShopReceipt>(receipts)

    constructor(shopId: UUID, ownerUuid: UUID, dimension: ResourceKey<Level>, barrelPos: BlockPos, interaction: UUID, item: UUID, text: UUID, itemSold: ItemStack, stack: Int, price: Double, creative: Boolean) : this(shopId, ownerUuid, dimension, barrelPos, interaction, item, text, itemSold, stack, price, ArrayList<ShopReceipt>(), creative)
    fun getItemSold() = itemSold
    fun getStack() = stack
    fun getPrice() = price
    fun openOwnerMenu(owner: ServerPlayer) { ShopOwnerMenu.open(owner, this) }
	fun isOwner(player: ServerPlayer) = (this.isCreative && isAdmin(player)) || player.getUUID() == ownerUuid

	val level: ServerLevel? get() = SMPMod.minecraftServer?.getLevel(dimension)

    fun recordReceipt(receipt: ShopReceipt) {
        this.receipts.addFirst(receipt)
        while (this.receipts.size > 27) this.receipts.removeLast()
        ShopManager.get(this.level?: return).setDirty()
    } // TODO: yuu ($1 -> 1 pt, 100pt -> $1)
	// TODO: webshop

    val availableStock: Int get() {
        if (this.isCreative) return Int.MAX_VALUE

        var totalItems = 0
        val container = this.level?.getBlockEntity(barrelPos) as? Container ?: return 0
        for (i in 0..<container.containerSize) {
            val slotItem: ItemStack = container.getItem(i)
            if (ItemStack.isSameItemSameComponents(slotItem, itemSold)) totalItems += slotItem.count
        }
        return totalItems / stack
    }

    fun getFormattedInfoComponent(): Component {
        return Component.literal("\uD83D\uDED2 ").withStyle(ChatFormatting.GOLD)
            .append(Component.literal("Shop Details\n").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("Selling: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(stack.toString() + "x ").withStyle(ChatFormatting.AQUA))
            .append(itemSold.hoverName.copy().withStyle(ChatFormatting.AQUA))
            .append(Component.literal("\nPrice: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(String.format("$%.2f", price)).withStyle(ChatFormatting.GOLD))
            .append(Component.literal("\nStock: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(if (this.isCreative) "∞" else this.availableStock.toString() + " batches").withStyle(ChatFormatting.GREEN))
    }

    fun processPurchase(buyer: ServerPlayer): Int {
        val availableBatches = this.availableStock
        if (availableBatches < 1) return sendError(buyer, "This shop is out of stock!", 0)

        val eco: EconomyData = EconomyData.get() ?: return 0
        if (eco.getBalance(buyer.getUUID()) < price) return sendError(buyer, String.format("✖: Insufficient funds! You need $%.2f.", price), 0)

        if (eco.changeBalance(buyer.getUUID(), -price)) {
            if (!this.isCreative) {
                eco.changeBalance(ownerUuid, price)
                removeStockFromBarrel(stack)
            }

            recordReceipt(ShopReceipt(buyer.getUUID(), buyer.scoreboardName, stack, price, System.currentTimeMillis()))

            val itemsToGive = itemSold.copyWithCount(stack)
            if (!buyer.inventory.add(itemsToGive)) buyer.drop(itemsToGive, false)

            buyer.sendSystemMessage(Component.literal("🏢: ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("Bought ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(stack.toString() + "x " + itemSold.hoverName.string).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(String.format(" for $%.2f!", price)).withStyle(ChatFormatting.GOLD))
            )

            updateHologram()
        }
        return 1
    }

    private fun removeStockFromBarrel(amountToRemove: Int) {
        var amountToRemove = amountToRemove

        val container = this.level?.getBlockEntity(barrelPos) as? Container ?: return
        for (i in 0..<container.containerSize) {
            if (amountToRemove <= 0) break

            val slotItem: ItemStack = container.getItem(i)
            if (ItemStack.isSameItemSameComponents(slotItem, itemSold)) {
                val countInSlot = slotItem.count
                val take = min(countInSlot, amountToRemove)

                slotItem.shrink(take)
                amountToRemove -= take
            }
        }
        container.setChanged()
    }

    fun setPrice(price: Double) {
        this.price = rnd2DP(max(.0, price))
        updateHologram()
        ShopManager.get(this.level?: return).setDirty()
    }

    fun setStack(stack: Int) {
        this.stack = max(1, stack)
        updateHologram()
        ShopManager.get(this.level?: return).setDirty()
    }

    fun setItemSold(newItem: ItemStack) {
        this.itemSold = newItem.copyWithCount(1)
        updateItemDisplay()
        updateHologram()
        ShopManager.get(this.level?: return).setDirty()
    }

    fun updateItemDisplay() { ((this.level?: return).getEntity(itemDisplayUuid) as? ItemDisplay)?.itemStack = itemSold.copy() }
    fun updateHologram() { ((this.level?: return).getEntity(textDisplayUuid) as? Display.TextDisplay)?.text = Component.literal(String.format("§f%dx §e%s\n§a$%.2f\nStock: %s", stack, itemSold.hoverName.string, price, if (this.isCreative) "∞" else this.availableStock.toString())) }
    private fun safelyRemoveEntity(entityUuid: UUID) { ((this.level?: return).getEntity(entityUuid))?.discard() }

    fun destroyShop() {
        safelyRemoveEntity(interactionEntityUuid)
        safelyRemoveEntity(itemDisplayUuid)
        safelyRemoveEntity(textDisplayUuid)
    }

    internal object ShopOwnerMenu {
        fun open(player: ServerPlayer, shopData: ShopData) {
            if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUUID())) openBedrockForm(player, shopData)
            else openJavaGui(player, shopData)
        }

        private fun openJavaGui(player: ServerPlayer, shopData: ShopData) {
            val gui: SimpleGui = object : SimpleGui(MenuType.GENERIC_9x3, player, false) { override fun onOpen() { refreshGui(this, player, shopData) }}
            gui.setTitle(Component.literal("Shop Settings"))
            refreshGui(gui, player, shopData)
            gui.open()
        }

        private fun refreshGui(gui: SimpleGui, player: ServerPlayer, shopData: ShopData) {
            val filler = GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal(" "))
            for (i in 0..26) gui.setSlot(i, filler)
            gui.setSlot(10, GuiElementBuilder(Items.STAINED_GLASS_PANE.red())
                    .setName(Component.literal("- $1.00").withStyle(ChatFormatting.RED).append(Component.literal(" (Right-click: - $0.10)").withStyle(ChatFormatting.GRAY)))
                    .setCallback { type ->
                        val step = if (type.isRight) 0.1 else 1.0
                        shopData.setPrice(max(.0, shopData.getPrice() - step))
                        refreshGui(gui, player, shopData)
                    }
            )
            gui.setSlot(11, GuiElementBuilder(Items.GOLD_INGOT).setName(Component.literal(String.format("Current Price: $%.2f", shopData.getPrice())).withStyle(ChatFormatting.GOLD)))
            gui.setSlot(12, GuiElementBuilder(Items.STAINED_GLASS_PANE.lime())
                    .setName(Component.literal("+ $1.00").withStyle(ChatFormatting.GREEN).append(Component.literal(" (Right-click: + $0.10)").withStyle(ChatFormatting.GRAY)))
                    .setCallback { type ->
                        val step = if (type.isRight) 0.1 else 1.0
                        shopData.setPrice(shopData.getPrice() + step)
                        refreshGui(gui, player, shopData)
                    }
            )

            val soldDisplay = shopData.getItemSold().copyWithCount(min(shopData.getStack(), 64))
            soldDisplay.set(DataComponents.CUSTOM_NAME, Component.literal("Selling: ").withStyle(ChatFormatting.YELLOW)
                .append(shopData.getItemSold().hoverName)
                .append(Component.literal("\n\nClick with an item on your cursor to swap!").withStyle(ChatFormatting.DARK_GRAY))
            )
            gui.setSlot(13, GuiElementBuilder.from(soldDisplay)
                .setCallback { _ -> val carried = gui.getPlayer().containerMenu.carried
                    if (!carried.isEmpty) {
                        shopData.setItemSold(carried.copy())
                        refreshGui(gui, player, shopData)
                    }
                }
            )
            gui.setSlot(14, GuiElementBuilder(Items.STAINED_GLASS_PANE.red())
                .setName(Component.literal("- 1 Batch Size").withStyle(ChatFormatting.RED).append(Component.literal(" (Right-click: - 5)").withStyle(ChatFormatting.GRAY)))
                .setCallback { type ->
                    shopData.setStack(max(1, shopData.getStack() - if (type.isRight) 5 else 1))
                    refreshGui(gui, player, shopData)
                }
            )
            gui.setSlot(15, GuiElementBuilder(Items.BARREL).setName(Component.literal("Batch Size: " + shopData.getStack()).withStyle(ChatFormatting.AQUA)))
            gui.setSlot(16, GuiElementBuilder(Items.STAINED_GLASS_PANE.lime())
                .setName(Component.literal("+ 1 Batch Size").withStyle(ChatFormatting.GREEN).append(Component.literal(" (Right-click: + 5)").withStyle(ChatFormatting.GRAY)))
                .setCallback { type ->
                    shopData.setStack(shopData.getStack() + if (type.isRight) 5 else 1)
                    refreshGui(gui, player, shopData)
                }
            )
            gui.setSlot(22, GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("📜 View Sales Receipts").withStyle(ChatFormatting.GOLD).append(Component.literal("\n\nClick to inspect transaction history!").withStyle(ChatFormatting.GRAY)))
                .setCallback { _ -> openReceiptsGui(player, shopData) }
            )
        }

        private fun openBedrockForm(player: ServerPlayer, shopData: ShopData) {
            val form = CustomForm.builder()
                .title("Shop Settings")
                .input("Price ($)", "Enter new price", String.format("%.2f", shopData.getPrice()))
                .input("Batch Size", "Enter batch size", shopData.getStack().toString())
                .label("💡 To swap the item sold, hold the new item in your main hand before submitting!")
                .validResultHandler { response ->
                    val priceStr = response.next<String>()
                    val stackStr = response.next<String>()
                    try {
                        checkNotNull(priceStr)
                        val price = max(.0, priceStr.toDouble())
                        checkNotNull(stackStr)
                        val stack = max(1, stackStr.toInt())

                        shopData.setPrice(price)
                        shopData.setStack(stack)

                        val heldItem = player.mainHandItem
                        if (!heldItem.isEmpty) shopData.setItemSold(heldItem.copy())
                    } catch (_: NumberFormatException) {}
                }.build()

            FloodgateApi.getInstance().sendForm(player.getUUID(), form)
        }

        fun openReceiptsGui(player: ServerPlayer, shopData: ShopData) {
            if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUUID())) openBedrockReceiptsForm(player, shopData)
            else openJavaReceiptsGui(player, shopData)
        }

        private fun openJavaReceiptsGui(player: ServerPlayer, shopData: ShopData) {
            val gui = SimpleGui(MenuType.GENERIC_9x3, player, false)
            gui.setTitle(Component.literal("Sales History"))

            val filler = GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal(" "))
            for (i in 0..26) gui.setSlot(i, filler)

            val receipts = shopData.receipts
            for (i in 0..<min(receipts.size, 22)) {
                val receipt = receipts[i]
                val diffMs = max(0, System.currentTimeMillis() - receipt.timestamp)
                val mins = diffMs / 60000
                val timeAgo = if (mins < 1) "Just now" else if (mins < 60) mins.toString() + "m ago" else (mins / 60).toString() + "h ago"

                val name: Component = Component.literal("🧾 Sale to ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(receipt.buyerName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal("\n• Bought: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(receipt.stack.toString() + "x ").withStyle(ChatFormatting.AQUA))
                    .append(shopData.getItemSold().hoverName.copy().withStyle(ChatFormatting.AQUA))
                    .append(Component.literal("\n• Earned: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.format("$%.2f", receipt.price)).withStyle(ChatFormatting.GREEN))
                    .append(Component.literal("\n• Time: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(timeAgo).withStyle(ChatFormatting.DARK_GRAY))

                gui.setSlot(i, GuiElementBuilder(Items.PAPER).setName(name))
            }

            gui.setSlot(22, GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("⬅ Back to Settings").withStyle(ChatFormatting.RED))
                .setCallback { _ -> open(player, shopData) }
            )

            gui.open()
        }

        private fun openBedrockReceiptsForm(player: ServerPlayer, shopData: ShopData) {
            val form = SimpleForm.builder().title("Sales History")
            val receipts = shopData.receipts

            if (receipts.isEmpty()) form.content("No sales recorded yet.")
            else {
                val content = StringBuilder()
                for ((_, buyerName, stack, price, timestamp) in receipts) {
                    val mins = max(0, System.currentTimeMillis() - timestamp) / 60000
                    content.append(String.format("• %s bought %dx for $%.2f (%s)\n", buyerName, stack, price, if (mins < 1) "Just now" else if (mins < 60) mins.toString() + "m ago" else (mins / 60).toString() + "h ago"))
                }
                form.content(content.toString())
            }
            form.button("Back to Settings").validResultHandler { _ -> open(player, shopData) }
            FloodgateApi.getInstance().sendForm(player.getUUID(), form.build())
        }
    }

    @JvmRecord data class ShopReceipt(val buyerUuid: UUID, val buyerName: String, val stack: Int, val price: Double, val timestamp: Long) {
        companion object {
            val CODEC: Codec<ShopReceipt> = RecordCodecBuilder.create { instance -> instance.group(UUIDUtil.CODEC.fieldOf("buyer_id").forGetter(ShopReceipt::buyerUuid), Codec.STRING.fieldOf("buyer_name").forGetter(ShopReceipt::buyerName), Codec.INT.fieldOf("stack").forGetter(ShopReceipt::stack), Codec.DOUBLE.fieldOf("price").forGetter(ShopReceipt::price), Codec.LONG.fieldOf("timestamp").forGetter(ShopReceipt::timestamp)).apply(instance, ::ShopReceipt) }
        }
    }

    companion object {
        val CODEC: Codec<ShopData> = RecordCodecBuilder.create { instance -> instance.group(UUIDUtil.CODEC.fieldOf("shop_id").forGetter(ShopData::shopId), UUIDUtil.CODEC.fieldOf("owner_id").forGetter(ShopData::ownerUuid), ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension", Level.OVERWORLD).forGetter(ShopData::dimension), BlockPos.CODEC.fieldOf("barrel_pos").forGetter(ShopData::barrelPos), UUIDUtil.CODEC.fieldOf("interaction_id").forGetter(ShopData::interactionEntityUuid), UUIDUtil.CODEC.fieldOf("item_display_id").forGetter(ShopData::itemDisplayUuid), UUIDUtil.CODEC.fieldOf("text_display_id").forGetter(ShopData::textDisplayUuid), ItemStack.CODEC.fieldOf("item_sold").forGetter(ShopData::getItemSold), Codec.INT.fieldOf("stack").forGetter(ShopData::getStack), Codec.DOUBLE.fieldOf("price").forGetter(ShopData::getPrice), Codec.list(ShopReceipt.CODEC).optionalFieldOf("receipts", mutableListOf()).forGetter(ShopData::receipts), Codec.BOOL.optionalFieldOf("is_creative", false).forGetter(ShopData::isCreative)).apply(instance, ::ShopData) }
    }
}