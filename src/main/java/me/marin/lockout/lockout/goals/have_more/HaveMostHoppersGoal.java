package me.marin.lockout.lockout.goals.have_more;

import me.marin.lockout.Constants;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.LockoutTeamServer;
import me.marin.lockout.lockout.Goal;
import me.marin.lockout.lockout.interfaces.MostStatGoal;
import me.marin.lockout.lockout.texture.CustomTextureRenderer;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.UUID;

public class HaveMostHoppersGoal extends Goal implements CustomTextureRenderer, MostStatGoal {

    private static final ItemStack ITEM_STACK = Items.HOPPER.getDefaultInstance();
    
    public HaveMostHoppersGoal(String id, String data) {
        super(id, data);
    }

    @Override
    public String getGoalName() {
        return "Have the most Hoppers";
    }

    @Override
    public ItemStack getTextureItemStack() {
        return ITEM_STACK;
    }

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/overlay/up_overlay.png");
    
    @Override
    public boolean renderTexture(GuiGraphicsExtractor context, int x, int y, int tick) {
        context.item(ITEM_STACK, x, y);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0,0, 16, 16, 16, 16);
        return true;
    }

    @Override
    public int getStat(LockoutTeam team) {
        int max = 0;
        for (UUID uuid : ((LockoutTeamServer)team).getPlayerIds()) {
            max = Math.max(max, LockoutServer.lockout.playerHopperCounts.getOrDefault(uuid, 0));
        }
        return max;
    }
}
