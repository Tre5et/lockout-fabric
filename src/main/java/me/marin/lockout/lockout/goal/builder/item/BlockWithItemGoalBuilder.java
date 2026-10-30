package me.marin.lockout.lockout.goal.builder.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.name.NameProvider;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Optional;

public abstract class BlockWithItemGoalBuilder<T> extends GoalBuilder<ItemUtil.BlockedWithItem, T> {

    public BlockWithItemGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ItemUtil.BlockedWithItem, ?> progressSupplier, String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier, namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public void reifiedUpdater(ItemUtil.BlockedWithItem update) {}

    public static class HaveDisabled extends BlockWithItemGoalBuilder<Void> {
        public static final MapCodec<HaveDisabled> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Block", i)
                .and(RegistryCodecs.holder(Registries.ITEM).fieldOf("item").forGetter(HaveDisabled::getItem))
                .apply(i, HaveDisabled::new));

        @Getter
        private final Holder<Item> item;

        public HaveDisabled(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, Holder<Item> item) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                        @Override
                        public boolean test(ItemUtil.BlockedWithItem value, ServerPlayer player) {
                            return value.item().getItem().equals(item.value()) && value.blockingData() != null && value.secondsToDisable() > 0;
                        }

                        @Override
                        public String getId() {
                            return "DISABLE_" + ItemUtil.getItemId(item.value());
                        }

                        @Override
                        public String getName() {
                            return "with " + ItemUtil.getItemName(item.value()) + " and have it disabled";
                        }

                        @Override
                        public List<TextureExtractor> getExamples() {
                            return List.of(ItemTextureExtractor.item(item.value()).overlay(new FillTextureExtractor(ARGB.white(100)), TextureAnchor.BOTTOM_LEFT, 16, 8));
                        }
                    }),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.item = item;

            if(customNameProvider.isEmpty()) customName(_ -> "Have your " + ItemUtil.getItemName(item.value()) + " disabled");
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }
}
