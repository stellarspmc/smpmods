package spmc.smpmod.quest

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.StringRepresentable
import net.minecraft.world.item.ItemStack
import spmc.smpmod.economy.EconomyData.Companion.get
import java.util.*

@JvmRecord
data class Quest(val id: String, val title: String, val description: String, @JvmField val type: QuestType, @JvmField val target: Identifier, val requiredCount: Int, val questType: QuestCategory, val questReward: QuestReward, val npcId: Optional<String>, val preQuestId: Optional<String>) {
	val isNpcQuest: Boolean get() = this.questType == QuestCategory.NPC

	enum class QuestCategory: StringRepresentable {
		DAILY, WEEKLY, NPC;

		override fun getSerializedName() = this.name
		companion object {
			val CODEC: Codec<QuestCategory> = StringRepresentable.fromEnum { entries.toTypedArray() }
		}
	}

	enum class QuestType: StringRepresentable {
		MINE_BLOCK, KILL_MOB, GATHER_ITEM,
		// SLAYER, TODO: think of implementation
		//DELIVER_ITEM, TODO: impl (check cooking update)
		CRAFTING, FISHING, TRADE_MARKET; // TODO: impl (TRADE_MARKET)

		override fun getSerializedName() = this.name
		companion object {
			val CODEC: Codec<QuestType> = StringRepresentable.fromEnum { QuestType.entries.toTypedArray() }
		}
	}

	@JvmRecord
	data class QuestReward(val money: Double, val experience: Int, val items: MutableList<ItemStack>) {
		fun grant(player: ServerPlayer) {
			if (money > 0) get()?.changeBalance(player.getUUID(), money)
			if (experience > 0) player.giveExperiencePoints(experience)
			for (item in items) if (!player.inventory.add(item.copy())) player.drop(item.copy(), false)
		}

		companion object {
			val CODEC: Codec<QuestReward> = RecordCodecBuilder.create { it.group(Codec.DOUBLE.optionalFieldOf("money", 0.0).forGetter(QuestReward::money), Codec.INT.optionalFieldOf("experience", 0).forGetter(QuestReward::experience), ItemStack.CODEC.listOf().optionalFieldOf("items", mutableListOf<ItemStack>()).forGetter(QuestReward::items)).apply(it, ::QuestReward) }
		}
	}

	companion object {
		val CODEC: Codec<Quest> = RecordCodecBuilder.create { it.group(Codec.STRING.fieldOf("id").forGetter(Quest::id), Codec.STRING.fieldOf("title").forGetter(Quest::title), Codec.STRING.fieldOf("description").forGetter(Quest::description), QuestType.CODEC.fieldOf("type").forGetter(Quest::type), Identifier.CODEC.fieldOf("target").forGetter(Quest::target), Codec.INT.fieldOf("required_count").forGetter(Quest::requiredCount), QuestCategory.CODEC.fieldOf("quest_type").forGetter(Quest::questType), QuestReward.CODEC.fieldOf("reward").forGetter(Quest::questReward), Codec.STRING.optionalFieldOf("npc_id").forGetter(Quest::npcId), Codec.STRING.optionalFieldOf("prerequisite_quest_id").forGetter(Quest::preQuestId)).apply(it, ::Quest) }
	}
}