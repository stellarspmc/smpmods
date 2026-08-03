package fun.spmc.smpmod.economy.atm;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import fun.spmc.smpmod.economy.EconomySavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

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
        EconomySavedData eco = EconomySavedData.get(player.level());

        GuiElementBuilder filler = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal(" "));
        for (int i = 0; i < 27; i++) gui.setSlot(i, filler);
        gui.setSlot(10, new GuiElementBuilder(Items.STAINED_GLASS_PANE.red())
                .setName(Component.literal("- $10.00").withStyle(ChatFormatting.RED)
                        .append(Component.literal(" (Right-click: - $50.00)").withStyle(ChatFormatting.GRAY)))
                .setCallback((type) -> {
                    double amount = type.isRight ? 50.0 : 10.0;
                    eco.changeBalance(player.getUUID(), -amount);
                    refreshGui(gui, player);
                }));
        gui.setSlot(11, new GuiElementBuilder(Items.REDSTONE_BLOCK)
                .setName(Component.literal("- $100.00").withStyle(ChatFormatting.DARK_RED)
                        .append(Component.literal(" (Right-click: Withdraw All)").withStyle(ChatFormatting.GRAY)))
                .setCallback((type) -> {
                    if (type.isRight) {
                        double current = eco.getBalance(player.getUUID());
                        if (current > 0) eco.changeBalance(player.getUUID(), -current);
                    } else {
                        eco.changeBalance(player.getUUID(), -100.0);
                    }
                    refreshGui(gui, player);
                }));
        double balance = eco.getBalance(player.getUUID());
        gui.setSlot(13, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal(String.format("Balance: $%,.2f", balance)).withStyle(ChatFormatting.GOLD)));
        gui.setSlot(15, new GuiElementBuilder(Items.STAINED_GLASS_PANE.lime())
                .setName(Component.literal("+ $10.00").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(" (Right-click: + $50.00)").withStyle(ChatFormatting.GRAY)))
                .setCallback((type) -> {
                    double amount = type.isRight ? 50.0 : 10.0;
                    eco.changeBalance(player.getUUID(), amount);
                    refreshGui(gui, player);
                }));
        gui.setSlot(16, new GuiElementBuilder(Items.EMERALD_BLOCK)
                .setName(Component.literal("+ $100.00").withStyle(ChatFormatting.DARK_GREEN)
                        .append(Component.literal(" (Right-click: + $500.00)").withStyle(ChatFormatting.GRAY)))
                .setCallback((type) -> {
                    double amount = type.isRight ? 500.0 : 100.0;
                    eco.changeBalance(player.getUUID(), amount);
                    refreshGui(gui, player);
                }));
    }

    private static void openBedrockForm(ServerPlayer player) {
        EconomySavedData eco = EconomySavedData.get(player.level());
        double balance = eco.getBalance(player.getUUID());

        SimpleForm form = SimpleForm.builder()
                .title("ATM Machine")
                .content(String.format("Current Balance: $%,.2f", balance))
                .button("Withdraw $10")
                .button("Withdraw $100")
                .button("Deposit $10")
                .button("Deposit $100")
                .validResultHandler(response -> {
                    switch (response.clickedButtonId()) {
                        case 0 -> eco.changeBalance(player.getUUID(), -10.0);
                        case 1 -> eco.changeBalance(player.getUUID(), -100.0);
                        case 2 -> eco.changeBalance(player.getUUID(), 10.0);
                        case 3 -> eco.changeBalance(player.getUUID(), 100.0);
                    }
                    openBedrockForm(player);
                })
                .build();

        FloodgateApi.getInstance().sendForm(player.getUUID(), form);
    }
}