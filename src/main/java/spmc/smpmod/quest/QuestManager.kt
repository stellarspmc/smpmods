package spmc.smpmod.quest

import com.mojang.serialization.Codec
import spmc.smpmod.SMPMod.Companion.minecraftServer
import spmc.smpmod.registry.QuestRegistry
import net.minecraft.core.UUIDUtil
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import java.time.LocalDate
import java.util.*
import java.util.function.Function
import kotlin.math.min

class QuestManager @JvmOverloads constructor(questData: MutableMap<UUID, PlayerQuestData> = HashMap()) : SavedData() {
    private val playerQuests: MutableMap<UUID, PlayerQuestData> = HashMap(questData)

    fun checkAndResetRotations(player: ServerPlayer) {
        val data = getQuests(player)
        val currentDay = LocalDate.now().toEpochDay()
        val currentWeek = currentDay / 7

        if (data.lastDailyResetDay < currentDay) {
            refreshQuestsForCategory(data, player, Quest.QuestCategory.DAILY, QuestRegistry.allForDaily)
            data.lastDailyResetDay = currentDay
            setDirty()
        }
        if (data.lastWeeklyResetWeek < currentWeek) {
            refreshQuestsForCategory(data, player, Quest.QuestCategory.WEEKLY, QuestRegistry.allForWeekly)
            data.lastWeeklyResetWeek = currentWeek
            setDirty()
        }

        data.activeQuests.filter { a -> (QuestRegistry.get(a.questId)?.questType == Quest.QuestCategory.DAILY) or (QuestRegistry.get(a.questId)?.questType == Quest.QuestCategory.WEEKLY) }.forEach { a -> completeAndClaim(player, a) }
    }

    fun getAvailableNpcQuests(player: ServerPlayer, npcId: String): List<Quest?> {
        val data: PlayerQuestData = getQuests(player)
        return QuestRegistry.getAllForNpc(npcId).filter{ quest -> return@filter !(data.activeQuests.any { a -> a.questId == quest?.id } || data.completedQuestIds.contains(quest?.id)) && quest?.preQuestId?.map{ o -> data.completedQuestIds.contains(o) }?.orElse(true)!!}
    }

    fun completeAndClaim(player: ServerPlayer, activeQuest: PlayerQuestData.ActiveQuest) {
        val quest = activeQuest.getQuest()?: return
        if (!activeQuest.isCompleted || activeQuest.isClaimed) return

        val data: PlayerQuestData = getQuests(player)
        quest.questReward.grant(player)
        activeQuest.isClaimed = true
        data.completedQuestIds.add(quest.id)

        if (quest.isNpcQuest) data.activeQuests.remove(activeQuest)
        setDirty()
    }

    private fun refreshQuestsForCategory(data: PlayerQuestData, player: ServerPlayer, category: Quest.QuestCategory, availablePool: List<Quest>) {
        data.activeQuests.removeIf { q -> QuestRegistry.get(q.questId) != null && QuestRegistry.get(q.questId)?.questType == category }
        if (availablePool.isEmpty()) return

        val pool = ArrayList(availablePool)
        pool.shuffle(Random(player.getRandom().nextLong()))
        for (i in 0..<min(3, pool.size)) data.addQuest(pool[i].id)
    }

    companion object {
        val CODEC: Codec<QuestManager> = Codec.unboundedMap(UUIDUtil.CODEC, PlayerQuestData.CODEC).xmap({ questData -> QuestManager(questData) }, { manager -> manager.playerQuests })
        val TYPE = SavedDataType(Identifier.fromNamespaceAndPath("smpmod", "questing"), { QuestManager() }, CODEC, DataFixTypes.LEVEL)

        @JvmStatic fun get(): QuestManager? { return minecraftServer?.overworld()?.dataStorage?.computeIfAbsent(TYPE) }
        @JvmStatic fun getQuests(player: ServerPlayer): PlayerQuestData { return (get()?: return PlayerQuestData()).playerQuests.computeIfAbsent(player.getUUID()) { _ -> PlayerQuestData() } }
    }
}
