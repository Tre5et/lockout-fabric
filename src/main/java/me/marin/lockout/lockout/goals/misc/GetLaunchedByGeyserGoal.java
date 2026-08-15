package me.marin.lockout.lockout.goals.misc;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.Goal;
import me.marin.lockout.lockout.texture.TextureProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class GetLaunchedByGeyserGoal extends Goal implements TextureProvider {
    public GetLaunchedByGeyserGoal(String id, String data) {
        super(id, data);
    }

    @Override
    public String getGoalName() {
        return "Get launched by a Geyser";
    }

    @Override
    public ItemStack getTextureItemStack() {
        return null;
    }

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/geyser_launch.png");
    @Override
    public Identifier getTextureIdentifier() {
        return TEXTURE;
    }
}
