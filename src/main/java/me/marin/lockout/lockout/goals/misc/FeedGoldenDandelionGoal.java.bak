package me.marin.lockout.lockout.goals.misc;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.Goal;
import me.marin.lockout.lockout.texture.CycleTexturesProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class FeedGoldenDandelionGoal extends Goal implements CycleTexturesProvider {
    public FeedGoldenDandelionGoal(String id, String data) {
        super(id, data);
    }

    @Override
    public String getGoalName() {
        return "Feed Golden Dandelion to any Baby Mob";
    }

    @Override
    public ItemStack getTextureItemStack() {
        return null;
    }

    private static final List<Identifier> TEXTURES = List.of(
            Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/golden_dandelion/chicken.png"),
            Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/golden_dandelion/cow.png"),
            Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/golden_dandelion/pig.png"),
            Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/golden_dandelion/sheep.png")
    );

    @Override
    public List<Identifier> getTexturesToDisplay() {
        return TEXTURES;
    }
}
