package fun.spmc.smpmod.quest;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum QuestType implements StringRepresentable {
    MINE_BLOCK("mine_block"),
    KILL_MOB("kill_mob"),
    GATHER_ITEM("gather_item"),
    //DELIVER_ITEM("deliver_item"),
    CRAFTING("crafting"),
    FISHING("fishing"),
    TRADE_MARKET("trade_market");

    public static final Codec<QuestType> CODEC = StringRepresentable.fromEnum(QuestType::values);

    private final String name;

    QuestType(String name) {
        this.name = name;
    }

    @Override public @NonNull String getSerializedName() { return this.name; }
}
