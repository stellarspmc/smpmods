package spmc.smpmod.npc

import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.decoration.Mannequin

@Suppress("UsePropertyAccessSyntax")
object NPCManager {
    private val DEFINITIONS: MutableMap<String, CustomNPC> = HashMap()

    @JvmStatic fun register(npc: CustomNPC) { DEFINITIONS[npc.id] = npc }
    fun getDefinition(id: String): CustomNPC? = DEFINITIONS[id]
    fun isRegistered(id: String): Boolean = DEFINITIONS.containsKey(id)

    @JvmStatic val allIds: ArrayList<String> get() = java.util.ArrayList(DEFINITIONS.keys)

    @JvmStatic
    fun spawn(id: String, level: ServerLevel, pos: BlockPos): Mannequin? {
        val def = DEFINITIONS[id] ?: return null

        val data = NPCData.get() ?: return null
        if (data.hasNpc(id)) return data.getMannequin(level, id)

        val mannequin = EntityTypes.MANNEQUIN.create(level, EntitySpawnReason.TRIGGERED) ?: return null
        mannequin.setPos(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
	    if (def.profile != null) mannequin.profile = def.profile
        mannequin.customName = def.displayName
        mannequin.setImmovable(true)
        mannequin.isInvulnerable = true
        mannequin.setHideDescription(true)

        level.addFreshEntity(mannequin)
	    data.registerNpc(id, mannequin.getUUID())
        return mannequin
    }

    @JvmStatic
    fun register() {
        AttackEntityCallback.EVENT.register { player, world, hand, entity, _ ->
	        if (hand != InteractionHand.MAIN_HAND || world.isClientSide) return@register InteractionResult.PASS
	        if (entity is Mannequin) {
		        (DEFINITIONS[NPCData.get()?.getNpcId(entity.getUUID()) ?: return@register InteractionResult.PASS] ?: return@register InteractionResult.PASS).onAttack.accept(player as ServerPlayer, entity)
		        return@register InteractionResult.SUCCESS
	        }
	        return@register InteractionResult.PASS
        }

	    UseEntityCallback.EVENT.register { player, world, hand, entity, _ ->
		    if (world.isClientSide || hand != InteractionHand.MAIN_HAND) return@register InteractionResult.PASS
		    if (entity is Mannequin) {
			    (DEFINITIONS[NPCData.get()?.getNpcId(entity.getUUID()) ?: return@register InteractionResult.PASS] ?: return@register InteractionResult.PASS).onUse.accept(player as ServerPlayer, entity)
			    return@register InteractionResult.SUCCESS
		    }
		    return@register InteractionResult.PASS
	    }
    }

    @JvmStatic
    fun serverTickLoop(server: MinecraftServer) {
        val npcData = NPCData.get()?: return
        for (uuid in npcData.npcMap.values) {
            val def = DEFINITIONS[npcData.getNpcId(uuid)]
            val entity = server.overworld().getEntity(uuid) // TODO: account of different dimensions
            if (entity is Mannequin && entity.isAlive && def != null && def.lookAtPlayer()) {
                val nearestPlayer = entity.level().getNearestPlayer(entity, 12.0) ?: return
                entity.lookAt(EntityAnchorArgument.Anchor.EYES, nearestPlayer.eyePosition)
            }
        }
    }
}