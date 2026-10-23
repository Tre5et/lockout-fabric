package me.marin.lockout.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

public class TeamListSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        Collection<String> teamNames = context.getSource().getLevel().getScoreboard().getTeamNames();
        String[] existingTeams = builder.getRemaining().split(" ");
        return SharedSuggestionProvider.suggest(
                teamNames
                        .stream()
                        .filter(name -> Arrays.stream(existingTeams).noneMatch(p -> p.equalsIgnoreCase(name))),
                builder.createOffset(builder.getStart() + builder.getRemaining().lastIndexOf(' ') + 1)
        );
    }
}
