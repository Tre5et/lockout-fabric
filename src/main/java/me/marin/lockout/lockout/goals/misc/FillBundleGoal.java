package me.marin.lockout.lockout.goals.misc;

import me.marin.lockout.lockout.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FillBundleGoal extends Goal {

    private static final ItemStack ITEM = Items.BUNDLE.getDefaultInstance();

    public FillBundleGoal(String id, String data) {
        super(id, data);
    }

    @Override
    public String getGoalName() {
        return "Fill Bundle";
    }

    @Override
    public ItemStack getTextureItemStack() {
        return ITEM;
    }
}
