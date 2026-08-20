package fun.spmc.smpmod.plant;

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

public class PlantUtils {
    public static Mannequin spawnPlantSeller(ServerLevel level, BlockPos pos) {
        NPCData data = NPCData.get();
        if (!data.hasNpc("plant_seller")) {
            Mannequin mannequin = EntityTypes.MANNEQUIN.create(level, EntitySpawnReason.TRIGGERED);
            if (mannequin == null) return null;
            mannequin.setPos(pos.getX(), pos.getY(), pos.getZ());
            level.addFreshEntity(mannequin);
            mannequin.setProfile(NPCData.createCustomProfile("farmer", new int[]{1278417584, 1283873747, -1487483765, 2101674152}, "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTA1MDViMTE2NDg3YjBjNGE0NjkyMjI1ODBlOGZmNzQ1YzJiOGE4ZmZmODI0YmI1NjA0YThjYTc0NjVmOTk5MCJ9fX0="));
            mannequin.setCustomName(Component.literal("Farmer").withStyle(ChatFormatting.GREEN));
            mannequin.setImmovable(true);
            mannequin.setInvulnerable(true);
            mannequin.setHideDescription(true);

            data.registerNpc("plant_seller", mannequin.getUUID());
            return mannequin;
        }
        return data.getMannequin(level, "plant_seller");
    }

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, _) -> {
            if (hand != InteractionHand.MAIN_HAND || world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof Mannequin mannequin) {
                if (mannequin.getUUID().equals(NPCData.get().getUuid("plant_seller"))) {
                    NPCData.talkAsMannequin(mannequin, Component.literal("Hi, I am plant person."), (ServerPlayer) player);
                    // TODO: buy seeds here
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, _, entity, _) -> {
            if (world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof Mannequin mannequin) {
                if (mannequin.getUUID().equals(NPCData.get().getUuid("plant_seller"))) {
                    NPCData.talkAsMannequin(mannequin, Component.literal("I don't have any quests to offer you."), (ServerPlayer) player);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });
    }
}
