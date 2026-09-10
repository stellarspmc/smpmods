package spmc.smpmod.quest

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import spmc.smpmod.registry.QuestRegistry

class PlayerQuestData {
    @JvmField val activeQuests: MutableList<ActiveQuest> = ArrayList()
    val completedQuestIds: MutableSet<String> = HashSet()
    var lastDailyResetDay = 0L
    var lastWeeklyResetWeek = 0L

    fun addQuest(quest: String) { activeQuests.add(ActiveQuest(quest)) }

    class ActiveQuest @JvmOverloads constructor(val questId: String, @JvmField var currentCount: Int = 0, var isCompleted: Boolean = false, var isClaimed: Boolean = false) {
        fun increment(amount: Int): Boolean {
            if (this.isCompleted) return false

            this.currentCount += amount
            if (this.currentCount >= QuestRegistry.get(this.questId)?.requiredCount!!) { // TODO: type checks
                this.currentCount = QuestRegistry.get(this.questId)?.requiredCount!!
                this.isCompleted = true
            }
            return this.isCompleted
        }

        fun getQuest(): Quest? = QuestRegistry.get(this.questId)

        companion object {
            val CODEC: Codec<ActiveQuest> = RecordCodecBuilder.create { it.group(Codec.STRING.fieldOf("quest").forGetter(ActiveQuest::questId), Codec.INT.fieldOf("current_count").forGetter(ActiveQuest::currentCount), Codec.BOOL.fieldOf("completed").forGetter(ActiveQuest::isCompleted), Codec.BOOL.optionalFieldOf("claimed", false).forGetter(ActiveQuest::isClaimed)).apply(it, ::ActiveQuest)}
        }
    }

    companion object {
        val CODEC: Codec<PlayerQuestData> = RecordCodecBuilder.create { it.group(ActiveQuest.CODEC.listOf().fieldOf("active_quests").forGetter(PlayerQuestData::activeQuests), Codec.STRING.listOf().xmap({ a -> a.toSet() }, ::ArrayList).fieldOf("completed_quests").forGetter(PlayerQuestData::completedQuestIds), Codec.LONG.optionalFieldOf("last_daily_reset", 0L).forGetter(PlayerQuestData::lastDailyResetDay), Codec.LONG.optionalFieldOf("last_weekly_reset", 0L).forGetter(PlayerQuestData::lastWeeklyResetWeek)).apply(it) { active, completed, daily, weekly -> val data = PlayerQuestData(); data.activeQuests.addAll(active); data.completedQuestIds.addAll(completed); data.lastDailyResetDay = daily; data.lastWeeklyResetWeek = weekly; data }}
    }
}