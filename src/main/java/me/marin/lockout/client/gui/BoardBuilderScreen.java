package me.marin.lockout.client.gui;

import me.marin.lockout.Lockout;
import me.marin.lockout.client.LockoutClient;
import me.marin.lockout.client.goal.ClientGoal;
import me.marin.lockout.client.goal.option.ClientGoalOptionGenerator;
import me.marin.lockout.lockout.GoalRegistry;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;

import static me.marin.lockout.Constants.*;

public class BoardBuilderScreen extends Screen {
    private static final int GOAL_SIZE = 16;
    private static final int GOAL_PADDING = 2;
    private static final int BOARD_PADDING = 8;
    private static final int BOARD_MARGIN = 8;
    private static final int BUTTON_SIZE = 20;
    private static final int BUTTON_MARGIN = 8;

    public boolean displaySearch = false;
    public boolean displayEditData = false;
    private int sideBarWidth = 0;
    private int boardX = 0;
    private int boardY = 0;
    private int boardSize = 0;

    private EditBox titleTextField;
    private Button saveButton;
    private StringWidget saveErrorTextWidget;
    private Button clearBoardButton;
    private Button closeButton;
    private Button closeSearchButton;
    private Button increaseSizeButton;
    private Button decreaseSizeButton;
    private EditBox searchTextField;
    private BoardBuilderSearchWidget boardBuilderSearchWidget;
    private Button closeEditDataButton;


    public BoardBuilderScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        super.init();
        Font textRenderer = Minecraft.getInstance().font;

        int centerX = width / 2;
        int centerY = height / 2;

        boardSize = getBoardSize();
        int mainPanelWidth = getMainPanelWidth();
        int halfBoardSize = boardSize / 2;
        int halfMainPanelWidth = mainPanelWidth / 2;

        int availableSidebarWidth = width - mainPanelWidth;

        if (displaySearch) {
            sideBarWidth = Math.min(BoardBuilderSearchWidget.getPreferredWidth(font), availableSidebarWidth);

            double scrollY = boardBuilderSearchWidget == null ? 0 : boardBuilderSearchWidget.scrollAmount();
            boardBuilderSearchWidget = new BoardBuilderSearchWidget(
                    width - sideBarWidth,
                    40,
                    sideBarWidth,
                    height - 40 * 2, Component.empty());
            boardBuilderSearchWidget.setScrollAmount(scrollY);
            this.addRenderableWidget(boardBuilderSearchWidget);

            closeSearchButton = Button.builder(Component.nullToEmpty("<"), (_) -> closeSearch()).tooltip(Tooltip.create(Component.nullToEmpty("Close search"))).width(20).pos(boardBuilderSearchWidget.getX(), boardBuilderSearchWidget.getY() - 21).build();
            this.addRenderableWidget(closeSearchButton);

            searchTextField = new EditBox(textRenderer, closeSearchButton.getX() + closeSearchButton.getWidth() + 1 + 5, closeSearchButton.getY() + 1, boardBuilderSearchWidget.getWidth() - closeSearchButton.getWidth() - 2 - 5, 18, Component.empty());
            searchTextField.setResponder(s -> {
                BoardBuilderData.INSTANCE.setSearch(s);
                boardBuilderSearchWidget.searchUpdated(s);
            });
            if (!BoardBuilderData.INSTANCE.getSearch().isEmpty()) {
                searchTextField.setValue(BoardBuilderData.INSTANCE.getSearch());
            }
            searchTextField.setSuggestion(searchTextField.getValue().isEmpty() ? "Search goals.." : null);
            this.addRenderableWidget(searchTextField);
        } else if (displayEditData && BoardBuilderData.INSTANCE.getModifyingIdx() != null) {
            ClientGoal goal = BoardBuilderData.INSTANCE.getModifyingGoal();
            GoalBuilder<?,?> builder = GoalRegistry.INSTANCE.get(goal.getBuildData().id());
            ClientGoalOptionGenerator<?> generator = builder.getOptionSupplier().getClient();
            if(generator == null) {
                displayEditData = false;
            } else {
                sideBarWidth = Math.min(generator.getPreferredRenderWidth(), availableSidebarWidth);
                int height = Math.min(generator.getPreferredRenderHeight(), this.height - 66);
                int x = width - sideBarWidth;
                int y = centerY - 30 - (height / 2);
                try {
                    this.addRenderableWidget(generator.getWidgetUnsafe(x, y, sideBarWidth, height, textRenderer, (o) -> {
                        try {
                            BoardBuilderData.INSTANCE.setGoal(builder.buildClientGeneric(o));
                            rebuildWidgets();
                        } catch (IllegalGoalConstructionException e) {
                            Lockout.log("Failed to update goal " + builder + " with option " + o + ": " + e.getMessage());
                        }
                    }, goal.getBuildData().option().orElse(null)));
                } catch (IllegalGoalConstructionException e) {
                    Lockout.log("Failed to render goal option selector for goal " + builder.getStaticId() + " with option" + goal.getBuildData().option().orElse("null") + ": " + e.getMessage());
                }

                closeEditDataButton = Button.builder(Component.literal("Close"), (_) -> closeEditData()).width(50).pos(x, y + height + 6).build();
                this.addRenderableWidget(closeEditDataButton);
            }
        } else {
            sideBarWidth = 0;
        }

