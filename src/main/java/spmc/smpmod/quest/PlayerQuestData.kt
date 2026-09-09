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
            val CODEC: Codec<ActiveQuest> = RecordCodecBuilder.create { instance -> instance.group(Codec.STRING.fieldOf("quest").forGetter { obj -> obj.questId }, Codec.INT.fieldOf("current_count").forGetter { obj -> obj.currentCount }, Codec.BOOL.fieldOf("completed").forGetter { obj -> obj.isCompleted }, Codec.BOOL.optionalFieldOf("claimed", false).forGetter { obj -> obj.isClaimed }).apply(instance, ::ActiveQuest)}
        }
    }

    companion object {
        val CODEC: Codec<PlayerQuestData> = RecordCodecBuilder.create { instance -> instance.group(ActiveQuest.CODEC.listOf().fieldOf("active_quests").forGetter { p -> p.activeQuests }, Codec.STRING.listOf().xmap( { coll -> ArrayList(coll).toSet() }, { coll -> ArrayList(coll) }).fieldOf("completed_quests").forGetter { p -> p.completedQuestIds }, Codec.LONG.optionalFieldOf("last_daily_reset", 0L).forGetter { p -> p.lastDailyResetDay }, Codec.LONG.optionalFieldOf("last_weekly_reset", 0L).forGetter { p -> p.lastWeeklyResetWeek }).apply(instance) { active, completed, daily, weekly -> val data = PlayerQuestData(); data.activeQuests.addAll(active); data.completedQuestIds.addAll(completed); data.lastDailyResetDay = daily; data.lastWeeklyResetWeek = weekly; data } }
    }
}