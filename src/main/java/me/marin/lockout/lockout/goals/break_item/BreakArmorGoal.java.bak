package me.marin.lockout.lockout.goals.break_item;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.interfaces.BreakItemsGoal;
import me.marin.lockout.lockout.texture.TextureProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BreakArmorGoal extends BreakItemsGoal implements TextureProvider {

    public BreakArmorGoal(String id, String data) {
        super(id, data);
    }

    @Override
    public boolean satisfiedBy(Item item) {
        ItemStack stack = item.getDefaultInstance();
        return stack.has(DataComponents.EQUIPPABLE);
    }

    @Override
    public String getGoalName() {
        return "Break any Armor Piece";
    }

    @Override
    public ItemStack getTextureItemStack() {
        return null;
    }

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/break_armor.png");
    @Override
    public Identifier getTextureIdentifier() {
        return TEXTURE;
    }
}
