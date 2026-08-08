package fun.spmc.smpmod.misc;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class NPCData extends SavedData {
    public static final Codec<NPCData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, UUIDUtil.CODEC)
                    .optionalFieldOf("npcs", Map.of())
                    .forGetter(NPCData::getNpcMap)
    ).apply(instance, NPCData::new));

    public static final SavedDataType<NPCData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("smpmod", "npc_data"),
            NPCData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<String, UUID> npcs = new HashMap<>();

    public NPCData() {}
    public NPCData(Map<String, UUID> npcs) { this.npcs.putAll(npcs); }
    public Map<String, UUID> getNpcMap() { return this.npcs; }
    public static NPCData get() { return minecraftServer.overworld().getDataStorage().computeIfAbsent(TYPE); }
    public void removeNpc(String id) { if (this.npcs.remove(id) != null) this.setDirty(); }
    public @Nullable UUID getUuid(String id) { return this.npcs.get(id); }
    public boolean hasNpc(String id) { return this.npcs.containsKey(id); }

    public void registerNpc(String id, UUID uuid) {
        this.npcs.put(id, uuid);
        this.setDirty();
    }

    public @Nullable Mannequin getMannequin(ServerLevel level, String id) {
        UUID uuid = getUuid(id);
        if (uuid == null) return null;

        Entity entity = level.getEntity(uuid);
        if (entity instanceof Mannequin mannequin) return mannequin;
        return null;
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerLevel level = server.overworld();
            if (server.getTickCount() % 5 == 0) {
                NPCData npcData = NPCData.get();
                for (UUID uuid : npcData.getNpcMap().values()) {
                    Entity entity = level.getEntity(uuid);
                    if (entity instanceof Mannequin mannequin && mannequin.isAlive()) {
                        Player nearestPlayer = level.getNearestPlayer(mannequin, 12.0);
                        if (nearestPlayer != null) mannequin.lookAt(EntityAnchorArgument.Anchor.EYES, nearestPlayer.getEyePosition());
                    }
                }
            }
        });
    }

    public static ResolvableProfile createCustomProfile(String name, int[] uuidIntArray, String textureValue) {
       return createCustomProfile(name, UUIDUtil.uuidFromIntArray(uuidIntArray), textureValue);
    }

    public static ResolvableProfile createCustomProfile(String name, UUID uuid, String textureValue) {
        Multimap<String, Property> map = HashMultimap.create();
        map.put("textures", new Property("textures", textureValue));

        PropertyMap properties = new PropertyMap(map);
        GameProfile profile = new GameProfile(uuid, name, properties);

        return ResolvableProfile.createResolved(profile);
    }

    public static void talkAsMannequin(Mannequin mannequin, Component message, ServerPlayer player) {
        Component name = mannequin.getCustomName() != null ? mannequin.getCustomName() : mannequin.getName();
        MutableComponent finalMessage = Component.empty()
                .append(name)
                .append(Component.literal(": ").withStyle(ChatFormatting.WHITE))
                .append(message.copy().withStyle(ChatFormatting.WHITE));
        player.sendSystemMessage(finalMessage);
    }
}
