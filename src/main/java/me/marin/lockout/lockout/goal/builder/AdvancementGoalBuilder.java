package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.client.LockoutClient;
import me.marin.lockout.lockout.goal.AdvancementGoal;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdvancementGoalBuilder extends GoalBuilder<Void> {
    private final List<Identifier> advancements;

    public AdvancementGoalBuilder(String id, GoalCategory category, List<Identifier> advancements) {
        super("ADVANCEMENT_" + id, category);
        this.advancements = advancements;
    }

    @Override
    public NameExtractor defaultNameExtractor(Void option) {
        return NameExtractor.simple(() -> "Obtain " + advancements.stream()
                .map(a -> {
                    Advancement advancement = LockoutClient.allAdvancements.get(a);
                    if(advancement == null) {
                        return "\"" + a.getPath().substring(a.getPath().lastIndexOf('/')+1).toUpperCase() + "\"";
                    }
                    return "\"" + advancement.display().get().getTitle().getString() + "\"";
                })
                .collect(Collectors.joining(" or "))
                + " Advancement"
        );
    }

    @Override
    public TextureExtractor defaultTextureExtractor(Void option) {
        return new CycleTextureExtractor(advancements.stream()
                .map(a -> {
                    Advancement advancement = LockoutClient.allAdvancements.get(a);
                    if(advancement == null) {
                        return ItemTextureExtractor.item(Items.BARRIER);
                    }
                    return new StackingTextureExtractor(List.of(
                        GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/advancements/challenge_frame_unobtained.png")),
                        ItemTextureExtractor.item(advancement.display().get().getIcon().item().value())
                    ), 3);
                }).toList()
        );
    }

    @Override
    public GoalOptionGenerator<Void> optionGenerator() {
        return null;
    }

    @Override
    public Goal build(Void option) {
        return new AdvancementGoal(
                getId(option),
                getNameExtractor(option),
                getTextureExtractor(option),
                getTooltipInfo(option).orElse(null),
                getHints(option),
                new Pair<>(id, null),
                a -> advancements.contains(a.id())
        );
    }

    public static AdvancementGoalBuilder any(String id, GoalCategory category, Identifier... advancements) {
        return new AdvancementGoalBuilder(
                id,
                category,
                Arrays.stream(advancements).toList()
        );
    }

    public static AdvancementGoalBuilder any(GoalCategory category, Identifier... advancements) {
        return any(
                Arrays.stream(advancements)
                        .map(a -> a.getPath().split("/"))
                        .map(p -> p[p.length-1].toUpperCase())
                        .collect(Collectors.joining("_OR_")),
                category,
                advancements
        );
    }
}
