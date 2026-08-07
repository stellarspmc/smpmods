package fun.spmc.smpmod.economy.atm;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import fun.spmc.smpmod.economy.EconomyData;
import fun.spmc.smpmod.utils.MessageUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import static fun.spmc.smpmod.command.EconomyCommands.processItemDeposit;

public class ATMMenu {
    public static void open(ServerPlayer player) {
        if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUUID())) openBedrockForm(player);
        else openJavaGui(player);
    }
    private static void openJavaGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false) {
            @Override
            public void onOpen() {
                refreshGui(this, player);
            }
        };
        gui.setTitle(Component.literal("ATM Menu"));
        refreshGui(gui, player);
        gui.open();
    }

    private static void refreshGui(SimpleGui gui, ServerPlayer player) {
        EconomyData eco = EconomyData.get();

        GuiElementBuilder filler = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal(" "));
        for (int i = 0; i < 27; i++) gui.setSlot(i, filler);
        gui.setSlot(11, new GuiElementBuilder(Items.REDSTONE_BLOCK)
                .setName(Component.literal("Withdraw $100.00").withStyle(ChatFormatting.DARK_RED)
                        .append(Component.literal(" (Right-click: Withdraw All)").withStyle(ChatFormatting.GRAY)))
                .setCallback((type) -> {
                    if (type.isRight) {
                        double current = Math.round(eco.getBalance(player.getUUID()) * 100f) / 100f;
                        if (current > 0 && current % 100 == 0) {
                            eco.changeBalance(player.getUUID(), -current);
                            giveExactItems(player, (int) (current/100));
                        }
                    } else {
                        eco.changeBalance(player.getUUID(), -100.0);
                        giveExactItems(player, 1);
                    }
                    refreshGui(gui, player);
                }));
        double balance = eco.getBalance(player.getUUID());
        gui.setSlot(13, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal(String.format("Balance: $%,.2f", balance)).withStyle(ChatFormatting.GOLD)));
        gui.setSlot(15, new GuiElementBuilder(Items.EMERALD_BLOCK)
                .setName(Component.literal("Deposit All").withStyle(ChatFormatting.GREEN))
                .setCallback((_) -> {
                    eco.changeBalance(player.getUUID(), getDepositItems(player));
                    refreshGui(gui, player);
                }));
    }

    private static void openBedrockForm(ServerPlayer player) {
        EconomyData eco = EconomyData.get();
        double balance = eco.getBalance(player.getUUID());

        SimpleForm form = SimpleForm.builder()
                .title("ATM Machine")
                .content(String.format("Current Balance: $%,.2f", balance))
                .button("Withdraw $100")
                .button("Deposit All")
                .validResultHandler(response -> {
                    switch (response.clickedButtonId()) {
                        case 0 -> {
                            eco.changeBalance(player.getUUID(), -100.0);
                            giveExactItems(player, 1);
                        }
                        case 1 -> eco.changeBalance(player.getUUID(), getDepositItems(player));
                    }
                    openBedrockForm(player);
                })
                .build();

        FloodgateApi.getInstance().sendForm(player.getUUID(), form);
    }

    private static void giveExactItems(ServerPlayer player, int totalCount) {
        int maxStack = Items.DIAMOND.getDefaultMaxStackSize();
        while (totalCount > 0) {
            int stackSize = Math.min(totalCount, maxStack);
            ItemStack stack = new ItemStack(Items.DIAMOND, stackSize);
            if (!player.getInventory().add(stack)) {
                ItemEntity itemEntity = player.drop(stack, false);
                if (itemEntity != null) itemEntity.setNoPickUpDelay();
            }
            totalCount -= stackSize;
        }

        MessageUtils.sendSuccessMessage(player, String.format("Withdrew %dx Diamonds for $%d.", totalCount, totalCount * 100));
    }

    private static double getDepositItems(ServerPlayer player) {
        double totalPayout = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            double payout = processItemDeposit(player, stack);
            if (payout > 0) {
                totalPayout += payout;
                player.getInventory().removeItem(i, stack.getCount());
            }
        }

        if (totalPayout > 0) MessageUtils.sendSuccessMessage(player, String.format("Deposited all valid items for $%.2f to your account.", totalPayout));
        MessageUtils.sendErrorMessage(player, "No valid market currency items found in inventory.");

        return totalPayout;
    }
}