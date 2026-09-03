package me.marin.lockout.lockout.goal.acceptance;

import me.marin.lockout.client.LockoutClient;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.builder.entity.EntityUtil;
import me.marin.lockout.lockout.goal.builder.item.ItemUtil;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.StackingTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.FallLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class InListAcceptanceCondition<T,E> implements AcceptanceCondition<T> {
    private final List<E> acceptableElements;
    private final Function<E,String> toId;
    private final Function<E,String> toName;
    private final Function<E,TextureExtractor> toTextureExtractor;
    private final BiPredicate<T,E> equalsFunction;

    public InListAcceptanceCondition(List<E> acceptableElements, Function<E, String> toId, Function<E, String> toName, Function<E, TextureExtractor> toTextureExtractor, BiPredicate<T, E> equalsFunction) {
        this.acceptableElements = acceptableElements;
        this.toId = toId;
        this.toName = toName;
        this.toTextureExtractor = toTextureExtractor;
        this.equalsFunction = equalsFunction;
    }

    public InListAcceptanceCondition(List<E> acceptableElements, Function<T,E> toListItem, Function<E, String> toId, Function<E, String> toName, Function<E, TextureExtractor> toTextureExtractor) {
        this(acceptableElements, toId, toName, toTextureExtractor, (v,e) -> toListItem.apply(v).equals(e));
    }

    @Override
    public boolean test(T value) {
        return acceptableElements.stream().anyMatch(e -> equalsFunction.test(value, e));
    }

    @Override
    public String getId() {
        return acceptableElements.stream()
                .map(toId)
                .collect(Collectors.joining("_OR_"));
    }

    @Override
    public String getName() {
        return acceptableElements.stream()
                .map(toName)
                .distinct()
                .collect(Collectors.joining(" or "));
    }

    @Override
    public List<TextureExtractor> getExamples() {
        return acceptableElements.stream()
                .map(toTextureExtractor).toList();
    }

    public <M> InListAcceptanceCondition<M,E> mapEquals(BiPredicate<M,E> equalsFunction) {
        return new InListAcceptanceCondition<>(acceptableElements, toId, toId, toTextureExtractor, equalsFunction);
    }

    public static InListAcceptanceCondition<ItemStack, Item> item(Item... items) {
        return new InListAcceptanceCondition<>(
                Arrays.asList(items),
                ItemStack::getItem,
                ItemUtil::getItemId,
                ItemUtil::getItemName,
                ItemUtil::getItemTextureExtractor
        );
    }

    @SafeVarargs
    public static InListAcceptanceCondition<Pair<Item, Integer>, Pair<Item, Integer>> itemWithCount(Pair<Item, Integer>... items) {
        return new InListAcceptanceCondition<>(
                Arrays.asList(items),
                p -> (p.getB() == 1 ? "" :  p.getB() + "_") + ItemUtil.getItemId(p.getA()),
                p -> (p.getB() == 1 ? "" :  p.getB() + " ") + ItemUtil.getItemName(p.getA()),
                p -> ItemTextureExtractor.stack(p.getA(), p.getB()),
                (a, b) -> a.getA() == b.getA() && a.getB() >= b.getB()
        );
    }

    public static InListAcceptanceCondition<EntityType<?>, EntityType<?>> entity(EntityType<?>... entities) {
        return new InListAcceptanceCondition<>(
                Arrays.asList(entities),
                e -> e,
                EntityUtil::getEntityId,
                EntityUtil::getEntityName,
                EntityUtil::getEntityTextureExtractor
        );
    }

    public static InListAcceptanceCondition<EntityType<?>, EntityType<?>> spawnEgg(EntityType<?>... entities) {
        return new InListAcceptanceCondition<>(
                Arrays.asList(entities),
                e -> e,
                EntityUtil::getEntityId,
                EntityUtil::getEntityName,
                e -> EntityUtil.getSpawnEgg(e).map(i -> (TextureExtractor)ItemTextureExtractor.item(i)).orElse(EntityUtil.getEntityTextureExtractor(e))
        );
    }

    @SafeVarargs
    public static InListAcceptanceCondition<ItemStack, Holder<Potion>> potionEffect(Holder<Potion>... potions) {
        return new InListAcceptanceCondition<>(
                Arrays.asList(potions),
                i -> {
                    PotionContents contents = i.get(DataComponents.POTION_CONTENTS);
                    return contents == null ? null : contents.potion().orElse(null);
                },
                p -> p.value().name().toUpperCase(),
                p -> "Potion of " + BuilderUtil.idToName(p.value().name()),
                p -> new ItemTextureExtractor(ItemUtil.applyComponent(Items.POTION.getDefaultInstance(), DataComponents.POTION_CONTENTS, new PotionContents(p)))
        );
    }

    public static InListAcceptanceCondition<Player, FallLocation> fallLocation(List<Pair<FallLocation, Supplier<TextureExtractor>>> locations) {
        return new InListAcceptanceCondition<>(
                locations.stream().map(Pair::getA).toList(),
                FallLocation::getCurrentFallLocation,
                l -> l.id().toUpperCase(),
                l -> BuilderUtil.idToName(l.id()),
                l -> locations.stream().filter(p -> p.getA() == l).findFirst().get().getB().get()
        );
    }

    public static InListAcceptanceCondition<Identifier, Identifier> advancement(Identifier... identifiers) {
        return new InListAcceptanceCondition<>(
                Arrays.asList(identifiers),
                e -> e,
                BuilderUtil::identifierToId,
                a -> {
                    Advancement advancement = LockoutClient.allAdvancements.get(a);
                    if (advancement == null) {
                        return "\"" + BuilderUtil.identifierToName(a) + "\" Advancement";
                    }
                    return "\"" + advancement.display().get().getTitle().getString() + "\" Advancement";
                },
                a -> {
                    Advancement advancement = LockoutClient.allAdvancements.get(a);
                    if (advancement == null) {
                        return ItemTextureExtractor.item(Items.BARRIER);
                    }
                    return new StackingTextureExtractor(List.of(
                            GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/advancements/challenge_frame_unobtained.png")),
                            ItemTextureExtractor.item(advancement.display().get().getIcon().item().value())
                    ), 3);
                }
        );
    }

    public static InListAcceptanceCondition<Identifier, Identifier> statistic(Supplier<TextureExtractor> extractor, Identifier... statistics) {
        return new InListAcceptanceCondition<>(
                Arrays.asList(statistics),
                a -> a,
                BuilderUtil::identifierToId,
                BuilderUtil::identifierToName,
                _ -> extractor.get()
        );
    }

    public static InListAcceptanceCondition<ResourceKey<DamageType>, ResourceKey<DamageType>> damageType(Supplier<TextureExtractor> extractor, ResourceKey<DamageType>... types) {
        return new InListAcceptanceCondition<>(
                Arrays.asList(types),
                t -> t,
                d -> BuilderUtil.identifierToId(d.identifier()),
                d -> BuilderUtil.identifierToName(d.identifier()),
                _ -> extractor.get()
        );
    }
}
