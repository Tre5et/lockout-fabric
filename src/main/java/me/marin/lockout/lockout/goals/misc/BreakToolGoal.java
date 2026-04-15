package me.marin.lockout.lockout.goals.misc;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.Goal;
import me.marin.lockout.lockout.texture.TextureProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class BreakToolGoal extends Goal implements TextureProvider {

    public BreakToolGoal(String id, String data) {
        super(id, data);
    }

    @Override
    public String getGoalName() {
        return "Break a Tool";
    }

    @Override
    public ItemStack getTextureItemStack() {
        return null;
    }

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/break_tool.png");
    @Override
    public Identifier getTextureIdentifier() {
        return TEXTURE;
    }
}
