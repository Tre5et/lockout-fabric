package me.marin.lockout.lockout.goal.builder.item;

import com.google.gson.reflect.TypeToken;
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.ItemWithComponentAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.TextTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.lockout.goal.requirements.GoalRequirements;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ColorCollection;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ObtainItemGoalBuilder<T> extends GoalBuilder<ServerPlayer,T> {
    public ObtainItemGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, Inventory, ?> progressSupplier) {
        super("OBTAIN", "Obtain", GoalCategory.OBTAINING_ITEMS, optionSupplier, progressSupplier.map(ServerPlayer::getInventory));
    }

    @Override
    public void reifiedUpdater(ServerPlayer update) {}

    public static ObtainItemGoalBuilder<Void> all(Item... items) {
        return new ObtainItemGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.<Void,ItemStack>all(_ -> Arrays.stream(items).map(InListAcceptanceCondition::item).collect(Collectors.toUnmodifiableList())).map(ItemUtil::collectStacks)
        );
    }

    public static ObtainItemGoalBuilder<Void> any(Item... items) {
        return new ObtainItemGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.<Void,ItemStack>any(_ -> List.of(InListAcceptanceCondition.item(items))).map(ItemUtil::collectStacks)
        );
    }

    public static ObtainItemGoalBuilder<Integer> atLeast(int min, int max, Item... items) {
        return new ObtainItemGoalBuilder<>(
                GoalOptionSupplier.integer("Items to obtain", min, max, 1),
                GoalProgressSupplier.atLeast(_ -> Arrays.stream(items).map(InListAcceptanceCondition::item).collect(Collectors.toUnmodifiableList())).map(ItemUtil::collectStacks)
        );
    }

    @SafeVarargs
    public static ObtainItemGoalBuilder<Void> allWithCount(Pair<Item, Integer>... items) {
        return new ObtainItemGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.<Void,Pair<Item,Integer>>all(_ -> Arrays.stream(items).map(InListAcceptanceCondition::itemWithCount).collect(Collectors.toUnmodifiableList())).map(ItemUtil::collectCounts)
        );
    }

    public static ObtainItemGoalBuilder<Void> withCount(Item item, Integer integer) {
        return allWithCount(new Pair<>(item, integer));
    }

    public static ObtainItemGoalBuilder<Void> anyFullStack() {
        return new ObtainItemGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.<Void,ItemStack>any(_ -> List.of(new AcceptanceCondition<>() {
                    @Override
                    public boolean test(ItemStack value) {
                        return value.isStackable() && value.count() == value.getMaxStackSize();
                    }

                    @Override
                    public String getId() {
                        return "FULL_STACK";
                    }

                    @Override
                    public String getName() {
                        return "a Stack of any Item";
                    }

                    @Override
                    public List<TextureExtractor> getExamples() {
                        return List.of(TextTextureExtractor.text("64"));
                    }
                })).map(ItemUtil::collectStacks)
        );
    }

    public static ObtainItemGoalBuilder<DyeColor> colored(ColorCollection<Item> item, Integer count, String id) {
        ObtainItemGoalBuilder<DyeColor> builder = new ObtainItemGoalBuilder<>(
                GoalOptionSupplier.list("Color", DyeColor.VALUES, new TypeToken<>() {}, "COLORED", DyeColor::getName),
                GoalProgressSupplier.<DyeColor,Pair<Item,Integer>>any(c -> List.of(InListAcceptanceCondition.itemWithCount(new Pair<>(c == null ? null : item.white(), count)))).map(ItemUtil::collectCounts).withStaticId(id)
        );
        builder.require(GoalRequirements.COLORS);
        return builder;
    }

    public static ObtainItemGoalBuilder<Void> shieldWithBanner() {
        return new ObtainItemGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.<Void,ItemStack>any(_ -> List.of(new ItemWithComponentAcceptanceCondition(List.of(new ItemUtil.DataComponentCondition<>(
                                DataComponents.BASE_COLOR,
                                _ -> true,
                                s -> {
                                    DyeColor color = BuilderUtil.getRandomElement(DyeColor.VALUES);
                                    s.set(DataComponents.BASE_COLOR, color);
                                    s.set(DataComponents.BANNER_PATTERNS, ItemUtil.getRandomBannerPattern(BuilderUtil.getRandomElement(DyeColor.VALUES.stream().filter(c -> c != color).toList())));
                                },
                                () -> "SHIELD_WITH_BANNER",
                                () -> "Shield with Banner"
                        )), () -> Collections.nCopies(20, Items.SHIELD.getDefaultInstance())))
                ).map(ItemUtil::collectStacks)
        );
    }
}
