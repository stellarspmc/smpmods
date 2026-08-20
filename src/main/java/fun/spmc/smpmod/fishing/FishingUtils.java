package fun.spmc.smpmod.fishing;

import fun.spmc.smpmod.economy.EconomyData;
import fun.spmc.smpmod.misc.NPCData;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class FishingUtils {
     public static Mannequin spawnFishSeller(ServerLevel level, BlockPos pos) {
        NPCData data = NPCData.get();
        if (!data.hasNpc("fish_seller")) {
            Mannequin mannequin = EntityTypes.MANNEQUIN.create(level, EntitySpawnReason.TRIGGERED);
            if (mannequin == null) return null;
            mannequin.setPos(pos.getX(), pos.getY(), pos.getZ());
            level.addFreshEntity(mannequin);
            mannequin.setProfile(NPCData.createCustomProfile("fisher", new int[]{-1116145262, -304197271, -1414701672, -926620516}, "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM1MWQ3OGNlNDg5MzliYTg5YjllOTFlODk2MjQ2Mjc4NjEwOGUxNTczNzViOWY0MDg2ZjVjNjdkZGE2YzAyOSJ9fX0="));
            mannequin.setCustomName(Component.literal("Aquamaray").withStyle(ChatFormatting.AQUA));
            mannequin.setImmovable(true);
            mannequin.setInvulnerable(true);
            mannequin.setHideDescription(true);

            data.registerNpc("fish_seller", mannequin.getUUID());
            return mannequin;
        }
        return data.getMannequin(level, "fish_seller");
    }

     public static void register() {
         AttackEntityCallback.EVENT.register((player, world, hand, entity, _) -> {
            if (hand != InteractionHand.MAIN_HAND || world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof Mannequin mannequin) {
                if (mannequin.getUUID().equals(NPCData.get().getUuid("fish_seller"))) {
                    NPCData.talkAsMannequin(mannequin, Component.literal("Ahoy! Drop whatever fish you want to sell into the bin, then close it when you're done."), (ServerPlayer) player);
                    openSellBin((ServerPlayer) player, mannequin);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, _, entity, _) -> {
            if (world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof Mannequin mannequin) {
                if (mannequin.getUUID().equals(NPCData.get().getUuid("fish_seller"))) {
                    NPCData.talkAsMannequin(mannequin, Component.literal("I don't have any quests to offer you."), (ServerPlayer) player);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });
    }

    private static void openSellBin(ServerPlayer player, Mannequin mannequin) {
        SimpleContainer sellContainer = new SimpleContainer(54);

        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, _) -> new ChestMenu(MenuType.GENERIC_9x6, containerId, playerInventory, sellContainer, 6) {
                    @Override public boolean stillValid(@NonNull Player player) { return true; }

                    @Override
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
    }
}
