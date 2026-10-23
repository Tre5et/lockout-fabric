package me.marin.lockout.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.client.LockoutClient;
import me.marin.lockout.game.LockoutGame;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LockoutTeamSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        if(!LockoutGame.isActive(LockoutClient.lockout)) return SharedSuggestionProvider.suggest(List.of(), builder.createOffset(builder.getStart()));
        Collection<String> teamNames = LockoutClient.lockout.getTeams().stream().map(LockoutTeam::getDisplayName).toList();
        return SharedSuggestionProvider.suggest(
                teamNames,
                builder.createOffset(builder.getStart())
        );
    }
}
