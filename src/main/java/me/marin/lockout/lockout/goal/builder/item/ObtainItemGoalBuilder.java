package me.marin.lockout.lockout.goal.builder.item;

import com.google.gson.reflect.TypeToken;
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.AnyAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.ItemWithComponentAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import me.marin.lockout.lockout.goal.requirements.GoalRequirements;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.level.block.ColorCollection;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObtainItemGoalBuilder<T> extends GoalBuilder<BuilderUtil.Tick,T> {
    public ObtainItemGoalBuilder(String idPrefix, String namePrefix, GoalCategory category, GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, Inventory, ?> progressSupplier) {
        super(idPrefix, namePrefix, category, optionSupplier, GoalProgressSupplier.player(progressSupplier.map(ServerPlayer::getInventory)));
    }

    public ObtainItemGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, Inventory, ?> progressSupplier) {
        this("OBTAIN", "Obtain", GoalCategory.OBTAINING_ITEMS, optionSupplier, progressSupplier);
    }

    @Override
    public void reifiedUpdater(BuilderUtil.Tick update) {}

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
                    public boolean test(ItemStack value, ServerPlayer player) {
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
                GoalProgressSupplier.<DyeColor,Pair<Item,Integer>>any(c -> List.of(InListAcceptanceCondition.itemWithCount(new Pair<>(c == null ? null : item.pick(c), count)))).map(ItemUtil::collectCounts).withStaticId(id)
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

    public static GoalBuilder<BuilderUtil.Tick, Void> allDistinct() {
        return new ObtainItemGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.distinct(_ -> List.of(new AnyAcceptanceCondition<>(
                        "ITMES",
                        () -> "items in Inventory",
                        () -> BuiltInRegistries.ITEM.stream().map(ItemTextureExtractor::item).collect(Collectors.toUnmodifiableList())
                )), ItemStack::getItem).creationValue(36).map(ItemUtil::collectStacks)
        ).customName(_ -> "Fill Inventory with unique Items");
    }

    public static ObtainItemGoalBuilder<Void> armorPiece(Item... items) {
        return new ObtainItemGoalBuilder<>("WEAR", "Wear", GoalCategory.ARMOR,
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.<Void,ItemStack>any(_ -> List.of(InListAcceptanceCondition.item(items))).map(ItemUtil::collectArmorPieces)
        );
    }

    public static GoalBuilder<BuilderUtil.Tick,DyeColor> dyedArmorPiece(Item item) {
        return new ObtainItemGoalBuilder<>("WEAR", "Wear", GoalCategory.ARMOR,
                GoalOptionSupplier.list("Color", DyeColor.VALUES, new TypeToken<>() {}, "COLORED", DyeColor::getName),
                GoalProgressSupplier.<DyeColor,ItemStack>any(c -> List.of(new ItemWithComponentAcceptanceCondition(
                        List.of(new ItemUtil.DataComponentCondition<>(
                                DataComponents.DYED_COLOR, v -> v.rgb() == (c.getTextureDiffuseColor() & 0xFFFFFF),
                                i -> i.set(DataComponents.DYED_COLOR, new DyedItemColor(c.getTextureDiffuseColor() & 0xFFFFFF)),
                                () -> (c == null ? "COLORED" : "COLORED_" + c.getName().toUpperCase()) + "_" + ItemUtil.getItemId(item), () -> BuilderUtil.idToName(c.getName()) + " " + ItemUtil.getItemName(item)
                        )),
                        () -> List.of(item.getDefaultInstance())
                ).and(InListAcceptanceCondition.item(item)))).map(ItemUtil::collectArmorPieces)
        ).customTextureExtractor(c -> {
            ItemStack stack = item.getDefaultInstance();
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(c.getTextureDiffuseColor() & 0xFFFFFF));
            return new ItemTextureExtractor(stack);
        }).require(GoalRequirements.COLORS);
    }

    public static ObtainItemGoalBuilder<Void> allArmorOfMaterial(ArmorMaterial material) {
        return new ObtainItemGoalBuilder<>("WEAR", "Wear", GoalCategory.ARMOR,
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.<Void,ItemStack>all(_ -> ItemUtil.ARMORS.getOrDefault(material, List.of()).stream().map(InListAcceptanceCondition::item).collect(Collectors.toUnmodifiableList())).map(ItemUtil::collectArmorPieces)
        );
    }

    public static GoalBuilder<BuilderUtil.Tick,Void> allEnchantedArmor() {
        return new ObtainItemGoalBuilder<>("WEAR", "Wear", GoalCategory.ARMOR,
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.countMatching(_ -> List.of(new ItemWithComponentAcceptanceCondition(List.of(new ItemUtil.DataComponentCondition<>(
                        DataComponents.ENCHANTMENTS,
                        v -> !v.isEmpty(),
                        s -> s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true),
                        () -> "FULL_ENCHANTED_ARMOR",
                        () -> "full Enchanted Armor"
                )), () -> ItemUtil.ARMOR_PIECE.stream().map(Item::getDefaultInstance).toList()))
                ).creationValue(4).map(ItemUtil::collectArmorPieces)
        );
    }

    public static GoalBuilder<BuilderUtil.Tick,Void> allDifferentArmorMaterial() {
        return new ObtainItemGoalBuilder<>("WEAR", "Wear", GoalCategory.ARMOR,
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.distinct(_ -> List.of(InListAcceptanceCondition.item(ItemUtil.ARMOR_PIECE.toArray(Item[]::new))), v -> ItemUtil.getArmorMaterial(v.getItem()).orElse(null))
                        .creationValue(4).map(ItemUtil::collectArmorPieces)
        ).customName(_ -> "Wear full Armor of different Materials")
                .customTextureExtractor(_ -> new CycleTextureExtractor(Stream.generate(() -> BuilderUtil.getDistinctRandomElements(
                                ItemUtil.FULL_ARMOR_MATERIALS,
                                4
                        ))
                        .limit(10)
                        .map(a -> TextureExtractor.BLANK
                                .overlay(ItemTextureExtractor.item(ItemUtil.ARMORS.get(a.getFirst()).getFirst()), TextureAnchor.TOP_LEFT)
                                .overlay(ItemTextureExtractor.item(ItemUtil.ARMORS.get(a.get(1)).get(1)), TextureAnchor.TOP_RIGHT)
                                .overlay(ItemTextureExtractor.item(ItemUtil.ARMORS.get(a.get(2)).get(2)), TextureAnchor.BOTTOM_LEFT)
                                .overlay(ItemTextureExtractor.item(ItemUtil.ARMORS.get(a.get(3)).get(3)), TextureAnchor.BOTTOM_RIGHT))
                        .toList())
                );
    }

    public static GoalBuilder<BuilderUtil.Tick,Void> allDifferentDyedLeatherArmor() {
        return new ObtainItemGoalBuilder<>("WEAR", "Wear", GoalCategory.ARMOR,
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.distinct(_ -> List.of(InListAcceptanceCondition.item(ItemUtil.ARMORS.get(ArmorMaterials.LEATHER).toArray(Item[]::new))), v -> {
                        if(!v.has(DataComponents.DYED_COLOR)) return null;
                        return v.get(DataComponents.DYED_COLOR).rgb();
                }).creationValue(4).map(ItemUtil::collectArmorPieces)
        ).customName(_ -> "Wear full Leather Armor in different Colors")
                .customTextureExtractor(_ -> new CycleTextureExtractor(Stream.generate(() -> BuilderUtil.getDistinctRandomElements(DyeColor.VALUES, 4)
                                        .stream().<Function<Item,ItemStack>>map(c -> i -> {
                                            ItemStack stack = i.getDefaultInstance();
                                            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(c.getTextureDiffuseColor() & 0xFFFFFF));
                                            return stack;
                                        }).toList()
                        )
                        .limit(10)
                        .map(a -> TextureExtractor.BLANK
                                .overlay(new ItemTextureExtractor(a.getFirst().apply(Items.LEATHER_HELMET)), TextureAnchor.TOP_LEFT)
                                .overlay(new ItemTextureExtractor(a.get(1).apply(Items.LEATHER_CHESTPLATE)), TextureAnchor.TOP_RIGHT)
                                .overlay(new ItemTextureExtractor(a.get(2).apply(Items.LEATHER_LEGGINGS)), TextureAnchor.BOTTOM_LEFT)
                                .overlay(new ItemTextureExtractor(a.get(3).apply(Items.LEATHER_BOOTS)), TextureAnchor.BOTTOM_RIGHT))
                        .toList()
                ));
    }
}