package me.marin.lockout.lockout.goal.acceptance;

import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.mixin.server.PlayerInventoryAccessor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AllInInventoryAcceptanceCondition implements AcceptanceCondition<Inventory> {
    private final List<AcceptanceCondition<ItemStack>> conditions;

    public AllInInventoryAcceptanceCondition(List<AcceptanceCondition<ItemStack>> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean test(Inventory value) {
        return conditions.stream().allMatch(c -> {
            for(ItemStack item : ((PlayerInventoryAccessor) value).getPlayerInventory()) {
                if(!item.isEmpty() && c.test(item)) return true;
            }
            var offHandItem = ((PlayerInventoryAccessor) value).getEquipment().get(EquipmentSlot.OFFHAND);
            return !offHandItem.isEmpty() && c.test(offHandItem);
        });
    }

    @Override
    public String getId() {
        return conditions.stream().map(AcceptanceCondition::getId).collect(Collectors.joining("_AND_"));
    }

    @Override
    public String getName() {
        return conditions.stream().map(AcceptanceCondition::getName).collect(Collectors.joining(" and "));
    }

    @Override
    public List<TextureExtractor> getExamples() {
        return conditions.stream().flatMap(c -> c.getExamples().stream()).toList();
    }

    @SafeVarargs
    public static AllInInventoryAcceptanceCondition of(AcceptanceCondition<ItemStack>... stacks) {
        return new AllInInventoryAcceptanceCondition(Arrays.asList(stacks));
    }
}
