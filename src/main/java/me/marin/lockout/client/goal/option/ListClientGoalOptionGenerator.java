package me.marin.lockout.client.goal.option;

import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import lombok.Getter;
import me.marin.lockout.lockout.goal.option.ListGoalOptionGenerator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class ListClientGoalOptionGenerator<T> extends ListGoalOptionGenerator<T> implements ClientGoalOptionGenerator<T> {
    @Getter
    private final String title;
    private final Function<T,String> toName;

    public ListClientGoalOptionGenerator(String title, List<T> entries, TypeToken<T> typeToken, Function<T, String> toName) {
        super(entries, typeToken);
        this.title = title;
        this.toName = toName;
    }

    @Override
    public int getPreferredRenderWidth() {
        return 100;
    }

    @Override
    public int getPreferredRenderHeight() {
        return Widget.TITLE_HEIGHT + Widget.MARGIN_Y + Widget.ITEM_HEIGHT * getEntries().size();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public AbstractWidget getWidget(int x, int y, int width, int height, Font font, Consumer<T> update, T current) {
        return new Widget(x, y, width, height, Component.literal(title), update);
    }

    @Environment(EnvType.CLIENT)
    public class Widget extends AbstractScrollArea {
        private static final int TITLE_HEIGHT = 14;
        private static final int ITEM_HEIGHT = 18;
        private static final int MARGIN_Y = 2;
        private static final int MARGIN_X = 10;
        private static final Style TITLE_STYLE = Style.EMPTY.withBold(true).withColor(TextColor.WHITE).withUnderlined(true);

        private final Consumer<T> update;
        private T hovered = null;

        public Widget(int x, int y, int width, int height, Component message, Consumer<T> update) {
            super(x, y + TITLE_HEIGHT, width, height - TITLE_HEIGHT, message, defaultSettings(10));
            this.update = update;
        }

        @Override
        protected int contentHeight() {
            return ITEM_HEIGHT * getEntries().size() + MARGIN_Y;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
            Font textRenderer = Minecraft.getInstance().font;

            context.text(textRenderer, FormattedCharSequence.forward(title, TITLE_STYLE), getX(), getY() - TITLE_HEIGHT + MARGIN_Y, 0xFF000000);

            hovered = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;
            context.enableScissor(this.getX() - 1, this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight());

            if(hovered != null) {
                context.requestCursor(CursorTypes.POINTING_HAND);
            }

            int y = MARGIN_Y;
            for (T entry : getEntries()) {
                String name = toName.apply(entry);
                context.text(textRenderer, name, getX() + 2, getY() + y - (int)scrollAmount() + 5, Color.WHITE.getRGB());
                if (Objects.equals(entry, hovered)) {
                    // Draw border manually since drawBorder method doesn't exist
                    context.fill(getX() - 1, getY() + y - (int)scrollAmount() - 1, getX() + getWidth() - MARGIN_X, getY()+ y - (int)scrollAmount(), Color.LIGHT_GRAY.getRGB());
                    context.fill(getX() - 1, getY()+ y - (int)scrollAmount() + 15, getX() + getWidth() - MARGIN_X, getY() + y - (int)scrollAmount() + 16, Color.LIGHT_GRAY.getRGB());
                    context.fill(getX() - 1, getY() + y - (int)scrollAmount() - 1, getX(), getY() + y - (int)scrollAmount() + 16, Color.LIGHT_GRAY.getRGB());
                    context.fill(getX() + getWidth() - MARGIN_X - 1, getY() + y - (int)scrollAmount() - 1, getX() + getWidth() - MARGIN_X, getY() + y - (int)scrollAmount() + 16, Color.LIGHT_GRAY.getRGB());
                }
                y += ITEM_HEIGHT;
            }

            context.disableScissor();
            this.extractScrollbar(context, mouseX, mouseY);
        }

        protected final T getEntryAtPosition(double x, double y) {
            double relativeX = x - this.getX();
            double relativeY = y - getY() - MARGIN_Y + scrollAmount();
            int index = (int)(relativeY / ITEM_HEIGHT);

            if(relativeX >= 0 && relativeX < this.getWidth() - MARGIN_X && index >= 0 && index < getEntries().size()) {
                return getEntries().get(index);
            }
            return null;
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean consumed) {
            if (hovered != null) {
                update.accept(hovered);
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            }
            var bl = updateScrolling(click);
            return super.mouseClicked(click, consumed) || bl;
        }

        @Override
        protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {}
    }
}
