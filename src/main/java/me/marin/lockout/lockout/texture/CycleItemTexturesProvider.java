package me.marin.lockout.lockout.texture;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;

public interface CycleItemTexturesProvider extends CustomTextureRenderer {

    List<Item> getItemsToDisplay();

    @Override
    default boolean renderTexture(GuiGraphics context, int x, int y, int tick) {
        int mod = tick % (60 * getItemsToDisplay().size());
        context.renderItem(getItemsToDisplay().get(mod / 60).getDefaultInstance(), x, y);
        return true;
    }

}
