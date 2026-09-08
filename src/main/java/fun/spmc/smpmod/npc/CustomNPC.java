package fun.spmc.smpmod.npc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.function.BiConsumer;

public class CustomNPC {
    private final String id;
    private final Component displayName;
    private final ResolvableProfile profile;
    private final BiConsumer<ServerPlayer, Mannequin> onAttack;
    private final BiConsumer<ServerPlayer, Mannequin> onUse;
    private final boolean lookAtPlayer;

    private CustomNPC(Builder builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.profile = builder.profile;
        this.onAttack = builder.onAttack;
        this.onUse = builder.onUse;
        this.lookAtPlayer = builder.lookAtPlayer;
    }

    public String getId() { return id; }
    public Component getDisplayName() { return displayName; }
    public ResolvableProfile getProfile() { return profile; }
    public BiConsumer<ServerPlayer, Mannequin> getOnAttack() { return onAttack; }
    public BiConsumer<ServerPlayer, Mannequin> getOnUse() { return onUse; }
    public boolean lookAtPlayer() { return lookAtPlayer; }

    public static class Builder {
        private final String id;
        private Component displayName;
        private ResolvableProfile profile;
        private final boolean lookAtPlayer;
        private BiConsumer<ServerPlayer, Mannequin> onAttack = (player, npc) -> {};
        private BiConsumer<ServerPlayer, Mannequin> onUse = (player, npc) -> {};

        public Builder(String id, boolean lookAtPlayer) {
            this.id = id;
            this.displayName = Component.literal(id);
            this.lookAtPlayer = lookAtPlayer;
        }

        public Builder displayName(Component displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder profile(ResolvableProfile profile) {
            this.profile = profile;
            return this;
        }

        public Builder skin(String name, int[] uuidIntArray, String textureValue) {
            this.profile = NPCData.createCustomProfile(name, uuidIntArray, textureValue);
            return this;
        }

        public Builder onAttack(BiConsumer<ServerPlayer, Mannequin> onAttack) {
            this.onAttack = onAttack;
            return this;
        }

        public Builder onUse(BiConsumer<ServerPlayer, Mannequin> onUse) {
            this.onUse = onUse;
            return this;
        }

        public CustomNPC build() { return new CustomNPC(this); }
    }
}