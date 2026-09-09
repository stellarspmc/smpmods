package `fun`.spmc.smpmod.quest

import com.mojang.serialization.Codec
import `fun`.spmc.smpmod.SMPMod
import `fun`.spmc.smpmod.registry.QuestRegistry
import net.minecraft.core.UUIDUtil
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import java.time.LocalDate
import java.util.*
import java.util.function.Function
import java.util.function.Supplier
import kotlin.math.min

class QuestManager @JvmOverloads constructor(questData: MutableMap<UUID, PlayerQuestData> = HashMap<UUID, PlayerQuestData>()) : SavedData() {
    private val playerQuests: MutableMap<UUID, PlayerQuestData> = HashMap<UUID, PlayerQuestData>(questData)

    fun checkAndResetRotations(player: ServerPlayer) {
        val data: PlayerQuestData = getQuests(player)
        val currentDay = LocalDate.now().toEpochDay()
        val currentWeek = currentDay / 7

        if (data.lastDailyResetDay < currentDay) {
            refreshQuestsForCategory(data, player, Quest.QuestCategory.DAILY, QuestRegistry.getAllForDaily())
            data.lastDailyResetDay = currentDay
            setDirty()
        }
        if (data.lastWeeklyResetWeek < currentWeek) {
            refreshQuestsForCategory(data, player, Quest.QuestCategory.WEEKLY, QuestRegistry.getAllForWeekly())
            data.lastWeeklyResetWeek = currentWeek
            setDirty()
        }

        data.activeQuests.stream()
            .filter { a: PlayerQuestData.ActiveQuest -> (a.quest.questType == Quest.QuestCategory.DAILY) or (a.quest.questType == Quest.QuestCategory.WEEKLY) }
            .forEach { a: PlayerQuestData.ActiveQuest -> completeAndClaim(player, a) }
    }

    fun getAvailableNpcQuests(player: ServerPlayer, npcId: String?): List<Quest>? {
        val data: PlayerQuestData = getQuests(player)

        return QuestRegistry.getAllForNpc(npcId).stream().filter { quest: Quest ->
            return@filter !(data.activeQuests.stream().anyMatch { a: PlayerQuestData.ActiveQuest -> a.quest.id == quest.id } || data.completedQuestIds.contains(quest.id)) && quest.preQuestId.map(Function { o: String -> data.completedQuestIds.contains(o) }).orElse(true)!!
        }.toList()
    }

    fun completeAndClaim(player: ServerPlayer, activeQuest: PlayerQuestData.ActiveQuest) {
        val quest: Quest = activeQuest.quest?: return
        if (!activeQuest.isCompleted || activeQuest.isClaimed) return

        val data: PlayerQuestData = getQuests(player)
        quest.questReward.grant(player)
        activeQuest.isClaimed = true
        data.completedQuestIds.add(quest.id)

        if (quest.isNpcQuest) data.activeQuests.remove(activeQuest)
        setDirty()
    }

    private fun refreshQuestsForCategory(data: PlayerQuestData, player: ServerPlayer, category: Quest.QuestCategory, availablePool: MutableList<Quest?>) {
        data.activeQuests.removeIf { q: PlayerQuestData.ActiveQuest -> QuestRegistry.get(q.getQuestId()) != null && QuestRegistry.get(q.getQuestId()).questType == category }
        if (availablePool.isEmpty()) return

        val pool: MutableList<Quest> = ArrayList(availablePool)
        pool.shuffle(Random(player.getRandom().nextLong()))
        for (i in 0..<min(3, pool.size)) data.addQuest(pool[i].id)
    }

    companion object {
        val CODEC: Codec<QuestManager> = Codec.unboundedMap(UUIDUtil.CODEC, PlayerQuestData.CODEC).xmap(Function { questData: MutableMap<UUID, PlayerQuestData> -> QuestManager(questData) }, Function { manager: QuestManager -> manager.playerQuests })
        val TYPE: SavedDataType<QuestManager> = SavedDataType<QuestManager>(Identifier.fromNamespaceAndPath("smpmod", "questing"), { QuestManager() }, CODEC, DataFixTypes.LEVEL)

        @JvmStatic fun get(): QuestManager { return SMPMod.minecraftServer!!.overworld().dataStorage.computeIfAbsent<QuestManager>(TYPE) }
        @JvmStatic fun getQuests(player: ServerPlayer): PlayerQuestData { return get().playerQuests.computeIfAbsent(player.getUUID()) { `_`: UUID? -> PlayerQuestData() } }
    }
}
