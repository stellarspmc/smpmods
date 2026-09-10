package spmc.smpmod.npc

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.decoration.Mannequin
import net.minecraft.world.item.component.ResolvableProfile
import spmc.smpmod.npc.NPCData.Companion.createCustomProfile
import java.util.function.BiConsumer

class CustomNPC private constructor(builder: Builder) {
	val id = builder.id
	val displayName: Component
	val profile: ResolvableProfile?
	val onAttack: BiConsumer<ServerPlayer, Mannequin>
	val onUse: BiConsumer<ServerPlayer, Mannequin>
	private val lookAtPlayer: Boolean

	init {
		this.displayName = builder.displayName
		this.profile = builder.profile
		this.onAttack = builder.onAttack
		this.onUse = builder.onUse
		this.lookAtPlayer = builder.lookAtPlayer
	}

	fun lookAtPlayer() = lookAtPlayer

	class Builder(var id: String, lookAtPlayer: Boolean) {
		var displayName: Component
		var profile: ResolvableProfile? = null
		val lookAtPlayer: Boolean
		var onAttack: BiConsumer<ServerPlayer, Mannequin> = BiConsumer { _, _ -> }
		var onUse: BiConsumer<ServerPlayer, Mannequin> = BiConsumer { _, _ -> }

		init {
			this.displayName = Component.literal(id)
			this.lookAtPlayer = lookAtPlayer
		}

		fun displayName(displayName: Component) = apply { this.displayName = displayName }
		fun profile(profile: ResolvableProfile) = apply { this.profile = profile }
		fun skin(name: String, uuidIntArray: IntArray, textureValue: String) = apply { this.profile = createCustomProfile(name, uuidIntArray, textureValue) }
		fun onAttack(onAttack: BiConsumer<ServerPlayer, Mannequin>) = apply { this.onAttack = onAttack }
		fun onUse(onUse: BiConsumer<ServerPlayer, Mannequin>) = apply { this.onUse = onUse }

		fun build() = CustomNPC(this)
	}
}