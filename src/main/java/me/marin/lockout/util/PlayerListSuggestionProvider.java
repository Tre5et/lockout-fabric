package me.marin.lockout.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

public class PlayerListSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        String[] existingPlayers = builder.getRemaining().split(" ");
        return SharedSuggestionProvider.suggest(
                context.getSource().getOnlinePlayerNames()
                        .stream()
                        .filter(name -> Arrays.stream(existingPlayers).noneMatch(p -> p.equalsIgnoreCase(name))),
                builder.createOffset(builder.getStart() + builder.getRemaining().lastIndexOf(' ') + 1)
        );
    }
}
