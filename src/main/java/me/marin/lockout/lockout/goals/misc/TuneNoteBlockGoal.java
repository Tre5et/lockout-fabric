package me.marin.lockout.lockout.goals.misc;

import me.marin.lockout.lockout.interfaces.IncrementStatGoal;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.List;

public class TuneNoteBlockGoal extends IncrementStatGoal {

    public TuneNoteBlockGoal(String id, String data) {
        super(id, data);
    }

    private static final List<Identifier> STATS = List.of(Stats.TUNE_NOTEBLOCK);
    @Override
    public List<Identifier> getStats() {
        return STATS;
    }

    @Override
    public String getGoalName() {
        return "Tune a Note Block";
    }

    private static final ItemStack ITEM_STACK = Items.NOTE_BLOCK.getDefaultInstance();
    @Override
    public ItemStack getTextureItemStack() {
        return ITEM_STACK;
    }

}
