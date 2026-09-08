package fun.spmc.smpmod.npc;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NPCManager {
    private static final Map<String, CustomNPC> DEFINITIONS = new HashMap<>();
    public static void register(CustomNPC npc) { DEFINITIONS.put(npc.getId(), npc); }
    public static @Nullable CustomNPC getDefinition(String id) { return DEFINITIONS.get(id); }
    public static boolean isRegistered(String id) { return DEFINITIONS.containsKey(id); }
    public static ArrayList<String> getAllIds() { return new ArrayList<>(DEFINITIONS.keySet()); }

    public static @Nullable Mannequin spawn(String id, ServerLevel level, BlockPos pos) {
        CustomNPC def = DEFINITIONS.get(id);
        if (def == null) return null;

        NPCData data = NPCData.get();
        if (data.hasNpc(id)) return data.getMannequin(level, id);

        Mannequin mannequin = EntityTypes.MANNEQUIN.create(level, EntitySpawnReason.TRIGGERED);
        if (mannequin == null) return null;

        mannequin.setPos(pos.getX(), pos.getY(), pos.getZ());
        if (def.getProfile() != null) mannequin.setProfile(def.getProfile());
        mannequin.setCustomName(def.getDisplayName());
        mannequin.setImmovable(true);
        mannequin.setInvulnerable(true);
        mannequin.setHideDescription(true);

        level.addFreshEntity(mannequin);
        data.registerNpc(id, mannequin.getUUID());

        return mannequin;
    }

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, _) -> {
            if (hand != InteractionHand.MAIN_HAND || world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof Mannequin mannequin) {
                String npcId = NPCData.get().getNpcId(mannequin.getUUID());
                if (npcId != null) {
                    CustomNPC def = DEFINITIONS.get(npcId);
                    if (def != null) {
                        def.getOnAttack().accept((ServerPlayer) player, mannequin);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, _, entity, _) -> {
            if (world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof Mannequin mannequin) {
                String npcId = NPCData.get().getNpcId(mannequin.getUUID());
                if (npcId != null) {
                    CustomNPC def = DEFINITIONS.get(npcId);
                    if (def != null) {
                        def.getOnUse().accept((ServerPlayer) player, mannequin);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerLevel level = server.overworld();
            if (server.getTickCount() % 5 == 0) {
                NPCData npcData = NPCData.get();
                for (UUID uuid : npcData.getNpcMap().values()) {
                    CustomNPC def = DEFINITIONS.get(npcData.getNpcId(uuid));
                    Entity entity = level.getEntity(uuid);
                    if (entity instanceof Mannequin mannequin && mannequin.isAlive() && def != null && def.lookAtPlayer()) {
                        Player nearestPlayer = level.getNearestPlayer(mannequin, 12.0);
                        if (nearestPlayer != null) mannequin.lookAt(EntityAnchorArgument.Anchor.EYES, nearestPlayer.getEyePosition());
                    }
                }
            }
        });
    }
}