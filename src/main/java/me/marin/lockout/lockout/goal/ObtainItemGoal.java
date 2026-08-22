package me.marin.lockout.lockout.goal;

import me.marin.lockout.lockout.goal.tooltip.TooltipInfo;
import me.marin.lockout.lockout.texture.TextureRenderer;
import net.minecraft.world.entity.player.Inventory;
import oshi.util.tuples.Pair;

import java.util.function.Predicate;

public class ObtainItemGoal extends SatisfiableGoal<Inventory> {
    public ObtainItemGoal(String id, String name, TooltipInfo tooltipInfo, TextureRenderer textureRenderer, Pair<String, String> buildData, Predicate<Inventory> satisfiedPredicate) {
        super(id, name, tooltipInfo, textureRenderer, buildData, satisfiedPredicate);
    }
}
