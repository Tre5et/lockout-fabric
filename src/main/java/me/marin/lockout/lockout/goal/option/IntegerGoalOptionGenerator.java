package me.marin.lockout.lockout.goal.option;

import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class IntegerGoalOptionGenerator implements GoalOptionGenerator<Integer> {
    private final String title;
    private final int min;
    private final int max;
    private final int step;
    private final int exampleCount;

    public IntegerGoalOptionGenerator(String title, int min, int max, int step, int exampleCount) {
        this.title = title;
        this.min = min;
        this.max = max;
        this.step = step;
        this.exampleCount = exampleCount;
    }

    @Override
    public Optional<Integer> generate(Function<Integer, Boolean> allowOption) {
        return RANDOM.ints(min/step, (max+1)/step)
                .distinct()
                .filter(allowOption::apply)
                .map(n -> n*step)
                .boxed()
                .findFirst();
    }

    @Override
    public List<Integer> examples() {
        return RANDOM.ints(min/step, (max+1)/step)
                .distinct()
                .limit(exampleCount)
                .map(n -> n*step)
                .boxed()
                .toList();
    }

    @Override
    public String serialize(Integer option) {
        return String.valueOf(option);
    }

    @Override
    public Integer deserialize(String serialized) throws IllegalGoalConstructionException {
        try {
            return Integer.parseInt(serialized);
        } catch (NumberFormatException e) {
            throw new IllegalGoalConstructionException("Not a valid integer: " + serialized, e);
        }
    }

    @Override
    public int getPreferredRenderWidth() {
        return Math.max(2*Widget.MARGIN_X + 72, 180);
    }

    @Override
    public int getPreferredRenderHeight() {
        return Widget.TITLE_HEIGHT + 2*Widget.MARGIN_Y + 18;
    }

    @Override
    public AbstractWidget getWidget(int x, int y, int width, int height, Font font, Consumer<Integer> update, Integer current) {
        return new Widget(x, y, width, height, Component.literal(title), update, current);
    }

    public class Widget extends AbstractWidget  {
        private static final int TITLE_HEIGHT = 14;
        private static final int MARGIN_Y = 2;
        private static final int MARGIN_X = 10;
        private static final Style TITLE_STYLE = Style.EMPTY.withBold(true).withColor(TextColor.WHITE).withUnderlined(true);

        private Integer current;

        private final Button minusButton;
        private final Button plusButton;

        public Widget(int x, int y, int width, int height, Component message, Consumer<Integer> update, Integer current) {
            super(x, y, width, height, message);
            this.current = current;
            this.minusButton = Button.builder(Component.literal("-"), (b) -> {
                if(this.current > min) {
                    update.accept(--this.current);
                }
            }).pos(x + MARGIN_X, y + TITLE_HEIGHT + MARGIN_Y).size(18, 18).build();
            this.plusButton = Button.builder(Component.literal("+"), (b) -> {
                if(this.current < max) {
                    update.accept(++this.current);
                }
            }).pos(x + 54 + MARGIN_X, y + TITLE_HEIGHT + MARGIN_Y).size(18, 18).build();
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float a) {
            Font font = Minecraft.getInstance().font;

            extractor.text(font, FormattedCharSequence.forward(title, TITLE_STYLE), getX() + MARGIN_X, getY() + MARGIN_Y, 0xFF000000);

            extractor.centeredText(font, String.valueOf(current), getX() + 36 + MARGIN_X, getY() + TITLE_HEIGHT + MARGIN_Y + (18 - font.lineHeight)/2, 0xFFFFFFFF);
            if(current > min) minusButton.extractRenderState(extractor, mouseX, mouseY, a);
            if(current < max) plusButton.extractRenderState(extractor, mouseX, mouseY, a);
        }

        @Override
        protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {}

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
            minusButton.mouseClicked(event, doubleClick);
            plusButton.mouseClicked(event, doubleClick);
            return super.mouseClicked(event, doubleClick);
        }
    }
}