        int maxCenterX = width - sideBarWidth - halfMainPanelWidth;
        int mainContentCenterX = Math.min(centerX, maxCenterX);
        boardX = mainContentCenterX - halfBoardSize;
        boardY = centerY - halfBoardSize;

        titleTextField = new EditBox(textRenderer, mainContentCenterX - 60, boardY - 18 - 8, 120, 18, Component.empty());
        titleTextField.setResponder(BoardBuilderData.INSTANCE::setTitle);
        titleTextField.setValue(BoardBuilderData.INSTANCE.getTitle());
        titleTextField.setSuggestion(titleTextField.getValue().isEmpty() ? "Board Name" : null);
        this.addRenderableWidget(titleTextField);

        final int BOTTOM_BUTTONS_Y = height - 30;

        saveButton = Button.builder(Component.nullToEmpty("Save Board"), (_) -> saveGoals(10, height - 45)).width(85).pos(10, BOTTOM_BUTTONS_Y).build();
        saveButton.active = BoardBuilderData.INSTANCE.isValid();
        this.addRenderableWidget(saveButton);

        closeButton = Button.builder(Component.nullToEmpty("Close"), (_) -> onClose()).width(50).pos(width - 50 - 10, BOTTOM_BUTTONS_Y).build();
        this.addRenderableWidget(closeButton);

        clearBoardButton = Button.builder(Component.nullToEmpty("Clear Board"), (_) -> {
            BoardBuilderData.INSTANCE.clear();
            closeEditData();
            closeSearch();
        }).width(85).pos(closeButton.getX() - 85 - 10, BOTTOM_BUTTONS_Y).build();
        this.addRenderableWidget(clearBoardButton);

        increaseSizeButton = Button.builder(Component.literal("+"), (_) -> {
            BoardBuilderData.INSTANCE.incrementSize();
            rebuildWidgets();
        }).tooltip(Tooltip.create(Component.literal("Increase board size"))).width(BUTTON_SIZE).pos(mainContentCenterX + halfMainPanelWidth - BUTTON_SIZE - BUTTON_MARGIN, centerY - 10).build();
        increaseSizeButton.active = BoardBuilderData.INSTANCE.getSize() != MAX_BOARD_SIZE;
        this.addRenderableWidget(increaseSizeButton);

