package me.marin.lockout.client.goal.option;

import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.function.Consumer;

public interface ClientGoalOptionGenerator<T> extends GoalOptionGenerator<T> {
    int getPreferredRenderWidth();
    int getPreferredRenderHeight();
    @Environment(EnvType.CLIENT)
    AbstractWidget getWidget(int x, int y, int width, int height, Font font, Consumer<T> update, T current);


    default AbstractWidget getWidgetUnsafe(int x, int y, int width, int height, Font font, Consumer<T> update, String current) throws IllegalGoalConstructionException {
        return getWidget(x, y, width, height, font, update, deserialize(current));
    }
}
