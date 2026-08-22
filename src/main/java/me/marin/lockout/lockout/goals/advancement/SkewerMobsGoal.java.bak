package me.marin.lockout.lockout.goals.advancement;

import me.marin.lockout.lockout.interfaces.AdvancementGoal;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.List;

public class SkewerMobsGoal extends AdvancementGoal {

    private static final ItemStack ITEM_STACK = Items.NETHERITE_SPEAR.getDefaultInstance();
    private static final List<Identifier> ADVANCEMENTS = List.of(Identifier.fromNamespaceAndPath("minecraft", "adventure/spear_many_mobs"));

    public SkewerMobsGoal(String id, String data) {
        super(id, data);
    }

    @Override
    public String getGoalName() {
        return "Skewer 5 mobs at once";
    }

    @Override
    public ItemStack getTextureItemStack() {
        return ITEM_STACK;
    }

    @Override
    public List<Identifier> getAdvancements() {
        return ADVANCEMENTS;
    }
}