        decreaseSizeButton = Button.builder(Component.literal("-"), (_) -> {
            BoardBuilderData.INSTANCE.decrementSize();
            rebuildWidgets();
        }).tooltip(Tooltip.create(Component.literal("Decrease board size"))).width(BUTTON_SIZE).pos(mainContentCenterX - halfMainPanelWidth + BUTTON_MARGIN, centerY - 10).build();
        decreaseSizeButton.active = BoardBuilderData.INSTANCE.getSize() != MIN_BOARD_SIZE;
        this.addRenderableWidget(decreaseSizeButton);
    }

    private int getMainPanelWidth() {
        return getBoardSize() + 2 * BOARD_PADDING + 2 * BOARD_MARGIN + 2 * BUTTON_SIZE + 2 * BUTTON_MARGIN;
    }

    private int getBoardSize() {
        return BoardBuilderData.INSTANCE.getSize() * (GOAL_SIZE + GOAL_PADDING) - GOAL_PADDING;
    }

    private void saveGoals(int errorX, int errorY) {
        if(!BoardBuilderData.INSTANCE.isValid()) {
            showError("Invalid board, fix and try again.", errorX, errorY);
            return;
        }

        String boardName = BoardBuilderData.INSTANCE.getTitle().trim();
        if (boardName.isBlank()) {
            boardName = "Custom Board";
        }
        try {
            boardName = BoardBuilderIO.INSTANCE.getSuitableName(boardName);
        } catch (IOException e) {
            showError("Failed to generate valid board name.", errorX, errorY);
            Lockout.error(e);
        }

        try {
            BoardBuilderIO.INSTANCE.saveBoard(boardName, BoardBuilderData.INSTANCE.getGoals());
        } catch (IOException e) {
            showError("Failed to save board.", errorX, errorY);
            Lockout.error(e);
        }

        String finalBoardName = boardName;
        // TODO: Fix board builder
        Component openBoardFile = Component.literal("[Open file]").withStyle(style ->
                style.withClickEvent(new ClickEvent.OpenFile(BoardBuilderIO.INSTANCE.getBoardPath(finalBoardName).toFile().getAbsolutePath()))
                        .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty("Click to open board file.")))
                        .applyFormat(ChatFormatting.WHITE)
        );
        Component openBoardsDirectory = Component.literal("[View all boards]").withStyle(style ->
                style.withClickEvent(new ClickEvent.OpenFile(BoardBuilderIO.DIRECTORY.toFile().getAbsolutePath()))
                        .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty("Click to open boards directory.")))
                        .applyFormat(ChatFormatting.WHITE)
        );
        Minecraft.getInstance().player.sendSystemMessage(Component.literal("Saved custom board as " + boardName + BoardBuilderIO.FILE_EXTENSION + "!\n").withStyle(ChatFormatting.GREEN).append(openBoardFile).append(" ").append(openBoardsDirectory));
        onClose();
    }

    private void showError(String message, int x, int y) {
        saveErrorTextWidget = new StringWidget(Component.literal(message).withStyle(ChatFormatting.RED), font);
        saveErrorTextWidget.setPosition(x, y);
        this.addRenderableWidget(saveErrorTextWidget);
    }

    @Override
    public void onClose() {
        closeSearch();
        super.onClose();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        //this.renderBackground(context, mouseX, mouseY, delta);
        super.extractRenderState(extractor, mouseX, mouseY, delta);

        Optional<Integer> hoveredIdx = BoardBuilderData.INSTANCE.getHoveredIndex(mouseX, mouseY, boardX, boardY, GOAL_SIZE, GOAL_PADDING);
        Integer editingIdx = BoardBuilderData.INSTANCE.getModifyingIdx();

        extractor.blitSprite(RenderPipelines.GUI_TEXTURED, GUI_CENTER_IDENTIFIER, boardX - BOARD_PADDING, boardY - BOARD_PADDING, boardSize + 2*BOARD_PADDING, boardSize + 2*BOARD_PADDING);

        //drawCenterBoard(extractor, mouseX, mouseY);
        BoardBuilderData.INSTANCE.extractGoals(extractor, font, boardX, boardY, GOAL_SIZE, GOAL_PADDING, (_,_,_,_) -> {}, (x,y, index, _) -> {
            if(hoveredIdx.isPresent() && index == hoveredIdx.get()) {
                extractor.fill(x, y, x + GOAL_SIZE, y + GOAL_SIZE, GUI_CENTER_HOVERED_COLOR);
            }
            if (editingIdx != null && index == editingIdx) {
                drawBorder(extractor, x, y, GOAL_SIZE, GOAL_SIZE, Color.RED.getRGB());
            }
        });

        if(hoveredIdx.isPresent()) {
            ClientGoal hoveredGoal = BoardBuilderData.INSTANCE.getGoals().get(hoveredIdx.get());
            if(hoveredGoal != null) {
                hoveredGoal.extractTooltip(extractor, font, mouseX, mouseY, false, false, Optional.empty(),
                    hoveredGoal.getBuildData().option().isPresent() ? Component.literal("Right-click to edit data.").withStyle(ChatFormatting.ITALIC).withColor(TextColor.GRAY) : null
                );
            }
        }

        titleTextField.setSuggestion(titleTextField.getValue().isEmpty() ? "Board Name" : null);
        if (displaySearch) {
            searchTextField.setSuggestion(searchTextField.getValue().isEmpty() ? "Search goals.." : null);
        }
        saveButton.active = BoardBuilderData.INSTANCE.isValid();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean consumed) {
        Optional<Integer> hoveredIdx = BoardBuilderData.INSTANCE.getHoveredIndex((int) click.x(), (int) click.y(), boardX, boardY, GOAL_SIZE, GOAL_PADDING);
        if ((click.button() == 0 || click.button() == 1) && hoveredIdx.isPresent()) {
            ClientGoal goal = BoardBuilderData.INSTANCE.getGoals().get(hoveredIdx.get());
            if (click.button() == 1 && goal != null && goal.getBuildData().option().isPresent()) {
                openEditData(hoveredIdx.get());
            } else {
                openSearch(hoveredIdx.get());
            }
            return true;
        } else {
            return super.mouseClicked(click, consumed);
        }
    }

    public void openSearch(int hoveredIdx) {
        displayEditData = false;
        displaySearch = true;

        BoardBuilderData.INSTANCE.setModifyingIdx(hoveredIdx);

        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        rebuildWidgets();
    }

    public void closeSearch() {
        displaySearch = false;

        BoardBuilderData.INSTANCE.setModifyingIdx(null);

        this.boardBuilderSearchWidget = null;
        this.closeSearchButton = null;
        this.searchTextField = null;

        rebuildWidgets();
    }

    public void openEditData(int hoveredIdx) {
        displaySearch = false;
        displayEditData = true;

        BoardBuilderData.INSTANCE.setModifyingIdx(hoveredIdx);

        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        rebuildWidgets();
    }

    public void closeEditData() {
        displayEditData = false;

        BoardBuilderData.INSTANCE.setModifyingIdx(null);

        this.closeEditDataButton = null;

        rebuildWidgets();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent input) {
        // Check if the pressed key matches the board keybinding
        if (LockoutClient.getBoardKeybinding().matches(input) && !titleTextField.isFocused() && !searchTextField.isFocused()) {
            this.onClose();
            return true;
        }
        return super.keyPressed(input);
    }

    private static void drawBorder(GuiGraphicsExtractor context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

}