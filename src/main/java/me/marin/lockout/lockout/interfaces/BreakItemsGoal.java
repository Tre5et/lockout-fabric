package me.marin.lockout.lockout.interfaces;

import me.marin.lockout.lockout.Goal;
import net.minecraft.world.item.Item;

public abstract class BreakItemsGoal extends Goal {

    public BreakItemsGoal(String id, String data) {
        super(id, data);
    }

    public abstract boolean satisfiedBy(Item item);
}
