package fun.spmc.smpmod.quest;

import fun.spmc.smpmod.misc.NPCData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.Mannequin;

public class QuestUtils {
    public static Mannequin spawnDaily(ServerLevel level, BlockPos pos) {
        NPCData data = NPCData.get();
        if (!data.hasNpc("daily")) {
            Mannequin mannequin = EntityTypes.MANNEQUIN.create(level, EntitySpawnReason.TRIGGERED);
            if (mannequin == null) return null;
            mannequin.setPos(pos.getX(), pos.getY(), pos.getZ());
            level.addFreshEntity(mannequin);
            mannequin.setProfile(NPCData.createCustomProfile("daily", new int[]{-1913824437,1951356145,-1425084227,-1019769070}, "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2IzZTgwMTkyOTIyOTMyOTNjNmUyYWI3N2VlZGZiZTE1YjQxMjZjNmM2NTI0N2UzNGQ3OTgzNzIyM2FhZjExNSJ9fX0="));
            mannequin.setCustomName(Component.literal("Rewarder").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
            mannequin.setImmovable(true);
            mannequin.setInvulnerable(true);
            mannequin.setHideDescription(true);

            data.registerNpc("daily", mannequin.getUUID());
            return mannequin;
        }
        return data.getMannequin(level, "daily");
    }
}
