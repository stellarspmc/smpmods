package spmc.smpmod.economy.shop

import com.mojang.math.Transformation
import com.mojang.serialization.Codec
import spmc.smpmod.utils.MessageUtils.sendError
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.*
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*
import java.util.List
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.StreamSupport
import kotlin.math.roundToInt

class ShopManager : SavedData() {
    private val shopsByInteractionUuid: MutableMap<UUID, ShopData> = HashMap()
    private val shopsByBarrelPos: MutableMap<BlockPos, ShopData> = HashMap()
    private val shopsById: MutableMap<UUID, ShopData> = HashMap()
    private fun registerShop(data: ShopData) {
        shopsById[data.shopId] = data
        shopsByInteractionUuid[data.interactionEntityUuid] = data
        shopsByBarrelPos[data.barrelPos] = data
    }

    companion object {
        val CODEC: Codec<ShopManager> = ShopData.CODEC.listOf().xmap({ shops: MutableList<ShopData> -> val manager = ShopManager(); for (shop in shops) manager.registerShop(shop); manager }, { manager: ShopManager -> List.copyOf(manager.shopsById.values) })
        val TYPE: SavedDataType<ShopManager> = SavedDataType(Identifier.fromNamespaceAndPath("smpmod", "shops"), { ShopManager() }, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE)

        fun getByInteraction(level: ServerLevel, entityUuid: UUID): ShopData? { return get(level).shopsByInteractionUuid[entityUuid] }
        @JvmStatic fun getByPos(level: ServerLevel, pos: BlockPos): ShopData? { return get(level).shopsByBarrelPos[pos] }
        @JvmStatic fun createCreativeShop(pos: BlockPos, price: Double, sellItem: ItemStack, level: ServerLevel) { createShop(null, pos, price, sellItem, level, true) }
        @JvmStatic fun get(level: ServerLevel): ShopManager { return level.dataStorage.computeIfAbsent(TYPE) }
        fun getAllShops(server: MinecraftServer): ArrayList<ShopData> { return StreamSupport.stream(server.allLevels.spliterator(), false).flatMap { a: ServerLevel -> get(a).shopsById.values.stream() }.collect(Collectors.toCollection { ArrayList() }) }

        @JvmOverloads
        fun createShop(owner: ServerPlayer?, pos: BlockPos, price: Double, sellItem: ItemStack, level: ServerLevel, isCreative: Boolean = false) {
            val x = pos.x + .5
            val y = (pos.y + 1).toDouble()
            val z = pos.z + .5
            val itemDisplay = EntityTypes.ITEM_DISPLAY.create(level, EntitySpawnReason.TRIGGERED)
            val textDisplay = EntityTypes.TEXT_DISPLAY.create(level, EntitySpawnReason.TRIGGERED)
            val interaction = EntityTypes.INTERACTION.create(level, EntitySpawnReason.TRIGGERED)
            if (interaction == null || itemDisplay == null || textDisplay == null) {
                interaction?.discard()
                itemDisplay?.discard()
                textDisplay?.discard()
                return
            }

            itemDisplay.setPos(x, y + .35, z)
            itemDisplay.itemStack = sellItem.copy()
            itemDisplay.setTransformation(Transformation(Vector3f(0f), Quaternionf().rotationY(Math.toRadians(-((owner?.yRot ?: (0 / 90f)).roundToInt() * 90f).toDouble()).toFloat()), Vector3f(0.5f), null))
            textDisplay.setPos(x, y + .85, z)
            val stockLabel = if (isCreative) "∞" else "0"
            val label = String.format("§f%dx §e%s\n§a$%.2f\nStock: %s", sellItem.count, sellItem.hoverName.string, price, stockLabel)
            textDisplay.text = Component.literal(label)
            textDisplay.billboardConstraints = Display.BillboardConstraints.CENTER
            interaction.setPos(x, y, z)
            interaction.height = 1f
            interaction.width = 1f
            level.addFreshEntity(itemDisplay)
            level.addFreshEntity(textDisplay)
            level.addFreshEntity(interaction)

            val data = ShopData(UUID.randomUUID(), owner?.getUUID() ?: UUID(0, 0), level.dimension(), pos, interaction.getUUID(), itemDisplay.getUUID(), textDisplay.getUUID(), sellItem.copyWithCount(1), sellItem.count, price, isCreative)
            val manager: ShopManager = get(level)
            manager.registerShop(data)
            manager.setDirty()
        }

        fun removeShop(shop: ShopData?, level: ServerLevel) {
            if (shop == null) return
            shop.destroyShop()

            val manager: ShopManager = get(level)
            manager.shopsByInteractionUuid.remove(shop.interactionEntityUuid)
            manager.shopsByBarrelPos.remove(shop.barrelPos)
            manager.shopsById.remove(shop.shopId)
            manager.setDirty()
        }

        fun register() {
            AttackEntityCallback.EVENT.register(AttackEntityCallback { player: Player, world: Level, hand: InteractionHand, entity: Entity, _: EntityHitResult? ->
                if (hand != InteractionHand.MAIN_HAND || world.isClientSide) return@AttackEntityCallback InteractionResult.PASS
                if (entity is Interaction) {
                    val shop: ShopData = getByInteraction(world as ServerLevel, entity.getUUID())?: return@AttackEntityCallback InteractionResult.PASS
                    if (player.isShiftKeyDown && shop.isOwner(player as ServerPlayer)) shop.openOwnerMenu(player)
                    else shop.processPurchase(player as ServerPlayer)

                    return@AttackEntityCallback InteractionResult.SUCCESS
                }
                return@AttackEntityCallback InteractionResult.PASS
            })

            UseEntityCallback.EVENT.register(UseEntityCallback { player: Player, world: Level, _: InteractionHand, entity: Entity, _: EntityHitResult? ->
                if (world.isClientSide) return@UseEntityCallback InteractionResult.PASS
                if (entity is Interaction) {
                    val shop: ShopData = getByInteraction(world as ServerLevel, entity.getUUID())?: return@UseEntityCallback InteractionResult.PASS

                    if (player.isShiftKeyDown && shop.isOwner(player as ServerPlayer)) removeShop(shop, world)
                    else player.sendSystemMessage(shop.getFormattedInfoComponent())
                    return@UseEntityCallback InteractionResult.SUCCESS
                }
                return@UseEntityCallback InteractionResult.PASS
            })

            UseBlockCallback.EVENT.register(UseBlockCallback { player: Player, world: Level, _: InteractionHand, hitResult: BlockHitResult ->
                if (world.isClientSide) return@UseBlockCallback InteractionResult.PASS
                val pos = hitResult.blockPos
                val shop: ShopData = getByPos(world as ServerLevel, pos)?: return@UseBlockCallback InteractionResult.PASS
                if (!shop.isOwner(player as ServerPlayer)) return@UseBlockCallback sendError(player, "You cannot open someone else's shop barrel!", InteractionResult.FAIL)
                return@UseBlockCallback InteractionResult.PASS
            })

            PlayerBlockBreakEvents.BEFORE.register(PlayerBlockBreakEvents.Before { world: Level, player: Player, pos: BlockPos, _: BlockState, _: BlockEntity? ->
                if (world.isClientSide) return@Before true
                if (getByPos(world as ServerLevel, pos) == null) return@Before true
                return@Before sendError(player as ServerPlayer, "You cannot break a shop!", false)
            })
        }

        fun serverTickLoop(server: MinecraftServer) { getAllShops(server).forEach(Consumer { obj: ShopData -> obj.updateHologram() }) }
    }
}