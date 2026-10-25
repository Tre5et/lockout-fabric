package me.marin.lockout.lockout.goal.difficulty;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.registries.LockoutRegistryKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class GameDifficulty {
    public static final Codec<GameDifficulty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(GameDifficulty::getName),
            DifficultyCheckFunction.CODEC.fieldOf("individual").forGetter(GameDifficulty::getIndividual),
            DifficultyCheckFunction.CODEC.fieldOf("total").forGetter(GameDifficulty::getTotal)
    ).apply(instance, GameDifficulty::new));

    @Getter
    private final String name;
    @Getter
    private final DifficultyCheckFunction individual;
    @Getter
    private final DifficultyCheckFunction total;

    public GameDifficulty(String name, DifficultyCheckFunction individual, DifficultyCheckFunction total) {
        this.name = name;
        this.individual = individual;
        this.total = total;
    }

    public static class ArgumentType implements com.mojang.brigadier.arguments.ArgumentType<Identifier> {

        public static ArgumentType newInstance() {
            return new ArgumentType();
        }

        @Override
        public Identifier parse(StringReader stringReader) throws CommandSyntaxException {
            String s = stringReader.readString();
            if(Minecraft.getInstance().level == null) throw new SimpleCommandExceptionType(Component.literal("The level is not accessible.")).createWithContext(stringReader);
            Optional<Registry<GameDifficulty>> registry = Minecraft.getInstance().level.registryAccess().lookup(LockoutRegistryKeys.DIFFICULTY);
            if(registry.isEmpty()) throw new SimpleCommandExceptionType(Component.literal("Unable to access difficulty registry.")).createWithContext(stringReader);
            Optional<Map.Entry<ResourceKey<GameDifficulty>, GameDifficulty>> difficulty = registry.get().entrySet().stream().filter(d -> Objects.equals(d.getValue().getName(), s)).findAny();
            if(difficulty.isEmpty()) throw new SimpleCommandExceptionType(Component.literal("Unable to access difficulty " + s + ".")).createWithContext(stringReader);
            return difficulty.get().getKey().identifier();
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            if(Minecraft.getInstance().level == null) return SharedSuggestionProvider.suggest(List.of(), builder);
            Optional<Registry<GameDifficulty>> registry = Minecraft.getInstance().level.registryAccess().lookup(LockoutRegistryKeys.DIFFICULTY);
            return registry.map(gameDifficulties -> SharedSuggestionProvider.suggest(gameDifficulties.stream().map(GameDifficulty::getName), builder)).orElseGet(() -> SharedSuggestionProvider.suggest(List.of(), builder));
        }
    }
}
