package me.marin.lockout.lockout.interfaces;

import me.marin.lockout.Constants;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.lockout.Goal;
import me.marin.lockout.lockout.texture.CustomTextureRenderer;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public abstract class VisitUniqueBiomesGoal extends Goal implements RequiresAmount, HasTooltipInfo, CustomTextureRenderer {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/unique_biomes.png");
    private final ItemStack DISPLAY_ITEM_STACK = Items.MAP.getDefaultInstance();

    public VisitUniqueBiomesGoal(String id, String data) {
        super(id, data);
        DISPLAY_ITEM_STACK.setCount(getAmount());
    }

    @Override
    public ItemStack getTextureItemStack() {
        return DISPLAY_ITEM_STACK;
    }

    @Override
    public boolean renderTexture(GuiGraphicsExtractor context, int x, int y, int tick) {
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, 16, 16, 16, 16);
        context.itemDecorations(Minecraft.getInstance().font, DISPLAY_ITEM_STACK, x, y);
        return true;
    }

    @Override
    public List<String> getTooltip(LockoutTeam team, Player player) {
        List<String> tooltip = new ArrayList<>();
        var biomes = LockoutServer.lockout.visitedBiomes.getOrDefault(team, new LinkedHashSet<>());

        tooltip.add(" ");
        tooltip.add("Unique Biomes Visited: " + biomes.size() + "/" + getAmount());
        tooltip.addAll(HasTooltipInfo.commaSeparatedList(biomes.stream().map(this::getBiomeName).toList()));
        tooltip.add(" ");

        return tooltip;
    }

    @Override
    public List<String> getSpectatorTooltip() {
        List<String> tooltip = new ArrayList<>();

        tooltip.add(" ");
        for (LockoutTeam team : LockoutServer.lockout.getTeams()) {
            var biomes = LockoutServer.lockout.visitedBiomes.getOrDefault(team, new LinkedHashSet<>());
            tooltip.add(team.getColor() + team.getDisplayName() + ChatFormatting.RESET + ": " + biomes.size() + "/" + getAmount());
        }
        tooltip.add(" ");

        return tooltip;
    }

    private String getBiomeName(Identifier id) {
        // Biome criteria names are like "minecraft:forest"
        // We can capitalize and replace underscores for a simple name
        String path = id.getPath();
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

}
