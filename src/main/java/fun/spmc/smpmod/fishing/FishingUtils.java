package fun.spmc.smpmod.fishing;

import fun.spmc.smpmod.economy.EconomyData;
import fun.spmc.smpmod.fishing.fish.FishItem;
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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.item.ItemStack;

public class FishingUtils {
     public static Mannequin spawnFishSeller(ServerLevel level, BlockPos pos) {
        NPCData data = NPCData.get();
        if (!data.hasNpc("fish_seller")) {
            Mannequin mannequin = EntityTypes.MANNEQUIN.create(level, EntitySpawnReason.TRIGGERED);
            if (mannequin == null) return null;
            mannequin.setPos(pos.getX(), pos.getY(), pos.getZ());
            level.addFreshEntity(mannequin);
            mannequin.setProfile(NPCData.createCustomProfile("fisher", new int[]{-1116145262, -304197271, -1414701672, -926620516}, "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM1MWQ3OGNlNDg5MzliYTg5YjllOTFlODk2MjQ2Mjc4NjEwOGUxNTczNzViOWY0MDg2ZjVjNjdkZGE2YzAyOSJ9fX0="));
            mannequin.setCustomName(Component.literal("Fisher").withStyle(ChatFormatting.AQUA));
            mannequin.setImmovable(true);
            mannequin.setInvulnerable(true);
            mannequin.setHideDescription(true);

            data.registerNpc("fish_seller", mannequin.getUUID());
            return mannequin;
        }
        return data.getMannequin(level, "fish_seller");
    }

     public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, _) -> {
            if (hand != InteractionHand.MAIN_HAND || world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof Mannequin mannequin) {
                if (mannequin.getUUID().equals(NPCData.get().getUuid("fish_seller"))) {
                    NPCData.talkAsMannequin(mannequin, Component.literal("Ahoy! Let me take a peek at your haul and see if you brought anything worth buying..."), (ServerPlayer) player);
                    double totalPayout = 0;
                    int totalFish = 0;
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (stack.isEmpty()) continue;
                        if (stack.getItem() instanceof FishItem) {
                            double payout = FishItem.getModifiedPrice(stack);
                            if (payout > 0) {
                                totalPayout += payout;
                                totalFish++;
                                player.getInventory().removeItem(i, stack.getCount());
                            }
                        }

                    }
                    if (totalPayout == 0) NPCData.talkAsMannequin(mannequin, Component.literal("Bah! Not a single fish in your pockets... Go cast a line and come back when you've got something with scales!"), (ServerPlayer) player);
                    else {
                        NPCData.talkAsMannequin(mannequin, Component.literal(String.format("Fine catch! I'll take those %d fish off your hands for $%.2f. Smooth sailing!", totalFish, totalPayout)), (ServerPlayer) player);
                        EconomyData.get().changeBalance(player.getUUID(), totalPayout);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, _, entity, _) -> {
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
}
