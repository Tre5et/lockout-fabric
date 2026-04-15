package me.marin.lockout.lockout.goals.misc;

import me.marin.lockout.LockoutTeam;
import me.marin.lockout.LockoutTeamServer;
import me.marin.lockout.lockout.Goal;
import me.marin.lockout.lockout.interfaces.HasTooltipInfo;
import me.marin.lockout.lockout.texture.CustomTextureRenderer;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Boat2KmGoal extends Goal implements CustomTextureRenderer, HasTooltipInfo {

    private static final ItemStack ITEM_STACK = Items.OAK_BOAT.getDefaultInstance();
    public Boat2KmGoal(String id, String data) {
        super(id, data);
    }

    @Override
    public String getGoalName() {
        return "Boat 2km";
    }

    @Override
    public ItemStack getTextureItemStack() {
        return ITEM_STACK;
    }

    @Override
    public boolean renderTexture(GuiGraphics context, int x, int y, int tick) {
        context.renderItem(ITEM_STACK, x, y);
        context.renderItemDecorations(Minecraft.getInstance().font,  ITEM_STACK, x, y, "2km");
        return true;
    }

    @Override
    public List<String> getTooltip(LockoutTeam team, Player player) {
        List<String> tooltip = new ArrayList<>();
        int maxDistance = 0;
        for (UUID playerId : ((LockoutTeamServer) team).getPlayerIds()) {
            maxDistance = Math.max(maxDistance, LockoutServer.lockout.distanceBoated.getOrDefault(playerId, 0));
        }

        tooltip.add(" ");
        tooltip.add("Distance: " + Math.min(2000, maxDistance / 100) + "/2000m");
        tooltip.add(" ");

        return tooltip;
    }

    @Override
    public List<String> getSpectatorTooltip() {
        List<String> tooltip = new ArrayList<>();

        tooltip.add(" ");
        for (LockoutTeam team : LockoutServer.lockout.getTeams()) {
            int maxDistance = 0;
            for (UUID playerId : ((LockoutTeamServer) team).getPlayerIds()) {
                maxDistance = Math.max(maxDistance, LockoutServer.lockout.distanceBoated.getOrDefault(playerId, 0));
            }
            tooltip.add(team.getColor() + team.getDisplayName() + ChatFormatting.RESET + ": " + Math.min(2000, maxDistance / 100) + "/2000m");
        }
        tooltip.add(" ");

        return tooltip;
    }
}
