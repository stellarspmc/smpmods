package `fun`.spmc.smpmod.registry

import `fun`.spmc.smpmod.economy.EconomyData
import `fun`.spmc.smpmod.fishing.FishItem
import `fun`.spmc.smpmod.npc.CustomNPC
import `fun`.spmc.smpmod.npc.NPCData.Companion.talkAsMannequin
import `fun`.spmc.smpmod.npc.NPCManager.register
import `fun`.spmc.smpmod.vault.VaultData
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.SimpleContainer
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.decoration.Mannequin
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack

object NPCRegistry {
    fun init() {
        register(CustomNPC.Builder("fish_seller", true)
            .displayName(Component.literal("Aquamaray").withStyle(ChatFormatting.AQUA))
            .skin("fisher", intArrayOf(-1116145262, -304197271, -1414701672, -926620516), "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM1MWQ3OGNlNDg5MzliYTg5YjllOTFlODk2MjQ2Mjc4NjEwOGUxNTczNzViOWY0MDg2ZjVjNjdkZGE2YzAyOSJ9fX0=")
            .onAttack { player: ServerPlayer, mannequin: Mannequin ->
                talkAsMannequin(mannequin, Component.literal("Ahoy! Drop whatever fish you want to sell into the bin, then close it when you're done."), player)
                val sellContainer = SimpleContainer(54)
                player.openMenu(SimpleMenuProvider({ containerId: Int, playerInventory: Inventory?, `_`: Player? ->
                    object: ChestMenu(MenuType.GENERIC_9x6, containerId, playerInventory!!, sellContainer, 6) {
                        override fun stillValid(player: Player): Boolean { return true }
                        override fun removed(player: Player) {
                            super.removed(player)
                            var totalPayout = .0
                            var totalFishCount = 0
                            for (i in 0..<sellContainer.containerSize) {
                                val stack = sellContainer.getItem(i)
                                if (stack.isEmpty) continue
                                if (stack.item is FishItem) {
                                    val price: Double = FishItem.getModifiedPrice(stack)
                                    totalPayout += price
                                    totalFishCount += stack.count
                                } else player.inventory.placeItemBackInInventory(stack)
                                sellContainer.setItem(i, ItemStack.EMPTY)
                            }
                            if (totalPayout > 0) {
                                EconomyData.get().changeBalance(player.getUUID(), totalPayout)
                                talkAsMannequin(mannequin, Component.literal(String.format("Fine catch! I'll buy those %d fish for $%.2f. Smooth sailing!", totalFishCount, totalPayout)), player as ServerPlayer)
                            } else talkAsMannequin(mannequin, Component.literal("Bah! You didn't leave any fish in the bin... Come back when you've got something with scales!"), player as ServerPlayer)
                        } } }, Component.literal("Fish Merchant - Sell Bin"))) }
            .onUse { player: ServerPlayer, mannequin: Mannequin -> talkAsMannequin(mannequin, Component.literal("I don't have any quests to offer you.. yet."), player) }
            .build()
        )

        register(CustomNPC.Builder("vault_guardian", true)
            .displayName(Component.literal("Vault Guardian").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .skin("vault_guardian", intArrayOf(-1964164316, 1320767568, -2005365226, 1352775866), "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBmNGI2NzRjZmMyZWJmMDEwZWEwYTAwNzM5NzY3YzA4YjNkNmE5N2EwNGVmNmZlM2QxYWY1NTljYzU0YzBjZiJ9fX0=")
            .onAttack { player: ServerPlayer, _: Mannequin -> VaultData.DonateAnvilGui(player).open() }
            .onUse { player: ServerPlayer, _: Mannequin -> VaultData.sendVaultMessage(player) }
            .build()
        )

        register(CustomNPC.Builder("plant_seller", true)
            .displayName(Component.literal("Farmer").withStyle(ChatFormatting.GREEN))
            .skin("farmer", intArrayOf(1278417584, 1283873747, -1487483765, 2101674152), "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTA1MDViMTE2NDg3YjBjNGE0NjkyMjI1ODBlOGZmNzQ1YzJiOGE4ZmZmODI0YmI1NjA0YThjYTc0NjVmOTk5MCJ9fX0=")
            .onAttack { player: ServerPlayer, mannequin: Mannequin -> talkAsMannequin(mannequin, Component.literal("Hi, I am plant person."), player)}
            .onUse { player: ServerPlayer, mannequin: Mannequin -> talkAsMannequin(mannequin, Component.literal("I don't have any quests to offer you."), player) }
            .build()
        )

        register(CustomNPC.Builder("dw_rewarder", true)
            .displayName(Component.literal("Rewarder").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .skin("dw_rewarder", intArrayOf(-1913824437, 1951356145, -1425084227, -1019769070), "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2IzZTgwMTkyOTIyOTMyOTNjNmUyYWI3N2VlZGZiZTE1YjQxMjZjNmM2NTI0N2UzNGQ3OTgzNzIyM2FhZjExNSJ9fX0=")
            .onAttack { player: ServerPlayer, mannequin: Mannequin -> talkAsMannequin(mannequin, Component.literal("stub: say smt"), player) }
            .onUse { player: ServerPlayer, mannequin: Mannequin -> talkAsMannequin(mannequin, Component.literal("stub: do something"), player) } // TODO: write dialogue
            .build()
        )
    }
}
