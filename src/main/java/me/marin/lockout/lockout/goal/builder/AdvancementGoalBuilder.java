package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.client.LockoutClient;
import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.SimpleClientGoalProgress;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.SimpleServerGoalProgress;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdvancementGoalBuilder extends GoalBuilder<AdvancementHolder, Void> {
    private final List<Identifier> advancements;

    public AdvancementGoalBuilder(String id, GoalCategory category, List<Identifier> advancements) {
        super("ADVANCEMENT_" + id, category);
        this.advancements = advancements;
    }

    @Override
    public Component defaultName(Void option) {
        return Component.literal("Obtain " + advancements.stream()
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
    public ServerGoalProgress<AdvancementHolder, ?> getServerGoalProgress(Void option) {
        return new SimpleServerGoalProgress<>(h -> advancements.contains(h.id()));
    }

    @Override
    public ClientGoalProgress<?> getClientGoalProgress(Void option) {
        return new SimpleClientGoalProgress();
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

    public static AdvancementCountingGoalBuilder counting(String id, GoalCategory category, int min, int max) {
        return new AdvancementCountingGoalBuilder(id, category, min, max);
    }
}
