package me.marin.lockout.lockout.goals.obtain;

import me.marin.lockout.lockout.interfaces.ObtainAllItemsGoal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.List;

public class ObtainAllDyesGoal extends ObtainAllItemsGoal {

    private static final List<Item> ITEMS = List.of(
            Items.DYE.white(),
            Items.DYE.orange(),
            Items.DYE.magenta(),
            Items.DYE.lightBlue(),
            Items.DYE.yellow(),
            Items.DYE.lime(),
            Items.DYE.pink(),
            Items.DYE.gray(),
            Items.DYE.lightGray(),
            Items.DYE.cyan(),
            Items.DYE.purple(),
            Items.DYE.blue(),
            Items.DYE.brown(),
            Items.DYE.green(),
            Items.DYE.red(),
            Items.DYE.black()
    );

    public ObtainAllDyesGoal(String id, String data) {
        super(id, data);
    }

    @Override
    public List<Item> getItems() {
        return ITEMS;
    }

    @Override
    public String getGoalName() {
        return "Obtain all Dyes";
    }

}

