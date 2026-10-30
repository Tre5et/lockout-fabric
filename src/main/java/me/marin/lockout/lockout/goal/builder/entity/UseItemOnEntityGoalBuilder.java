package me.marin.lockout.lockout.goal.builder.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.NumericRange;
import me.marin.lockout.lockout.goal.builder.item.ItemUtil;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.name.NameProvider;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureAnchor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractorProvider;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class UseItemOnEntityGoalBuilder<T> extends GoalBuilder<EntityUtil.UsedItemOnEntity, T> {

    public UseItemOnEntityGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, EntityUtil.UsedItemOnEntity, ?> progressSupplier, String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier, namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public void reifiedUpdater(EntityUtil.UsedItemOnEntity update) {}

    public static class Exactly extends UseItemOnEntityGoalBuilder<Void> {
        public static final MapCodec<Exactly> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Use", i)
                .and(i.group(
                        RegistryCodecs.holder(Registries.ITEM).fieldOf("item").forGetter(Exactly::getItem),
                        RegistryCodecs.holder(Registries.ENTITY_TYPE).fieldOf("entity").forGetter(Exactly::getEntity)
                )).apply(i, Exactly::new));

        @Getter
        private final Holder<Item> item;
        @Getter
        private final Holder<EntityType<?>> entity;

        public Exactly(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, Holder<Item> item, Holder<EntityType<?>> entity) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                        @Override
                        public boolean test(EntityUtil.UsedItemOnEntity value, ServerPlayer player) {
                            return value.entity().equals(entity.value()) && value.item().getItem().equals(item.value());
                        }

                        @Override
                        public String getId() {
                            return ItemUtil.getItemId(item.value()) + "_ON_" + EntityUtil.getEntityId(entity.value());
                        }

                        @Override
                        public String getName() {
                            return ItemUtil.getItemName(item.value()) + " on " + EntityUtil.getEntityName(entity.value());
                        }

                        @Override
                        public List<TextureExtractor> getExamples() {
                            return List.of(EntityUtil.getEntityTextureExtractor(entity.value()).overlay(ItemTextureExtractor.item(item.value()), TextureAnchor.TOP_RIGHT, 10));
                        }
                    }),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.item = item;
            this.entity = entity;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class NameTag extends UseItemOnEntityGoalBuilder<Void> {
        public static final MapCodec<NameTag> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Use", i)
                .and(i.group(
                        Codec.STRING.fieldOf("name_tag").forGetter(NameTag::getName),
                        RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(NameTag::getEntities)
                )).apply(i, NameTag::new));

        @Getter
        private final String name;
        @Getter
        private final HolderSet<EntityType<?>> entities;

        public NameTag(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, String name, HolderSet<EntityType<?>> entities) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                        private final InListAcceptanceCondition<EntityType<?>, EntityType<?>> condition = InListAcceptanceCondition.entity(entities.stream().map(Holder::value).collect(Collectors.toUnmodifiableList()));

                        @Override
                        public boolean test(EntityUtil.UsedItemOnEntity value, ServerPlayer player) {
                            return value.item().getItem().equals(Items.NAME_TAG) && value.item().has(DataComponents.CUSTOM_NAME)
                                    && value.item().get(DataComponents.CUSTOM_NAME).getString().equals(name)
                                    && condition.test(value.entity(), player);
                        }

                        @Override
                        public String getId() {
                            return "NAMETAG_" + name + "_ON_" + condition.getId();
                        }

                        @Override
                        public String getName() {
                            return "Nametag with name '" + name + "' on " + condition.getName();
                        }

                        @Override
                        public List<TextureExtractor> getExamples() {
                            return condition.getExamples().stream()
                                    .map(c -> c.overlay(ItemTextureExtractor.item(Items.NAME_TAG), TextureAnchor.TOP_RIGHT, 10))
                                    .toList();
                        }
                    }),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.name = name;
            this.entities = entities;

            if(customNameProvider.isEmpty()) this.customName(_ -> "Name " + InListAcceptanceCondition.entity(entities.stream().map(Holder::value).collect(Collectors.toUnmodifiableList())).getName() + " '" + name + "'");
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class Unique extends UseItemOnEntityGoalBuilder<Integer> implements NumericRange {
        public static final MapCodec<Unique> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Use", i)
                .and(RegistryCodecs.holderSet(Registries.ITEM).fieldOf("items").forGetter(Unique::getItems))
                .and(GoalBuilder.numericCodec(1, i))
                .apply(i, Unique::new));

        @Getter
        private final HolderSet<Item> items;
        @Getter
        private final int min;
        @Getter
        private final int max;
        @Getter
        private final int step;

        public Unique(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Item> items, int min, int max, int step) {
            super(GoalOptionSupplier.integer("Entities to use on", min, max, step),
                    GoalProgressSupplier.unique("Entities used on", _ -> InListAcceptanceCondition.item(items.stream().map(Holder::value).toList()).map(EntityUtil.UsedItemOnEntity::item), EntityUtil.UsedItemOnEntity::entity),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
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
}
