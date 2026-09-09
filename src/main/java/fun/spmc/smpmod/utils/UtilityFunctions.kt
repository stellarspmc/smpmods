package fun.spmc.smpmod.utils;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class UtilityFunctions {
    public static ServerLevel getLevelOfEntity(UUID uuid) {
        final AtomicReference<ServerLevel> level = new AtomicReference<>();
        minecraftServer.getAllLevels().forEach((a) -> {
            if (a.getEntity(uuid) != null) level.set(a);
        });
        return level.get();
    }

    public static SuggestionProvider<CommandSourceStack> streamToSuggestion(Stream<Item> itemStream) { return (_, builder) -> SharedSuggestionProvider.suggestResource(itemStream.distinct().map(BuiltInRegistries.ITEM::getKey), builder); }
}
