package me.marin.lockout.lockout.goal.builder.item;

import lombok.NonNull;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.rendering.texture.CycleTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.mixin.server.PlayerInventoryAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ObtainItemWithStackCheckGoalBuilder extends ObtainItemGoalBuilder<Void> {
    private final Component name;
    private final Item item;
    private final Predicate<ItemStack> stackChecker;
    private final List<Consumer<ItemStack>> defaultStackBuilders;

    public ObtainItemWithStackCheckGoalBuilder(String id, GoalCategory category, Component name, Item item, Predicate<ItemStack> stackChecker, List<Consumer<ItemStack>> defaultStackBuilders) {
        super(id, category);
        this.name = name;
        this.item = item;
        this.stackChecker = stackChecker;
        this.defaultStackBuilders = defaultStackBuilders;
    }

    @Override
    protected boolean satisfiedBy(Inventory inventory, Void option) {
        for(ItemStack item : ((PlayerInventoryAccessor) inventory).getPlayerInventory()) {
            if(item.isEmpty() || !item.getItem().equals(this.item)) return false;
            return stackChecker.test(item);
        }
        var offHandItem = ((PlayerInventoryAccessor) inventory).getEquipment().get(EquipmentSlot.OFFHAND);
        return !offHandItem.isEmpty() && offHandItem.getItem().equals(this.item) && stackChecker.test(offHandItem);
    }

    @Override
    public @NonNull Component defaultName(Void option) {
        return name;
    }

    @Override
    public @NonNull TextureExtractor defaultTextureExtractor(Void option) {
        return new CycleTextureExtractor(defaultStackBuilders.stream()
                .map(b -> {
                    ItemStack stack = item.getDefaultInstance();
                    b.accept(stack);
                    return stack;
                })
                .map(ItemTextureExtractor::new)
                .toList()
        );
    }

    public static ObtainItemWithStackCheckGoalBuilder of(String id, String name, Item item, Predicate<ItemStack> stackChecker, List<Consumer<ItemStack>> stackBuilders) {
        return new ObtainItemWithStackCheckGoalBuilder(id, GoalCategory.OBTAINING_ITEMS, Component.literal(name), item, stackChecker, stackBuilders);
    }
}
