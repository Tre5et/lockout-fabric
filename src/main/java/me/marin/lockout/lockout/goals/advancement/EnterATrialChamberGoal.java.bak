package me.marin.lockout.lockout.goals.advancement;

import me.marin.lockout.lockout.interfaces.AdvancementGoal;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.List;

public class EnterATrialChamberGoal extends AdvancementGoal {

    private static final ItemStack ITEM_STACK = Items.TRIAL_KEY.getDefaultInstance();

    public EnterATrialChamberGoal(String id, String data) {
        super(id, data);
    }

    private static final List<Identifier> ADVANCEMENTS = List.of(Identifier.fromNamespaceAndPath("minecraft", "adventure/minecraft_trials_edition"));
    @Override
    public List<Identifier> getAdvancements() {
        return ADVANCEMENTS;
    }

    @Override
    public String getGoalName() {
        return "Enter a Trial Chamber";
    }

    @Override
    public ItemStack getTextureItemStack() {
        return ITEM_STACK;
    }

}
