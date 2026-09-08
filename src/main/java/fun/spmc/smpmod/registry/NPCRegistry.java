package fun.spmc.smpmod.registry;

import fun.spmc.smpmod.economy.EconomyData;
import fun.spmc.smpmod.fishing.FishItem;
import fun.spmc.smpmod.npc.CustomNPC;
import fun.spmc.smpmod.npc.NPCData;
import fun.spmc.smpmod.npc.NPCManager;
import fun.spmc.smpmod.vault.VaultData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class NPCRegistry {
    public static void init() {
        NPCManager.register(new CustomNPC.Builder("fish_seller", true)
                .displayName(Component.literal("Aquamaray").withStyle(ChatFormatting.AQUA))
                .skin("fisher", new int[]{-1116145262, -304197271, -1414701672, -926620516}, "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM1MWQ3OGNlNDg5MzliYTg5YjllOTFlODk2MjQ2Mjc4NjEwOGUxNTczNzViOWY0MDg2ZjVjNjdkZGE2YzAyOSJ9fX0=")
                .onAttack((player, mannequin) -> {
                    NPCData.talkAsMannequin(mannequin, Component.literal("Ahoy! Drop whatever fish you want to sell into the bin, then close it when you're done."), player);
                    SimpleContainer sellContainer = new SimpleContainer(54);

                    player.openMenu(new SimpleMenuProvider((containerId, playerInventory, _) -> new ChestMenu(MenuType.GENERIC_9x6, containerId, playerInventory, sellContainer, 6) {
                                public boolean stillValid(@NonNull Player player) { return true; }
                                public void removed(@NonNull Player player) {
                                    super.removed(player);

                                    double totalPayout = 0;
                                    int totalFishCount = 0;
                                    for (int i = 0; i < sellContainer.getContainerSize(); i++) {
                                        ItemStack stack = sellContainer.getItem(i);
                                        if (stack.isEmpty()) continue;

                                        if (stack.getItem() instanceof FishItem) {
                                            double price = FishItem.getModifiedPrice(stack);
                                            totalPayout += price;
                                            totalFishCount += stack.getCount();
                                        } else player.getInventory().placeItemBackInInventory(stack);
                                        sellContainer.setItem(i, ItemStack.EMPTY);
                                    }

                                    if (totalPayout > 0) {
                                        EconomyData.get().changeBalance(player.getUUID(), totalPayout);
                                        NPCData.talkAsMannequin(mannequin, Component.literal(String.format("Fine catch! I'll buy those %d fish for $%.2f. Smooth sailing!", totalFishCount, totalPayout)), (ServerPlayer) player);
                                    } else NPCData.talkAsMannequin(mannequin, Component.literal("Bah! You didn't leave any fish in the bin... Come back when you've got something with scales!"), (ServerPlayer) player);
                                }
                            },
                            Component.literal("Fish Merchant - Sell Bin")
                    ));
                })
                .onUse((player, mannequin) -> NPCData.talkAsMannequin(mannequin, Component.literal("I don't have any quests to offer you.. yet."), player))
                .build());

        NPCManager.register(new CustomNPC.Builder("vault_guardian", true)
                .displayName(Component.literal("Vault Guardian").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .skin("vault_guardian", new int[]{-2090072119,-764915421,-1802191614,-271883842}, "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmZkODNhY2NhOWFmM3JiYWQ3MDVmNzE0MzU1ZDk0MTA3NDEyY2E0ZWJiZDRjZTkzOTE2MGMxYmUxMGNjZDFhMiJ9fX0=")
                .onAttack((player, _) -> new VaultData.DonateAnvilGui(player).open())
                .onUse((player, _) -> VaultData.sendVaultMessage(player))
                .build());

        NPCManager.register(new CustomNPC.Builder("plant_seller", true)
                .displayName(Component.literal("Farmer").withStyle(ChatFormatting.GREEN))
                .skin("farmer", new int[]{1278417584, 1283873747, -1487483765, 2101674152}, "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTA1MDViMTE2NDg3YjBjNGE0NjkyMjI1ODBlOGZmNzQ1YzJiOGE4ZmZmODI0YmI1NjA0YThjYTc0NjVmOTk5MCJ9fX0=")
                .onAttack((player, mannequin) -> NPCData.talkAsMannequin(mannequin, Component.literal("Hi, I am plant person."), player))
                .onUse((player, mannequin) -> NPCData.talkAsMannequin(mannequin, Component.literal("I don't have any quests to offer you."), player))
                .build());


    }
}
