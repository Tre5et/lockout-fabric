package me.marin.lockout.client.gui;

import me.marin.lockout.lockout.GoalRegistry;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class BoardBuilderSearchWidget extends AbstractScrollArea {

    private static final int MARGIN_X = 3;
    private static final int MARGIN_Y = 3;
    private static final int ITEM_HEIGHT = 18;

    private int rowWidth;
    private int left;
    private int right;
    private int top;
    private static GoalEntry hovered;
    private final List<GoalEntry> registeredGoals;
    private List<GoalEntry> visibleGoals;

    public BoardBuilderSearchWidget(int x, int y, int width, int height, Component text) {
        super(x, y, width, height, text, defaultSettings(1));
        this.registeredGoals = GoalRegistry.INSTANCE.getRegisteredGoals().stream()
                .map(GoalEntry::new)
                .toList();
        visibleGoals = new ArrayList<>(registeredGoals);
        searchUpdated(BoardBuilderData.INSTANCE.getSearch());
    }

    public void setScrollAmount(double scrollY) {
        super.setScrollAmount(scrollY);
    }

    public double scrollAmount() {
        return super.scrollAmount();
    }

    @Override
    protected int contentHeight() {
        return visibleGoals.size() * ITEM_HEIGHT;
    }

    @Override
    protected double scrollRate() {
        return ITEM_HEIGHT / 2.0;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.rowWidth = getWidth() - MARGIN_X * 2;
        this.left = getX() + MARGIN_X;
        this.top = getY();
        this.right = getX() + getWidth() - MARGIN_X;

        hovered = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;

        context.enableScissor(this.left - 1, this.top, this.right + 1, getY() + getHeight());

        int y = 4;
        for (GoalEntry goalEntry : visibleGoals) {
            goalEntry.extractContent(context, getX() + MARGIN_X, getY() + y - (int)scrollAmount() - 3, Objects.equals(goalEntry, hovered), delta);
            y += 18;
        }

        context.disableScissor();
        this.extractScrollbar(context, this.right, this.top);
    }

    protected final GoalEntry getEntryAtPosition(double x, double y) {
        int halfRowWidth = this.rowWidth / 2;
        int centerX = this.left + this.width / 2;
        int left = centerX - halfRowWidth;
        int right = centerX + halfRowWidth;
        int scrolledY = Mth.floor(y - (double)this.top) + (int)scrollAmount() - MARGIN_Y + 3;
        int idx = scrolledY / ITEM_HEIGHT;
        if (x < (this.right + MARGIN_X - 6) && x >= (double) left && x <= (double) right && idx >= 0 && scrolledY >= 0 && idx < visibleGoals.size()) {
            return visibleGoals.get(idx);
        }
        return null;
    }

    public void searchUpdated(String search) {
        setScrollAmount(0);
        visibleGoals = new ArrayList<>(registeredGoals).stream()
                .filter(e -> e.matchesSearch(search))
                .collect(Collectors.toList());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean consumed) {
        if (hovered != null) {
            BoardBuilderData.INSTANCE.setGoal(hovered.getCurrentExampleGoal());
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
        var bl = updateScrolling(click);
        return super.mouseClicked(click, consumed) || bl;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {}

    public static final class GoalEntry extends ObjectSelectionList.Entry<GoalEntry> {

        private final List<Goal> exampleGoals;
        private final String goalNameSuffix;
        private final List<String> goalNames;

        public GoalEntry(GoalBuilder<?> goalBuilder) {
            this.exampleGoals = goalBuilder.buildExamples();
            this.goalNameSuffix = exampleGoals.size() > 1 ? " (+" + (exampleGoals.size() - 1) + ")" : "";
            this.goalNames = exampleGoals.stream()
                    .map(g -> g.getName() + goalNameSuffix)
                    .toList();
        }

        public Goal getCurrentExampleGoal() {
            int seconds = Math.toIntExact(System.currentTimeMillis() / 1000);
            int mod = (seconds / 3) % exampleGoals.size();
            return exampleGoals.get(mod);
        }

        public boolean matchesSearch(String search) {
            return goalNames.stream()
                    .anyMatch(g -> g.toLowerCase().contains(search.toLowerCase()));
        }

        @Override
        public Component getNarration() {
            return Component.empty();
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor context, int x, int y, boolean hovered, float tickDelta) {
            Font textRenderer = Minecraft.getInstance().font;

            Goal goal = getCurrentExampleGoal();
            String displayName = goal.getName() + goalNameSuffix;

            goal.render(context, textRenderer, x, y);
            context.text(textRenderer, displayName, x + 18, y + 5, Color.WHITE.getRGB());
            if (hovered) {
                // Draw border manually since drawBorder method doesn't exist
                context.fill(x - 1, y - 1, x + 18 + textRenderer.width(displayName) + 1, y, Color.LIGHT_GRAY.getRGB());
                context.fill(x - 1, y + 15, x + 18 + textRenderer.width(displayName) + 1, y + 16, Color.LIGHT_GRAY.getRGB());
                context.fill(x - 1, y - 1, x, y + 16, Color.LIGHT_GRAY.getRGB());
                context.fill(x + 18 + textRenderer.width(displayName), y - 1, x + 18 + textRenderer.width(displayName) + 1, y + 16, Color.LIGHT_GRAY.getRGB());
            }
        }
    }


}
