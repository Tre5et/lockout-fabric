package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.SimpleClientGoalProgress;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.group.GoalGroups;
import me.marin.lockout.lockout.goal.rendering.texture.CycleTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.SimpleServerGoalProgress;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BrewPotionGoalBuilder extends GoalBuilder<Holder<Potion>, Void> {
    private final List<Holder<Potion>> acceptablePotions;

    public BrewPotionGoalBuilder(String id, GoalCategory category, List<Holder<Potion>> acceptablePotions) {
        super("BREW_POTION_" + id, category);
        this.acceptablePotions = acceptablePotions;
    }

    @Override
    public @NonNull Component defaultName(Void option) {
        return Component.literal("Brew a Potion of " + acceptablePotions.stream()
                .map(p -> p.value().name())
                .distinct()
                .map(BuilderUtil::idToName)
                .collect(Collectors.joining(" or "))
        );
    }

    @Override
    public @NonNull TextureExtractor defaultTextureExtractor(Void option) {
        return new CycleTextureExtractor(acceptablePotions.stream()
                .map(p -> new ItemTextureExtractor(getPotionItemStack(p)))
                .toList()
        );
    }

    @Override
    public @NonNull ServerGoalProgress<Holder<Potion>, ?> getServerGoalProgress(Void option) {
        return new SimpleServerGoalProgress<>(acceptablePotions::contains);
    }

    @Override
    public @NonNull ClientGoalProgress<?> getClientGoalProgress(Void option) {
        return new SimpleClientGoalProgress();
    }

    @Override
    public void reifiedUpdater(Holder<Potion> update) {}

    public static ItemStack getPotionItemStack(Holder<Potion> potion) {
        ItemStack stack = Items.POTION.getDefaultInstance();
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return stack;
    }

    @SafeVarargs
    public static BrewPotionGoalBuilder any(Holder<Potion>... potions) {
        String id = Arrays.stream(potions)
                .map(p -> p.value().name().toUpperCase())
                .distinct()
                .collect(Collectors.joining("_OR_"));
        BrewPotionGoalBuilder builder = new BrewPotionGoalBuilder(id, GoalCategory.BREWING, Arrays.stream(potions).toList());
        builder.group(GoalGroups.BREW_POTION);
        return builder;
    }
}
