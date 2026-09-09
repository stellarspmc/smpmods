package spmc.smpmod.quest

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import spmc.smpmod.registry.QuestRegistry
import org.jetbrains.annotations.Unmodifiable
import java.util.function.Function

class PlayerQuestData {
    @JvmField val activeQuests: MutableList<ActiveQuest> = ArrayList()
    val completedQuestIds: MutableSet<String> = HashSet()
    var lastDailyResetDay: Long = 0
    var lastWeeklyResetWeek: Long = 0

    fun addQuest(quest: String) { activeQuests.add(ActiveQuest(quest)) }

    class ActiveQuest @JvmOverloads constructor(val questId: String, @JvmField var currentCount: Int = 0, var isCompleted: Boolean = false, var isClaimed: Boolean = false) {
        fun increment(amount: Int): Boolean {
            if (this.isCompleted) return false

            this.currentCount += amount
            if (this.currentCount >= QuestRegistry.get(this.questId).requiredCount) {
                this.currentCount = QuestRegistry.get(this.questId).requiredCount
                this.isCompleted = true
            }
            return this.isCompleted
        }

        fun getQuest(): Quest? = QuestRegistry.get(this.questId)

        companion object {
            val CODEC: Codec<ActiveQuest> =
                RecordCodecBuilder.create(Function { instance: RecordCodecBuilder.Instance<ActiveQuest> ->
                    instance.group(
                        Codec.STRING.fieldOf("quest").forGetter { obj: ActiveQuest -> obj.questId },
                        Codec.INT.fieldOf("current_count").forGetter { obj: ActiveQuest -> obj.currentCount },
                        Codec.BOOL.fieldOf("completed").forGetter { obj: ActiveQuest -> obj.isCompleted },
                        Codec.BOOL.optionalFieldOf("claimed", false).forGetter { obj: ActiveQuest -> obj.isClaimed }
                    ).apply(instance)
                    { quest: String, currentCount: Int, completed: Boolean, claimed: Boolean -> ActiveQuest(quest, currentCount, completed, claimed) }
                })
        }
    }

    companion object {
        val CODEC: Codec<PlayerQuestData> =
            RecordCodecBuilder.create(Function { instance: RecordCodecBuilder.Instance<PlayerQuestData> ->
                instance.group<List<ActiveQuest>, @Unmodifiable Set<String>, Long, Long>(
                    ActiveQuest.CODEC.listOf().fieldOf("active_quests").forGetter { p: PlayerQuestData -> p.activeQuests },
                    Codec.STRING.listOf().xmap<@Unmodifiable Set<String>>(Function { coll: List<String> -> ArrayList(coll).toSet() }, Function { coll: Set<String> -> ArrayList(coll) }).fieldOf("completed_quests").forGetter { p: PlayerQuestData -> p.completedQuestIds },
                    Codec.LONG.optionalFieldOf("last_daily_reset", 0L).forGetter { p: PlayerQuestData -> p.lastDailyResetDay },
                    Codec.LONG.optionalFieldOf("last_weekly_reset", 0L).forGetter { p: PlayerQuestData -> p.lastWeeklyResetWeek }
                ).apply(instance) { active: List<ActiveQuest>, completed: Set<String>, daily: Long, weekly: Long -> val data = PlayerQuestData()
                    data.activeQuests.addAll(active)
                    data.completedQuestIds.addAll(completed)
                    data.lastDailyResetDay = daily
                    data.lastWeeklyResetWeek = weekly
                    data
                }
            })
    }
}