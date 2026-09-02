package me.marin.lockout.lockout.goal.builder.item;

import com.google.gson.reflect.TypeToken;
import me.marin.lockout.lockout.goal.acceptance.AllInInventoryAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.ItemWithComponentAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ColorCollection;

import java.util.Collections;
import java.util.List;

public class ObtainItemGoalBuilder<T> extends GoalBuilder<Inventory,T> {
    public ObtainItemGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, Inventory, ?> progressSupplier) {
        super("OBTAIN", "Obtain", GoalCategory.OBTAINING_ITEMS, optionSupplier, progressSupplier);
    }

    @Override
    public void reifiedUpdater(Inventory update) {}

    public static ObtainItemGoalBuilder<Void> any(Item... items) {
        return new ObtainItemGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> AllInInventoryAcceptanceCondition.of(InListAcceptanceCondition.item(items)))
        );
    }

    public static ObtainItemGoalBuilder<DyeColor> colored(ColorCollection<Item> item) {
        return new ObtainItemGoalBuilder<>(
                GoalOptionSupplier.list("Color", DyeColor.VALUES, new TypeToken<>() {}, "COLORED", DyeColor::getName),
                GoalProgressSupplier.simple(c -> AllInInventoryAcceptanceCondition.of(InListAcceptanceCondition.item(item.pick(c))))
        );
    }

    public static ObtainItemGoalBuilder<Void> shieldWithBanner() {
        return new ObtainItemGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> AllInInventoryAcceptanceCondition.of(
                        new ItemWithComponentAcceptanceCondition(List.of(new ItemUtil.DataComponentCondition<>(
                                DataComponents.BASE_COLOR,
                                _ -> true,
                                s -> {
                                    DyeColor color = BuilderUtil.getRandomElement(DyeColor.VALUES);
                                    s.set(DataComponents.BASE_COLOR, color);
                                    s.set(DataComponents.BANNER_PATTERNS, ItemUtil.getRandomBannerPattern(BuilderUtil.getRandomElement(DyeColor.VALUES.stream().filter(c -> c != color).toList())));
                                },
                                () -> "SHIELD_WITH_BANNER",
                                () -> "Shield with Banner"
                        )), () -> Collections.nCopies(20, Items.SHIELD.getDefaultInstance()))
                ))
        );
    }
}
