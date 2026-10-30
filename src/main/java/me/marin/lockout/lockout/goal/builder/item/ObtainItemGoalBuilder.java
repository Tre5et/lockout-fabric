package me.marin.lockout.lockout.goal.builder.item;

import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.AnyAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.ItemWithComponentAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.NumericRange;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.id.IdProvider;
import me.marin.lockout.lockout.goal.rendering.name.NameProvider;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import me.marin.lockout.lockout.goal.requirements.GoalRequirements;
import me.marin.lockout.registries.LockoutRegistryKeys;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ObtainItemGoalBuilder<T> extends GoalBuilder<BuilderUtil.Tick,T> {
    public ObtainItemGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, Inventory, ?> progressSupplier, String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, GoalProgressSupplier.player(progressSupplier.map(ServerPlayer::getInventory)), idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public void reifiedUpdater(BuilderUtil.Tick update) {}

    public static class All extends ObtainItemGoalBuilder<Void> {
        public static final MapCodec<All> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("OBTAIN", "Obtain", i)
                .and(RegistryCodecs.holderSet(Registries.ITEM).fieldOf("items").forGetter(All::getItems))
                .apply(i, All::new));

        @Getter
        private final HolderSet<Item> items;

        public All(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Item> items) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.<Void,ItemStack>all(_ -> items.stream().map(Holder::value).map(InListAcceptanceCondition::item).collect(Collectors.toUnmodifiableList())).map(ItemUtil::collectStacks),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.items = items;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class Any extends ObtainItemGoalBuilder<Void> {
        public static final MapCodec<Any> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("OBTAIN", "Obtain", i)
                .and(RegistryCodecs.holderSet(Registries.ITEM).fieldOf("items").forGetter(Any::getItems))
                .apply(i, Any::new));

        @Getter
        private final HolderSet<Item> items;

        public Any(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Item> items) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.<Void,ItemStack>any(_ -> List.of(InListAcceptanceCondition.item(items.stream().map(Holder::value).toList()))).map(ItemUtil::collectStacks),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.items = items;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class AtLeast extends ObtainItemGoalBuilder<Integer> implements NumericRange {
        public static final MapCodec<AtLeast> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("OBTAIN", "Obtain", i)
                .and(RegistryCodecs.holderSet(Registries.ITEM).fieldOf("items").forGetter(AtLeast::getItems))
                .and(GoalBuilder.numericCodec(1, i))
                .apply(i, AtLeast::new));

        @Getter
        private final HolderSet<Item> items;
        @Getter
        private final int min;
        @Getter
        private final int max;
        @Getter
        private final int step;

        public AtLeast(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Item> items, int min, int max, int step) {
            super(GoalOptionSupplier.integer("Items to obtain", min, max, 1),
                    GoalProgressSupplier.atLeast(_ -> items.stream().map(Holder::value).map(InListAcceptanceCondition::item).collect(Collectors.toUnmodifiableList())).map(ItemUtil::collectStacks),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.items = items;
            this.min = min;
            this.max = max;
            this.step = step;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class WithCount extends ObtainItemGoalBuilder<Void> {
        public static final MapCodec<WithCount> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("OBTAIN", "Obtain", i)
                .and(i.group(
                        RegistryCodecs.holderSet(Registries.ITEM).fieldOf("items").forGetter(WithCount::getItems),
                        Codec.INT.fieldOf("count").forGetter(WithCount::getCount)
                )).apply(i, WithCount::new));

        @Getter
        private final HolderSet<Item> items;
        @Getter
        private final int count;

        public WithCount(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Item> items, int count) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.<Void,ItemStack>any(_ -> List.of(InListAcceptanceCondition.item(items.stream().map(Holder::value).toList()))).map(ItemUtil::collectStacks),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.items = items;
            this.count = count;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class AnyFullStack extends ObtainItemGoalBuilder<Void> {
        public static final MapCodec<AnyFullStack> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("OBTAIN", "Obtain", i)
                .apply(i, AnyFullStack::new));

        public AnyFullStack(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
            super(GoalOptionSupplier.NONE,
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
                    })).map(ItemUtil::collectStacks),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class Colored extends ObtainItemGoalBuilder<DyeColor> {
        public static final MapCodec<Colored> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("OBTAIN", "Obtain", i)
                .and(i.group(
                        RegistryCodecs.holder(LockoutRegistryKeys.COLORED_ITEM).fieldOf("item_collection").forGetter(Colored::getCollection),
                        Codec.INT.optionalFieldOf("count", 1).forGetter(Colored::getCount)
                )).apply(i, Colored::new));

        @Getter
        private final Holder<ColorCollection<Item>> collection;
        @Getter
        private final int count;

        public Colored(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, Holder<ColorCollection<Item>> collection, int count) {
            super(GoalOptionSupplier.list("Color", DyeColor.VALUES, new TypeToken<>() {}, "COLORED", DyeColor::getName),
                    GoalProgressSupplier.<DyeColor,Pair<Item,Integer>>any(c -> List.of(InListAcceptanceCondition.itemWithCount(new Pair<>(c == null ? null : collection.value().pick(c), count)))).map(ItemUtil::collectCounts).withStaticId(collection.unwrapKey().map(k -> BuilderUtil.identifierToId(k.identifier())).orElse("")),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.collection = collection;
            this.count = count;

            ArrayList<Holder<GoalRequirement>> req = new ArrayList<>(List.of(Holder.direct(GoalRequirements.COLORS)));
            req.addAll(requirements.stream().toList());
            this.requirements = HolderSet.direct(req);
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class ShieldWithBanner extends ObtainItemGoalBuilder<Void> {
        public static final MapCodec<ShieldWithBanner> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("OBTAIN", "Obtain", i)
                .apply(i, ShieldWithBanner::new));

        public ShieldWithBanner(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
            super(GoalOptionSupplier.NONE,
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
                    ).map(ItemUtil::collectStacks),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class AllDistinct extends ObtainItemGoalBuilder<Void> {
        public static final MapCodec<AllDistinct> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("OBTAIN", "Obtain", i)
                .apply(i, AllDistinct::new));

        public AllDistinct(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.distinct(_ -> List.of(new AnyAcceptanceCondition<>(
                            "ITMES",
                            () -> "items in Inventory",
                            () -> BuiltInRegistries.ITEM.stream().map(ItemTextureExtractor::item).collect(Collectors.toUnmodifiableList())
                    )), ItemStack::getItem).creationValue(36).map(ItemUtil::collectStacks),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);

            if(customNameProvider.isEmpty()) customName(_ -> "Fill Inventory with unique Items");
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class ArmorPiece extends ObtainItemGoalBuilder<Void> {
        public static final MapCodec<ArmorPiece> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("WEAR", "Wear", i)
                .and(RegistryCodecs.holderSet(Registries.ITEM).fieldOf("items").forGetter(ArmorPiece::getItems))
                .apply(i, ArmorPiece::new));

        @Getter
        private final HolderSet<Item> items;

        public ArmorPiece(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Item> items) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.<Void,ItemStack>any(_ -> List.of(InListAcceptanceCondition.item(items.stream().map(Holder::value).toList()))).map(ItemUtil::collectArmorPieces),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.items = items;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class DyedArmorPiece extends ObtainItemGoalBuilder<DyeColor> {
        public static final MapCodec<DyedArmorPiece> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("WEAR", "Wear", i)
                .and(RegistryCodecs.holder(Registries.ITEM).fieldOf("item").forGetter(DyedArmorPiece::getItem))
                .apply(i, DyedArmorPiece::new));

        @Getter
        private final Holder<Item> item;

        public DyedArmorPiece(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, Holder<Item> item) {
            super(GoalOptionSupplier.list("Color", DyeColor.VALUES, new TypeToken<>() {}, "COLORED", DyeColor::getName),
                    GoalProgressSupplier.<DyeColor,ItemStack>any(c -> List.of(new ItemWithComponentAcceptanceCondition(
                            List.of(new ItemUtil.DataComponentCondition<>(
                                    DataComponents.DYED_COLOR, v -> v.rgb() == (c.getTextureDiffuseColor() & 0xFFFFFF),
                                    i -> i.set(DataComponents.DYED_COLOR, new DyedItemColor(c.getTextureDiffuseColor() & 0xFFFFFF)),
                                    () -> (c == null ? "COLORED" : "COLORED_" + c.getName().toUpperCase()) + "_" + ItemUtil.getItemId(item.value()), () -> BuilderUtil.idToName(c.getName()) + " " + ItemUtil.getItemName(item.value())
                            )),
                            () -> List.of(item.value().getDefaultInstance())
                    ).and(InListAcceptanceCondition.item(item.value())))).map(ItemUtil::collectArmorPieces),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.item = item;

            if(customTextureExtractorProvider.isEmpty()) customTextureExtractor(c -> {
                ItemStack stack = item.value().getDefaultInstance();
                if(!(c instanceof DyeColor color)) return new ItemTextureExtractor(stack);
                stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color.getTextureDiffuseColor() & 0xFFFFFF));
                return new ItemTextureExtractor(stack);
            });

            ArrayList<Holder<GoalRequirement>> req = new ArrayList<>(List.of(Holder.direct(GoalRequirements.COLORS)));
            req.addAll(requirements.stream().toList());
            this.requirements = HolderSet.direct(req);
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class FullArmorOfMaterial extends ObtainItemGoalBuilder<Void> {
        public static final MapCodec<FullArmorOfMaterial> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("WEAR", "Wear", i)
                .and(RegistryCodecs.holder(LockoutRegistryKeys.ARMOR_MATERIAL).fieldOf("material").forGetter(FullArmorOfMaterial::getMaterial))
                .apply(i, FullArmorOfMaterial::new));

        @Getter
        Holder<ArmorMaterial> material;

        public FullArmorOfMaterial(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, Holder<ArmorMaterial> material) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.<Void,ItemStack>all(_ -> ItemUtil.ARMORS.getOrDefault(material.value(), List.of()).stream().map(InListAcceptanceCondition::item).collect(Collectors.toUnmodifiableList())).map(ItemUtil::collectArmorPieces),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.material = material;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class AllEnchantedArmor extends ObtainItemGoalBuilder<Void> {
        public static final MapCodec<AllEnchantedArmor> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("WEAR", "Wear", i)
                .apply(i, AllEnchantedArmor::new));

        public AllEnchantedArmor(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.countMatching(_ -> List.of(new ItemWithComponentAcceptanceCondition(List.of(new ItemUtil.DataComponentCondition<>(
                                    DataComponents.ENCHANTMENTS,
                                    v -> !v.isEmpty(),
                                    s -> s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true),
                                    () -> "FULL_ENCHANTED_ARMOR",
                                    () -> "full Enchanted Armor"
                            )), () -> ItemUtil.ARMOR_PIECE.stream().map(Item::getDefaultInstance).toList()))).creationValue(4).map(ItemUtil::collectArmorPieces),
                            idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class AllDifferentArmorMaterial extends ObtainItemGoalBuilder<Void> {
        public static final MapCodec<AllDifferentArmorMaterial> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("WEAR", "Wear", i)
                .apply(i, AllDifferentArmorMaterial::new));

        public AllDifferentArmorMaterial(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.distinct(_ -> List.of(InListAcceptanceCondition.item(ItemUtil.ARMOR_PIECE.toArray(Item[]::new))), v -> ItemUtil.getArmorMaterial(v.getItem()).orElse(null))
                            .creationValue(4).map(ItemUtil::collectArmorPieces),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);

            if(customNameProvider.isEmpty()) customName(_ -> "Wear full Armor of different Materials");

            if(customTextureExtractorProvider.isEmpty()) customTextureExtractor(_ -> new CycleTextureExtractor(Stream.generate(() -> BuilderUtil.getDistinctRandomElements(
                            ItemUtil.FULL_ARMOR_MATERIALS,
                            4
                    ))
                    .limit(10)
                    .map(a -> TextureExtractor.blank()
                            .overlay(ItemTextureExtractor.item(ItemUtil.ARMORS.get(a.getFirst()).getFirst()), TextureAnchor.TOP_LEFT)
                            .overlay(ItemTextureExtractor.item(ItemUtil.ARMORS.get(a.get(1)).get(1)), TextureAnchor.TOP_RIGHT)
                            .overlay(ItemTextureExtractor.item(ItemUtil.ARMORS.get(a.get(2)).get(2)), TextureAnchor.BOTTOM_LEFT)
                            .overlay(ItemTextureExtractor.item(ItemUtil.ARMORS.get(a.get(3)).get(3)), TextureAnchor.BOTTOM_RIGHT))
                    .toList())
            );
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class AllDifferentDyedLeatherArmor extends ObtainItemGoalBuilder<Void> {
        public static final MapCodec<AllDifferentDyedLeatherArmor> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("WEAR", "Wear", i)
                .apply(i, AllDifferentDyedLeatherArmor::new));

        public AllDifferentDyedLeatherArmor(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.distinct(_ -> List.of(InListAcceptanceCondition.item(ItemUtil.ARMORS.get(ArmorMaterials.LEATHER).toArray(Item[]::new))), v -> {
                        if(!v.has(DataComponents.DYED_COLOR)) return null;
                        return v.get(DataComponents.DYED_COLOR).rgb();
                    }).creationValue(4).map(ItemUtil::collectArmorPieces),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);

            if(customNameProvider.isEmpty()) customName(_ -> "Wear full Leather Armor in different Colors");

            if(customTextureExtractorProvider.isEmpty()) customTextureExtractor(_ -> new CycleTextureExtractor(Stream.generate(() -> BuilderUtil.getDistinctRandomElements(DyeColor.VALUES, 4)
                            .stream().<Function<Item,ItemStack>>map(c -> i -> {
                                ItemStack stack = i.getDefaultInstance();
                                stack.set(DataComponents.DYED_COLOR, new DyedItemColor(c.getTextureDiffuseColor() & 0xFFFFFF));
                                return stack;
                            }).toList()
                    )
                    .limit(10)
                    .map(a -> TextureExtractor.blank()
                            .overlay(new ItemTextureExtractor(a.getFirst().apply(Items.LEATHER_HELMET)), TextureAnchor.TOP_LEFT)
                            .overlay(new ItemTextureExtractor(a.get(1).apply(Items.LEATHER_CHESTPLATE)), TextureAnchor.TOP_RIGHT)
                            .overlay(new ItemTextureExtractor(a.get(2).apply(Items.LEATHER_LEGGINGS)), TextureAnchor.BOTTOM_LEFT)
                            .overlay(new ItemTextureExtractor(a.get(3).apply(Items.LEATHER_BOOTS)), TextureAnchor.BOTTOM_RIGHT))
                    .toList()
            ));
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }
}