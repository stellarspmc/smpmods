package fun.spmc.smpmod.quest.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fun.spmc.smpmod.economy.EconomyData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public record Quest(String id, String title, String description, QuestType type, Identifier target, int requiredCount,
        QuestCategory questType, QuestReward questReward, Optional<String> npcId, Optional<String> preQuestId) {
    public static final Codec<Quest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(Quest::id),
            Codec.STRING.fieldOf("title").forGetter(Quest::title),
            Codec.STRING.fieldOf("description").forGetter(Quest::description),
            QuestType.CODEC.fieldOf("type").forGetter(Quest::type),
            Identifier.CODEC.fieldOf("target").forGetter(Quest::target),
            Codec.INT.fieldOf("required_count").forGetter(Quest::requiredCount),
            QuestCategory.CODEC.fieldOf("quest_type").forGetter(Quest::questType),
            QuestReward.CODEC.fieldOf("reward").forGetter(Quest::questReward),
            Codec.STRING.optionalFieldOf("npc_id").forGetter(Quest::npcId),
            Codec.STRING.optionalFieldOf("prerequisite_quest_id").forGetter(Quest::preQuestId)
    ).apply(instance, Quest::new));

    public boolean isNpcQuest() {
        return questType() == QuestCategory.NPC;
    }

    public enum QuestCategory implements StringRepresentable {
        DAILY("daily"),
        WEEKLY("weekly"),
        NPC("npc");

        public static final Codec<QuestCategory> CODEC = StringRepresentable.fromEnum(QuestCategory::values);
        private final String name;

        QuestCategory(String name) { this.name = name; }
        @Override public @NonNull String getSerializedName() { return this.name; }
    }

    public enum QuestType implements StringRepresentable {
        MINE_BLOCK("mine_block"),
        KILL_MOB("kill_mob"),
        GATHER_ITEM("gather_item"), // TODO: impl
        //DELIVER_ITEM("deliver_item"),
        CRAFTING("crafting"),
        FISHING("fishing"),
        TRADE_MARKET("trade_market"); // TODO: impl

        public static final Codec<QuestType> CODEC = StringRepresentable.fromEnum(QuestType::values);

        private final String name;

        QuestType(String name) {
            this.name = name;
        }

        @Override public @NonNull String getSerializedName() { return this.name; }
    }

    public record QuestReward(double money, int experience, List<ItemStack> items) {
        public static final Codec<QuestReward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("money", 0d).forGetter(QuestReward::money),
                Codec.INT.optionalFieldOf("experience", 0).forGetter(QuestReward::experience),
                ItemStack.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(QuestReward::items)
        ).apply(instance, QuestReward::new));

        public void grant(ServerPlayer player) {
            if (money > 0) EconomyData.get().changeBalance(player.getUUID(), money);
            if (experience > 0) player.giveExperiencePoints(experience);
            for (ItemStack item : items) if (!player.getInventory().add(item.copy())) player.drop(item.copy(), false);
        }
    }
}